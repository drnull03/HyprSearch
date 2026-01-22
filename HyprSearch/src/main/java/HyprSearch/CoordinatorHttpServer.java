package HyprSearch;

import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class CoordinatorHttpServer {

    public static HttpServer start(int port, ServiceRegistry registry) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/search", exchange -> {
            try {
                if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                    exchange.sendResponseHeaders(405, -1);
                    return;
                }

                InputStream is = exchange.getRequestBody();
                String query = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                is.close();

                List<String> workers = registry.getAllServiceAddresses();
                Map<String, Map<String, Double>> globalDocTf = new HashMap<>();
                Map<String, Integer> globalTermDf = new HashMap<>();
                int totalDocsInCluster = 0;

                for (String worker : workers) {
                    try {
                        String targetUrl = worker.endsWith("/") ? worker + "search" : worker + "/search";
                        byte[] responseBytes = HttpClientUtil.postRaw(targetUrl, query);
                        
                        if (responseBytes != null && responseBytes.length > 0) {
                            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(responseBytes));
                            WorkerResponse workerData = (WorkerResponse) ois.readObject();
                            int workerDocCount = ois.readInt();
                            totalDocsInCluster += workerDocCount;
                            globalDocTf.putAll(workerData.docToTermTf());
                            workerData.termToDocumentCount().forEach((term, count) -> 
                                globalTermDf.merge(term, count, Integer::sum));
                        }
                    } catch (Exception e) {
                        System.err.println("Error calling worker " + worker + ": " + e.getMessage());
                    }
                }

                final int N = totalDocsInCluster;
                List<SearchResult> finalResults = new ArrayList<>();
                for (var entry : globalDocTf.entrySet()) {
                    String docName = entry.getKey();
                    Map<String, Double> termTfs = entry.getValue();
                    double totalScore = 0.0;
                    for (var tfEntry : termTfs.entrySet()) {
                        String term = tfEntry.getKey();
                        if (globalTermDf.containsKey(term) && globalTermDf.get(term) > 0) {
                            double tf = tfEntry.getValue();
                            double idf = Math.log((double) N / globalTermDf.get(term));
                            totalScore += tf * idf;
                        }
                    }
                    finalResults.add(new SearchResult(docName, totalScore));
                }

                String jsonResponse = finalResults.stream()
                    .sorted((a, b) -> Double.compare(b.score(), a.score()))
                    .limit(10)
                    .map(r -> String.format("{\"doc\":\"%s\", \"score\":%f}", r.doc(), r.score()))
                    .collect(Collectors.joining(",", "[", "]"));

                byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
            } catch (Exception e) {
                e.printStackTrace();
                exchange.sendResponseHeaders(500, -1);
            } finally {
                exchange.close();
            }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("Coordinator HTTP server started on port " + port);
        return server; // Return the instance
    }
}