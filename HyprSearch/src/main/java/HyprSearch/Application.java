package HyprSearch;

import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class Application implements Watcher {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    private static final String ZK_ADDRESS = "localhost:2181"; // IMPORTANT
    private static final int SESSION_TIMEOUT = 3000;
    private static final int DEFAULT_PORT = 8080;

    private ZooKeeper zooKeeper;
    private final CountDownLatch connectedSignal = new CountDownLatch(1);

    public static void main(String[] args) throws Exception {

        int currentServerPort =
                args.length == 1 ? Integer.parseInt(args[0]) : DEFAULT_PORT;

        Application application = new Application();
        ZooKeeper zooKeeper = application.connectToZookeeper();

        
        application.connectedSignal.await();
        logger.info("ZooKeeper connected");

        ServiceRegistry serviceRegistry = new ServiceRegistry(zooKeeper);
        OnElectionAction onElectionAction =
                new OnElectionAction(serviceRegistry, currentServerPort);

        LeaderElection leaderElection =
                new LeaderElection(zooKeeper, onElectionAction);

        leaderElection.volunteerForLeadership();
        leaderElection.reelectLeader();

        application.run();
        application.close();
    }

    public ZooKeeper connectToZookeeper() throws IOException {
        this.zooKeeper = new ZooKeeper(ZK_ADDRESS, SESSION_TIMEOUT, this);
        return zooKeeper;
    }

    public void run() throws InterruptedException {
        synchronized (zooKeeper) {
            zooKeeper.wait();
        }
    }

    private void close() throws InterruptedException {
        zooKeeper.close();
    }

    @Override
    public void process(WatchedEvent event) {
        if (event.getState() == Event.KeeperState.SyncConnected) {
            connectedSignal.countDown();
        }

        if (event.getState() == Event.KeeperState.Disconnected ||
            event.getState() == Event.KeeperState.Expired) {

            synchronized (zooKeeper) {
                zooKeeper.notifyAll();
            }
        }
    }
}
