package performancemeasure;

import java.io.IOException;
import java.io.FileWriter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;
import org.apache.commons.cli.*;
import java.util.Random; 


public class ScalabiltyExp {

    public static CommandLine argparse(String[] args) throws ParseException {
        Options options = new Options();

        Option host = new Option("h", "host", true, "ip address & port of server");
        Option zkPath = new Option("z", "zkPath", true, "read write path on zookeeper");
        Option warmup = new Option("w", "warmup", true, "warmup time");
        Option duration = new Option("d", "duration", true, "duration time");
        Option mode = new Option("m", "mode", true, "read write different loading mode");
        Option numClient = new Option("n", "numClient", true, "number of total clients");
        Option percent = new Option("p", "percent", true, "percent of strong/weak 100=strong-only 0=weak-only");
        Option percentWrite = new Option("pw", "percentWrite", true, "percent of wrtie/read 100=write-only 0=read-only");
        options.addOption(host);
        options.addOption(zkPath);
        options.addOption(warmup);
        options.addOption(duration);
        options.addOption(mode);
        options.addOption(numClient);
        options.addOption(percent);
        options.addOption(percentWrite);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
            return cmd;
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("ScalabiltyExp", options);
            System.exit(1);
            return null;
        }
    }

    static class MyRunnable implements Runnable {
        ZooKeeper zk;
        String mode;
        int numClient;
        int warmup;
        int duration;
        int threadIdx;
        int percent;
        int percentWrite;
        ReentrantLock loglock;

        public MyRunnable(CommandLine cmd, ZooKeeper zk, int threadIdx, ReentrantLock loglock) {
            this.zk = zk;
            this.threadIdx = threadIdx;
            this.loglock = loglock;
            this.mode = cmd.getOptionValue("mode", "write");
            this.numClient = Integer.parseInt(cmd.getOptionValue("numClient", "1"));
            this.warmup = Integer.parseInt(cmd.getOptionValue("warmup", "120"));
            this.duration = Integer.parseInt(cmd.getOptionValue("duration", "180"));
            this.percent = Integer.parseInt(cmd.getOptionValue("percent", "100"));
            this.percentWrite = Integer.parseInt(cmd.getOptionValue("percentWrite", "100"));
        }

        public void run() {
            double durationSum = 0;
            int count = 0;
            long finalTime = (long)(System.nanoTime() + (this.warmup + this.duration) * 1e9);
            long finalTimeWarm = (long)(System.nanoTime() + this.warmup * 1e9);
            boolean doneWarm = false;

            while (true) {
                try {
                    String ops = getOps(this.mode, this.percentWrite);
                    String zkPath = getZkPath(this.percent);

                    long start, end;
                    if (ops == "write") {
                        start = System.nanoTime();
                        zk.setData("/" + zkPath, "test".getBytes(), -1);
                        end = System.nanoTime();
                    } else {
                        start = System.nanoTime();
                        zk.getData("/" + zkPath , false, null);
                        end = System.nanoTime();
                    }

                    ++count;
                    durationSum += (end - start) / 1000000.0;

                    if (!doneWarm && (System.nanoTime() > finalTimeWarm)) { // warmup timeout
                        System.out.println("Done Warmup [" + String.valueOf(this.percent) + "%] " + durationSum / count + " ms");
                        count = 0;
                        durationSum = 0;
                        doneWarm = true;
                    } 
                    if (System.nanoTime() > finalTime) { // duration timeout
                        System.out.println("Done Duration [" + String.valueOf(this.percent) + "%] " + durationSum / count + " ms");
                        loglock.lock();
                        FileWriter myWriter = new FileWriter(String.format("./outputs/exp-%s-%d%%write-%d%%strong-%d-client", 
                                                             this.mode, this.percentWrite, this.percent, this.numClient), true);
                        String stat = String.format("Thread - %d , Done [%d%%] total: %d time: %f ms avg lat: %f ms\n",
                                                     this.threadIdx, this.percent, count, durationSum, durationSum / count);
                        myWriter.write(stat);
                        myWriter.flush();
                        myWriter.close();
                        loglock.unlock();
                        break;
                    }
                } catch (KeeperException|InterruptedException|IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private String getOps(String ops, Integer percentWrite) {
            if (ops == "write") {
                this.percentWrite = 100; // update for log filename
                return ops;
            } else if (ops == "read") {
                this.percentWrite = 0; // update for log filename
                return ops;
            }
            Random rand = new Random(); 
            if (percentWrite > rand.nextInt(100)) {
                return "write"; // write
            } else {
                return "read"; // read
            }
        }

        private String getZkPath(Integer percent) {
            if (percent == 100) {
                return "1"; // Strong
            } else if (percent == 0) {
                return "2"; // Weak
            } else {
                
            }
            Random rand = new Random(); 
            if (percent > rand.nextInt(100)) {
                return "1"; // Strong
            } else {
                return "2"; // Weak
            }
        }
    }
    public static void main(String[] args)  throws IOException, KeeperException, InterruptedException, ParseException {
        CommandLine cmd = argparse(args);
        String setting = String.format("host:%s strong:%s%% write:%s%% warm:%s duration:%s mode:%s numClient:%s", 
                                        cmd.getOptionValue("host"), cmd.getOptionValue("percent"), cmd.getOptionValue("percentWrite"),
                                        cmd.getOptionValue("warmup"), cmd.getOptionValue("duration"),
                                        cmd.getOptionValue("mode"), cmd.getOptionValue("numClient"));
        System.out.println(setting);

        ReentrantLock lock = new ReentrantLock(); // this is for writing stat to log
        int num = Integer.parseInt(cmd.getOptionValue("numClient", "1"));
        String host = cmd.getOptionValue("host", "localhost:2182");
        for (int i = 0; i < num ; i++) {
            System.out.println("starting running thread - " + i);
            Runnable r = new MyRunnable(cmd, new ZooKeeper(host, 5000, null), i, lock);
            new Thread(r).start();
        }
    }
}