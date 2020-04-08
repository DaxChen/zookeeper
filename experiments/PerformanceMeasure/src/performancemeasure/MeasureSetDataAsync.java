package performancemeasure;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class MeasureSetDataAsync {


    static ZooKeeper zk;

    public static void main(String[] args) throws IOException, InterruptedException {
        zk = new ZooKeeper("localhost:2182", 5000, null);

        double strongSum = 0;
        double weakSum = 0;
        int count = 0;

        while (true) {
            double[] result = measureStrongWeakAsync();
            count++;
            strongSum += result[0];
            weakSum += result[1];
            if (count % 1000 == 0)
                System.out.printf("STRONG: %.3f ms, WEAK: %.3f ms\n", strongSum / count,
                        weakSum / count);
        }

    }

    /**
     * 1. strong call async
     * 
     * 2. weak call async
     * 
     * 3. wait for both done
     * 
     * 4. record both time
     * 
     * @throws InterruptedException
     */
    private static double[] measureStrongWeakAsync() throws InterruptedException {
        // used to block the method from returning
        // the only argument 2 means calling countDown() twice will cause await() to
        // return.
        CountDownLatch signal = new CountDownLatch(2);

        // pass argument to callback function
        class Context {
            long strongStart;
            long strongEnd;
            long weakStart;
            long weakEnd;
        }
        Context context = new Context();

        // call strong async
        context.strongStart = System.nanoTime();
        zk.setData("/1", "strong".getBytes(), -1, (int rc, String path, Object ctx, Stat stat) -> {
            Context c = (Context) ctx;
            c.strongEnd = System.nanoTime();

            signal.countDown();
        }, context);


        // call weak async
        context.weakStart = System.nanoTime();
        zk.setData("/2", "weak".getBytes(), -1, (int rc, String path, Object ctx, Stat stat) -> {
            Context c = (Context) ctx;
            c.weakEnd = System.nanoTime();

            signal.countDown();
        }, context);

        // block until countDown() is called twice
        signal.await();

        return new double[] {(context.strongEnd - context.strongStart) / 1000000.0,
                (context.weakEnd - context.weakStart) / 1000000.0};
    }

}
