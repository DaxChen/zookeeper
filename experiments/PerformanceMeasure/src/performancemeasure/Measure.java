package performancemeasure;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;

public class Measure {

    static class MyRunnable implements Runnable {
        ZooKeeper zk;
        String path;
        
        public MyRunnable(String path, ZooKeeper zk) {
            this.path = path;
            this.zk = zk;
        }
        
        // 2 min warmup + 3 min measurement
        public void run() {
            long warmupStart = System.nanoTime();
            double durationSum = 0;
            int count = 0;
            
            while ((System.nanoTime() - warmupStart) / 60000 <= 2) {
            	try {
            		long start = System.nanoTime();
                zk.setData("/" + path, "test".getBytes(), -1);
                long end = System.nanoTime();
                
                ++count;
                durationSum += (end - start) / 1000000.0;
                if (count % 1000 == 0) {
                    System.out.println("warmup: [" + path + "] " + durationSum / count + " ms");
                }
                
	            } catch (KeeperException|InterruptedException e) {
	                e.printStackTrace();
	            }
            }
           
            durationSum = 0;
            count = 0;
            while (durationSum / 60000 <= 3) {
                try {
                    long start = System.nanoTime();
                    zk.setData("/" + path, "test".getBytes(), -1);
                    long end = System.nanoTime();

                    ++count;
                    durationSum += (end - start) / 1000000.0;

                    // print avg
                    if (count % 1000 == 0) {
                        System.out.println("measure: [" + path + "] " + durationSum / count + " ms");
                    }
                } catch (KeeperException|InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args)
            throws IOException, KeeperException, InterruptedException {
    		ZooKeeper zk = new ZooKeeper("10.10.1.2:2181", 5000, null);
    		zk.create("/1", "strong".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
      	zk.create("/2", "weak".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
      	zk.create("/3", "midweak".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    		
        for (String path : args) {
            System.out.println("starting thread for path: " + path);
            //"localhost:2183" "10.10.1.2:2183"
            Runnable r = new MyRunnable(path, new ZooKeeper("10.10.1.2:2181", 5000, null));
            new Thread(r).start();
        }
    }
}