package HyprSearch;

import com.sun.net.httpserver.HttpServer;
import java.util.Collections;
import java.util.List;

public class OnElectionAction implements OnElectionCallback {

    private final ServiceRegistry serviceRegistry;
    private final int port;
    private HttpServer currentHttpServer; // Track the running server

    public OnElectionAction(ServiceRegistry serviceRegistry, int port) {
        this.serviceRegistry = serviceRegistry;
        this.port = port;
    }

    @Override
    public synchronized void onElectedToBeLeader() {
        try {
            System.out.println("I am the LEADER (Coordinator)");
            
            
            if (currentHttpServer != null) {
                System.out.println("Stopping current Worker server to transition to Leader...");
                currentHttpServer.stop(0); 
            }

            serviceRegistry.unregisterFromCluster();
            
            String coordinatorAddress = String.format("http://localhost:%d", port);
            serviceRegistry.registerAsLeader(coordinatorAddress);
            serviceRegistry.registerForUpdates();

            
            currentHttpServer = CoordinatorHttpServer.start(port, serviceRegistry);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void onWorker() {
        try {
            System.out.println("I am a WORKER");
            
            // In case of a re-election where we stay a worker, 
            // stop the old one to re-initialize the engine with new distribution
            if (currentHttpServer != null) {
                currentHttpServer.stop(0);
            }

            String currentServerAddress = String.format("http://localhost:%d", port);
            serviceRegistry.registerToCluster(currentServerAddress);

            List<String> workerZnodes = serviceRegistry.getWorkerZnodeNames(); 
            Collections.sort(workerZnodes);

            String myZnode = serviceRegistry.getMyZnodeName();
            int workerIndex = workerZnodes.indexOf(myZnode);
            int totalWorkers = workerZnodes.size();

            System.out.println(String.format("Worker distribution: Index %d of %d", 
                               workerIndex, totalWorkers));

            // Start Worker server
            currentHttpServer = WorkerHttpServer.start(port, "dataset", workerIndex, totalWorkers);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}