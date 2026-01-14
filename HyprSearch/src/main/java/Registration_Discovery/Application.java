package Registration_Discovery;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Application implements Watcher {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    private static final String address = "172.29.3.101:2181";
    private static final int SESSION_TIMEOUT = 3000; //dead client
    private static final int DEFAULT_PORT = 8080;
    private ZooKeeper zooKeeper;

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {

        int currentServerPort = args.length == 1 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        Application application = new Application();
        ZooKeeper zooKeeper = application.connectToZookeeper();

        logger.info("Connected");

        ServiceRegistry serviceRegistry = new ServiceRegistry(zooKeeper);

        OnElectionAction onElectionAction = new OnElectionAction(serviceRegistry, currentServerPort);

        LeaderElection leaderElection = new LeaderElection(zooKeeper, onElectionAction);
        leaderElection.volunteerForLeadership();
        leaderElection.reelectLeader();

        application.run();
        application.close();


    }

    public ZooKeeper connectToZookeeper() throws IOException {
        this.zooKeeper = new ZooKeeper(address, SESSION_TIMEOUT, this);
        return zooKeeper;
    }

    public void run() throws InterruptedException {
        synchronized (zooKeeper) {
            zooKeeper.wait();
        }
    }

    private void close() throws InterruptedException {
        this.zooKeeper.close();
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        switch (watchedEvent.getType()) {
            case None:
                if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
//                    System.out.println("Successfully connected to Zookeeper");
                    logger.debug("Successfully connected to Zookeeper");
                } else if (watchedEvent.getState() == Event.KeeperState.Disconnected) {
                    synchronized (zooKeeper) {
//                        System.out.println("Disconnected from Zookeeper");
                        logger.debug("Disconnected from Zookeeper");
                        zooKeeper.notifyAll();
                    }
                } else if (watchedEvent.getState() == Event.KeeperState.Closed) {
//                    System.out.println("Closed Successfully");
                    logger.debug("Closed Successfully");
                }
                break;
        }
    }
}
