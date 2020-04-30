package performancemeasure;

import java.util.*;
import java.io.IOException;
import java.io.FileWriter;
import java.io.BufferedWriter;
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
        Option numStrong = new Option("ns", "numStrong", true, "number of strong clients");
        Option numWeak = new Option("nw", "numWeak", true, "number of weak clients");
        Option percentWrite = new Option("pw", "percentWrite", true, "percent of wrtie/read 100=write-only 0=read-only");
        options.addOption(host);
        options.addOption(zkPath);
        options.addOption(warmup);
        options.addOption(duration);
        options.addOption(numStrong);
        options.addOption(numWeak);
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
        String zkPath; // this determine strong 1 or weak 2
        // int numClient;
        int numStrong;
        int numWeak;
        int warmup;
        int duration;
        int threadIdx;
        int percentWrite;
        ReentrantLock loglock;
        
        /* total */
        double durationSum;
        int count;

        /* seperate write & read */
        double durationSumWrite;
        double durationSumRead;
        int countWrite;
        int countRead;

        /* CDF duration */
        List<Double> durationLogWrite = new ArrayList<>();
        // List<Double> durationLogRead = new ArrayList<>();

        public MyRunnable(CommandLine cmd, ZooKeeper zk, int threadIdx, ReentrantLock loglock, String zkPath) {
            this.zk = zk;
            this.zkPath = zkPath;
            this.loglock = loglock;
            this.threadIdx = threadIdx;

            // this.numClient = Integer.parseInt(cmd.getOptionValue("numClient", "1"));
            this.numStrong = Integer.parseInt(cmd.getOptionValue("numStrong", "0"));
            this.numWeak = Integer.parseInt(cmd.getOptionValue("numWeak", "0"));
            this.warmup = Integer.parseInt(cmd.getOptionValue("warmup", "120"));
            this.duration = Integer.parseInt(cmd.getOptionValue("duration", "180"));
            this.percentWrite = Integer.parseInt(cmd.getOptionValue("percentWrite", "100"));
        }

        public void run() {
            this.durationSum = 0;
            this.count = 0;
            long finalTime = (long)(System.nanoTime() + (this.warmup + this.duration) * 1e9);
            long finalTimeWarm = (long)(System.nanoTime() + this.warmup * 1e9);
            boolean doneWarm = false;

            while (true) {
                try {
                    String ops = getOps(this.percentWrite);

                    long start, end;
                    if (ops == "write") {
                        start = System.nanoTime();
                        zk.setData("/" + this.zkPath, "test".getBytes(), -1);
                        end = System.nanoTime();
                    } else {
                        start = System.nanoTime();
                        zk.getData("/" + this.zkPath , false, null);
                        end = System.nanoTime();
                    }

                    updateStat(ops, (end - start) / 1000000.0);

                    if (!doneWarm && (System.nanoTime() > finalTimeWarm)) { // warmup timeout
                        System.out.println("Done Warmup [" + this.zkPath + "]" + this.durationSum / this.count + " ms");
                        resetStat();
                        doneWarm = true;
                    } 
                    if (System.nanoTime() > finalTime) { // duration timeout
                        System.out.println("Done Duration [" + this.zkPath + "] " + this.durationSum / this.count + " ms");
                        loglock.lock();
                        exportStat();
                        exportDuration();
                        loglock.unlock();
                        break;
                    }
                } catch (KeeperException|InterruptedException|IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void exportStat() throws IOException {
            FileWriter myWriter = new FileWriter(String.format("./outputs/exp-%d%%write-%ds%dw", 
                                                             this.percentWrite, this.numStrong, this.numWeak), true);
            String stat = String.format("Thread - %d , Done [%s] totalCount: %d totalTime: %f ms avgLat: %f ms " + 
                                        "writeCount: %d writeTime: %f ms avgLatWrite: %f ms " + 
                                        "readCount: %d readTime: %f ms avgLatRead: %f ms\n",
                                        this.threadIdx, this.zkPath, this.count, this.durationSum, this.durationSum / this.count,
                                        this.countWrite, this.durationSumWrite, this.durationSumWrite / this.countWrite,
                                        this.countRead, this.durationSumRead, this.durationSumRead / this.countRead);
            myWriter.write(stat);
            myWriter.flush();
            myWriter.close();
        }

        private void exportDuration() throws IOException {
            if (this.zkPath != "2") return; // only weak will be record

            BufferedWriter br = new BufferedWriter(new FileWriter(String.format("./outputs/exp-weak-cdf-%d%%write-%ds%dw.csv", 
                                                                              this.percentWrite, this.numStrong, this.numWeak), true));
            StringBuilder sb = new StringBuilder();

            // Append strings from array
            for (Double element : this.durationLogWrite) {
                sb.append(String.valueOf(element));
                sb.append(",");
            }
            br.write(sb.toString());
            br.close();
        }

        private void updateStat(String ops, double duration) {
            if (ops == "write") {
                this.countWrite += 1;
                this.durationSumWrite += duration;
                if (this.countWrite % 25 == 0 && this.zkPath == "2") { 
                    // sample to avoid too many data points
                    // here we only record weak write
                    this.durationLogWrite.add(duration);
                }
            } else if (ops == "read") {
                this.countRead += 1;
                this.durationSumRead += duration;
                // this.durationLogRead.add(duration);
            }
            this.count += 1;
            this.durationSum += duration;
        }

        private void resetStat() {
            this.durationLogWrite.clear();
            // this.durationLogRead.clear();
            this.count = 0;
            this.countRead = 0;
            this.countWrite = 0;
            this.durationSum = 0;
            this.durationSumWrite = 0;
            this.durationSumRead = 0;
        }

        private String getOps(Integer percentWrite) {
            Random rand = new Random(); 
            if (percentWrite > rand.nextInt(100)) {
                return "write";
            }
            return "read";
        }
    }
    public static void main(String[] args)  throws IOException, KeeperException, InterruptedException, ParseException {
        CommandLine cmd = argparse(args);
        String setting = String.format("host:%s write:%s%% warm:%s duration:%s numStrong:%s numWeak:%s", 
                                        cmd.getOptionValue("host"), cmd.getOptionValue("percentWrite"),
                                        cmd.getOptionValue("warmup"), cmd.getOptionValue("duration"),
                                        cmd.getOptionValue("numStrong"), cmd.getOptionValue("numWeak"));
        System.out.println(setting);

        ReentrantLock lock = new ReentrantLock(); // this is for writing stat to log
        // int num = Integer.parseInt(cmd.getOptionValue("numClient", "1"));
        int numStrong = Integer.parseInt(cmd.getOptionValue("numStrong", "0"));
        int numWeak = Integer.parseInt(cmd.getOptionValue("numWeak", "0"));
        String host = cmd.getOptionValue("host", "localhost:2182");

        for (int i = 0; i < numStrong ; i++) {
            System.out.println("starting running strong client - " + i);
            Runnable r = new MyRunnable(cmd, new ZooKeeper(host, 5000, null), i, lock, "1");
            new Thread(r).start();
        }

        for (int i = 0; i < numWeak ; i++) {
            System.out.println("starting running weak client - " + i);
            Runnable r = new MyRunnable(cmd, new ZooKeeper(host, 5000, null), i, lock, "2");
            new Thread(r).start();
        }
    }
}
