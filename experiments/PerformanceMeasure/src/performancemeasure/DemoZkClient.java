package performancemeasure;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class DemoZkClient {

    private static final String CONNECT_STRING = "localhost:2181,localhost:2182,localhost:2183";

    private static final int SESSION_TIMEOUT = 5000;

    public static void main(String[] args) throws Exception {
        // host_port, timeout, watcher
        ZooKeeper zk = new ZooKeeper(CONNECT_STRING, SESSION_TIMEOUT, null);

        /**
         * create znode (path, data, acl, createMode)
         */
        try {
            String create = zk.create("/1", "strong".getBytes(), Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT);
            System.out.println("create: " + create);
        } catch (Exception e) {
            System.out.println(e);
        }

        /**
         * check if znode exist
         */
        Stat exists = zk.exists("/1", null);
        if (exists == null) {
            System.out.println("/1 not exist");
        } else {
            System.out.println("/1 exists");
        }


        /**
         * get data in znode sync
         */
        byte[] data = zk.getData("/1", false, null);
        System.out.println(new String(data));


        /**
         * set data in znode
         */
        Stat setData = zk.setData("/1", "strong".getBytes(), -1);
        if (setData == null) {
            System.out.println("node not exist, set failed");
        } else {
            System.out.println("node exist, set succeeded");
        }


        /**
         * delete znode
         */

        zk.delete("/1", -1);



        // close connection
        zk.close();
    }

}
