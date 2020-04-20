package performancemeasure;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;

public class MeasureSetDataStrongWeak {
    static ZooKeeper zk;

    static Runnable measureStrong = () -> {
        double durationSum = 0;
        int count = 0;

        while (true) {
            try {
                long start = System.nanoTime();
                zk.setData("/1", "strong".getBytes(), -1);
                long end = System.nanoTime();
                // System.out.println("[STRONG] " + (end - start) / 1000000.0 + " ms");
                ++count;
                durationSum += (end - start) / 1000000.0;

                // print avg
                if (count % 1000 == 0) {
                    System.out.println("[STRONG] " + durationSum / count + " ms");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    static Runnable measureWeak = () -> {
        double durationSum = 0;
        int count = 0;

        while (true) {
            try {
                long start = System.nanoTime();
                zk.setData("/2", "weak".getBytes(), -1);
                long end = System.nanoTime();
                // System.out.println("[ WEAK ] " + (end - start) / 1000000.0 + " ms");
                ++count;
                durationSum += (end - start) / 1000000.0;

                // print avg
                if (count % 1000 == 0) {
                    System.out.println("[ WEAK ] " + durationSum / count + " ms");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public static void main(String[] args)
            throws IOException, KeeperException, InterruptedException {
        zk = new ZooKeeper("localhost:2182", 5000, null);
//        zk.create("/1", "strong".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
//        zk.create("/2", "weak".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

//        Thread measureStrongThread1 = new Thread(measureStrong);
//        Thread measureStrongThread2 = new Thread(measureStrong);
        Thread measureWeakThread1 = new Thread(measureWeak);
        Thread measureWeakThread2 = new Thread(measureWeak);

//        measureStrongThread1.start();
//        measureStrongThread2.start();
        measureWeakThread1.start();
        measureWeakThread2.start();
    }

    /**
     * This method is just a demo of how to use Async version of setData, by providing a callback
     * with lambda, and also use CoundDownLatch to block to prevent the method from returning.
     * 
     * @throws InterruptedException
     */
    @SuppressWarnings("unused")
    private static void demoAsync() throws InterruptedException {
        // used to block the method from returning
        // the only argument 1 means calling countDown() once will cause await() to
        // return.
        CountDownLatch demoAsyncSignal = new CountDownLatch(1);

        // here demo how to pass argument to callback function
        long start = System.nanoTime();

        zk.setData("/1", "strong".getBytes(), -1, (int rc, String path, Object ctx, Stat stat) -> {
            long innerEnd = System.nanoTime();

            // ctx is just the passed in start
            long innerStart = (long) ctx;
            System.out.println("setData spent: " + (innerEnd - innerStart) / 1000000.0 + " ms");

            // tell await() to return
            demoAsyncSignal.countDown();
        }, start);

        // block until countDown() is called
        demoAsyncSignal.await();
    }

}
