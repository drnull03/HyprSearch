package com.ds.HyprSearch;

import org.apache.zookeeper.ZooKeeper;
import java.io.IOException;

public class ZKConnection {
    private ZooKeeper zoo;
    //host contains 2181 
    //this code is a simple wrapper it just connect to our backend
    public ZooKeeper connect(String host) throws IOException {
        zoo = new ZooKeeper(host, 3000, event -> {
            
        });
        return zoo;
    }
    // this function just close the tcp connections
    public void close() throws InterruptedException {
        if (zoo != null) {
            zoo.close();
        }
    }
}
