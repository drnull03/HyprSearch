package com.ds.LeaderElection;

public class App {

    public static void main(String[] args) throws Exception {

        
        ZKConnection zkConnection = new ZKConnection();
        var zk = zkConnection.connect("localhost:2181");  
        // Create a leader election participant
        LeaderElection election = new LeaderElection(zk);

        // Volunteer for leadership (creates ephemeral sequential node)
        election.volunteerForLeadership();

        // Begin election process
        election.electLeader();

        


        // might figure out something better 
        zkConnection.close();
    }
}
