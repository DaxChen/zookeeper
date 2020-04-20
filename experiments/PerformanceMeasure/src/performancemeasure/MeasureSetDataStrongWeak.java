package performancemeasure;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class MeasureSetDataStrongWeak {

    static class MyRunnable implements Runnable {
        ZooKeeper zk;
        String path;
        public MyRunnable(String path, ZooKeeper zk) {
            this.path = path;
            this.zk = zk;
        }

        public void run() {
            double durationSum = 0;
            int count = 0;

            while (true) {
                try {
                    long start = System.nanoTime();
                    zk.setData("/" + path, "test".getBytes(), -1);
                    long end = System.nanoTime();

                    ++count;
                    durationSum += (end - start) / 1000000.0;

                    // print avg
                    if (count % 1000 == 0) {
                        System.out.println("[" + path + "] " + durationSum / count + " ms");
                    }
                } catch (KeeperException|InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args)
            throws IOException, KeeperException, InterruptedException {

        for (String path : args) {
            System.out.println("starting thread for path: " + path);
            Runnable r = new MyRunnable(path, new ZooKeeper("localhost:2182", 5000, null));
            new Thread(r).start();
        }
    }

    /**
     * This method is just a demo of how to use Async version of setData, by providing a callback
     * with lambda, and also use CoundDownLatch to block to prevent the method from returning.
     *
     * @throws InterruptedException
     */
    // @SuppressWarnings("unused")
    // private static void demoAsync() throws InterruptedException {
    //     // used to block the method from returning
    //     // the only argument 1 means calling countDown() once will cause await() to
    //     // return.
    //     CountDownLatch demoAsyncSignal = new CountDownLatch(1);
    //
    //     // here demo how to pass argument to callback function
    //     long start = System.nanoTime();
    //
    //     zk.setData("/1", "strong".getBytes(), -1, (int rc, String path, Object ctx, Stat stat) -> {
    //         long innerEnd = System.nanoTime();
    //
    //         // ctx is just the passed in start
    //         long innerStart = (long) ctx;
    //         System.out.println("setData spent: " + (innerEnd - innerStart) / 1000000.0 + " ms");
    //
    //         // tell await() to return
    //         demoAsyncSignal.countDown();
    //     }, start);
    //
    //     // block until countDown() is called
    //     demoAsyncSignal.await();
    // }

}
