package HyprSearch;

import org.apache.zookeeper.KeeperException;
import java.util.Collections;
import java.util.List;

public class OnElectionAction implements OnElectionCallback {

    private final ServiceRegistry serviceRegistry;
    private final int port;

    public OnElectionAction(ServiceRegistry serviceRegistry, int port) {
        this.serviceRegistry = serviceRegistry;
        this.port = port;
    }

    @Override
    public void onElectedToBeLeader() {
        try {
            System.out.println("I am the LEADER (Coordinator)");

            
            serviceRegistry.unregisterFromCluster();
            
           
            String coordinatorAddress = String.format("http://localhost:%d", port);
            serviceRegistry.registerAsLeader(coordinatorAddress);

           
            serviceRegistry.registerForUpdates();
            CoordinatorHttpServer.start(port, serviceRegistry);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWorker() {
        try {
            System.out.println("I am a WORKER");
            String currentServerAddress = String.format("http://localhost:%d", port);
            serviceRegistry.registerToCluster(currentServerAddress);

            List<String> workerZnodes = serviceRegistry.getWorkerZnodeNames(); 
            Collections.sort(workerZnodes);

            String myZnode = serviceRegistry.getMyZnodeName();
            int workerIndex = workerZnodes.indexOf(myZnode);
            int totalWorkers = workerZnodes.size();

            System.out.println(String.format("Worker distribution: Index %d of %d", 
                               workerIndex, totalWorkers));

            WorkerHttpServer.start(port, "dataset", workerIndex, totalWorkers);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}