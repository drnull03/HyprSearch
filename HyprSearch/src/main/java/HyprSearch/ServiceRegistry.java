package HyprSearch;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServiceRegistry implements Watcher {

    private static final Logger logger = LoggerFactory.getLogger(ServiceRegistry.class);

    private static final String REGISTRY_ZNODE = "/service_registry";
    private static final String LEADER_REGISTRY_ZNODE = "/leader_registry"; // New Leader
    private final ZooKeeper zooKeeper;

    private String currentZnode = null;
    private List<String> allServiceAddresses = null;

    public ServiceRegistry(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
        createZnode(REGISTRY_ZNODE, CreateMode.PERSISTENT);
        createZnode(LEADER_REGISTRY_ZNODE, CreateMode.PERSISTENT);
    }

    private void createZnode(String path, CreateMode mode) {
        try {
            if (zooKeeper.exists(path, false) == null) {
                zooKeeper.create(path, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, mode);
            }
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    
    public void registerAsLeader(String metadata) throws KeeperException, InterruptedException {
        
        List<String> children = zooKeeper.getChildren(LEADER_REGISTRY_ZNODE, false);
        for (String child : children) {
            zooKeeper.delete(LEADER_REGISTRY_ZNODE + "/" + child, -1);
        }
        
        zooKeeper.create(LEADER_REGISTRY_ZNODE + "/leader", 
                metadata.getBytes(), 
                ZooDefs.Ids.OPEN_ACL_UNSAFE, 
                CreateMode.EPHEMERAL);
        logger.info("Registered as leader at: {}", metadata);
    }

    public void registerToCluster(String metadata) throws KeeperException, InterruptedException {
        if (this.currentZnode != null) return;
        this.currentZnode = zooKeeper.create(REGISTRY_ZNODE + "/n_", 
                metadata.getBytes(), 
                ZooDefs.Ids.OPEN_ACL_UNSAFE, 
                CreateMode.EPHEMERAL_SEQUENTIAL);
    }

    public void unregisterFromCluster() {
        try {
            if (currentZnode != null && zooKeeper.exists(currentZnode, false) != null) {
                zooKeeper.delete(currentZnode, -1);
                currentZnode = null;
            }
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void registerForUpdates() {
        try {
            updateAddresses();
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private synchronized void updateAddresses() throws KeeperException, InterruptedException {
        List<String> workerZnodes = zooKeeper.getChildren(REGISTRY_ZNODE, this);
        List<String> addresses = new ArrayList<>(workerZnodes.size());

        for (String workerZnode : workerZnodes) {
            String workerFullPath = REGISTRY_ZNODE + "/" + workerZnode;
            Stat stat = zooKeeper.exists(workerFullPath, false);
            if (stat == null) continue;

            byte[] addressBytes = zooKeeper.getData(workerFullPath, false, stat);
            addresses.add(new String(addressBytes));
        }

        this.allServiceAddresses = Collections.unmodifiableList(addresses);
    }

    public List<String> getWorkerZnodeNames() throws KeeperException, InterruptedException {
        return zooKeeper.getChildren(REGISTRY_ZNODE, false);
    }

    public String getMyZnodeName() {
        if (currentZnode == null) return null;
        return currentZnode.replace(REGISTRY_ZNODE + "/", "");
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        if (watchedEvent.getType() == Event.EventType.NodeChildrenChanged) {
            try {
                updateAddresses();
            } catch (KeeperException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public List<String> getAllServiceAddresses() {
        return allServiceAddresses == null ? List.of() : allServiceAddresses;
    }
}