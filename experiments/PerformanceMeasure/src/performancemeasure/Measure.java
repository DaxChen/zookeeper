package performancemeasure;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;

public class Measure {

	static class MyRunnable implements Runnable {
  	double avgLatency = 0;
    ZooKeeper zk;
    String path;
    
    public MyRunnable(String path, ZooKeeper zk) {
        this.path = path;
        this.zk = zk;
    }
    
    public void run() {
    	// 2 min warmup
    	double durationSum = 0;
      int count = 0;
      long warmupStart = System.nanoTime();
      while ((System.nanoTime() - warmupStart) / 60000 <= 2000000) {
      	try {
      		long start = System.nanoTime();
          zk.setData("/" + path, "test".getBytes(), -1);
          long end = System.nanoTime();
          
          ++count;
          durationSum += (end - start) / 1000000.0;
        } catch (KeeperException|InterruptedException e) {
            e.printStackTrace();
        }
      }
      System.out.println("warmup: [" + path + "] " + durationSum / count + " ms");
      
      // 3 min measurement
      durationSum = 0;
      count = 0;
      long measureStart = System.nanoTime();
      while ((System.nanoTime() - measureStart) / 60000 <= 3000000) {
        try {
          long start = System.nanoTime();
          zk.setData("/" + path, "test".getBytes(), -1);
          long end = System.nanoTime();

          ++count;
          durationSum += (end - start) / 1000000.0;
        } catch (KeeperException|InterruptedException e) {
            e.printStackTrace();
        }
      }
      System.out.println("measure: [" + path + "] " + durationSum / count + " ms");
      
      avgLatency = durationSum / count;
    }
  }

  public static void main(String[] args) 
  		throws IOException, KeeperException, InterruptedException {
//		ZooKeeper zk = new ZooKeeper("localhost:2183", 5000, null);
//		zk.create("/1", "strong0".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
//  	zk.create("/2", "weak000".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
//  	zk.create("/3", "midweak".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    
  	String hostPort = "10.10.1.2:2181"; // "10.10.1.2:2181" // "localhost:2181"
  	
  	// strong vs. midweak paths 6s, 5s1w, ..., 1s5w, 6w
		String weakLevel = "3";
   	for (int numWeak = 0; numWeak <= 6; numWeak++) {
   		StringBuilder paths = new StringBuilder();
   		for (int num = 0; num < numWeak; num++) {
   			paths.append(weakLevel);
   		}
   		for (int num = numWeak; num < 6; num++) {
   			paths.append("1");
   		}
   		
   		exp(paths.toString(), hostPort);
   	}
  }
  
  public static void exp(String paths, String hostPort) 
  		throws IOException, KeeperException, InterruptedException {
  	
  	System.out.println("init runnabls");
  	String[] p = paths.split("");
  	MyRunnable[] runnables = new MyRunnable[6];
  	for (int i = 0; i < 6; i++) {
	    System.out.println("starting thread for path: " + p[i]);
	    runnables[i] = new MyRunnable(p[i], new ZooKeeper(hostPort, 5000, null));
  	}
  	
  	System.out.println("init threads and run");
  	Thread[] myThreads = new Thread[6];
  	for (int i = 0; i < 6; i++) {
  		myThreads[i] = new Thread(runnables[i]);
  		myThreads[i].start();
  	}
  	
  	for (int i = 0; i < 6; i++) {
  		myThreads[i].join();
  	}
  	
  	System.out.println("join threads and save file");
  	FileWriter f = new FileWriter(paths + ".txt");
  	for (int i = 0; i < 6; i++) {
  		double latency = runnables[i].avgLatency;
  		String s = p[i] + " " + latency + "ms\n";
  		f.write(s);
  	}
  	
  	f.close();
  }
}