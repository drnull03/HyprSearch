package HyprSearch;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.io.*;

public class WorkerHttpServer {

    public static HttpServer start(int port, String datasetPath, int workerIndex, int totalWorkers) throws Exception {
        System.out.println("Starting Worker HTTP Server on port " + port);
        TfIdfEngine engine = new TfIdfEngine(datasetPath, workerIndex, totalWorkers);

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/search", exchange -> {
            try {
                if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                    exchange.sendResponseHeaders(405, -1);
                    return;
                }
                String query = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                WorkerResponse stats = engine.calculateStatistics(query);

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(stats);
                oos.writeInt(engine.getDocumentCount());
                oos.flush();

                byte[] bytes = bos.toByteArray();
                exchange.getResponseHeaders().set("Content-Type", "application/octet-stream");
                exchange.sendResponseHeaders(200, bytes.length);

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
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
        System.out.println("Worker HTTP Server started on port " + port);
        return server; // Return the instance
    }
}