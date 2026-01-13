package com.ds.HyprSearch;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.List;

public class LeaderElection implements Watcher {
    private ZooKeeper zooKeeper;
    private String currentNode;
    private static final String ELECTION_NODE = "/election";

    public LeaderElection(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
    }

    public void volunteerForLeadership() throws Exception {
        // first time check
        Stat stat = zooKeeper.exists(ELECTION_NODE, false);
        if (stat == null) {
            zooKeeper.create(ELECTION_NODE, new byte[] {}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT); // this
                                                                                                                // is
                                                                                                                // presistent
        }

        // Create ephemeral sequential node
        currentNode = zooKeeper.create(ELECTION_NODE + "/node_", new byte[] {},
                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println("Node created: " + currentNode);
    }

    public void electLeader() throws Exception {
        while (true) {
            List<String> children = zooKeeper.getChildren(ELECTION_NODE, false);
            children.sort(String::compareTo);

            String smallestNode = children.get(0);
            String currentNodeName = currentNode.substring(currentNode.lastIndexOf("/") + 1);

            if (currentNodeName.equals(smallestNode)) {
                System.out.println(currentNode + " is the leader THIS IS MEEEE :) ");
                // Keep leader alive (e.g. 60 seconds)
                synchronized (this) {
                    wait();
                }
            } else {
                System.out.println(currentNode + "I am not the leader so sad :(");
                // Find the node immediately before current node
                int index = children.indexOf(currentNodeName) - 1;
                String watchNode = children.get(index);

                final Object lock = new Object();

                // Watch the previous node
                zooKeeper.exists(ELECTION_NODE + "/" + watchNode, event -> {
                    if (event.getType() == Event.EventType.NodeDeleted) {
                        synchronized (lock) {
                            lock.notifyAll();
                        }
                    }
                });

                // Wait until the previous node is deleted
                synchronized (lock) {
                    lock.wait();
                }
            }
        }
    }

    @Override
    public void process(WatchedEvent event) {

    }
}
