package HyprSearch;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class TfIdfEngine {

    private final Map<String, String> documents = new HashMap<>();

    public TfIdfEngine(String datasetPath, int workerIndex, int totalWorkers) throws IOException {
        Path path = Paths.get(datasetPath);

        if (!Files.exists(path) || !Files.isDirectory(path)) {
            throw new IOException("Dataset path invalid: " + datasetPath);
        }

        List<Path> allFiles = Files.list(path)
                .filter(Files::isRegularFile)   
                .sorted()
                .toList();

        if (allFiles.isEmpty()) {
            System.out.println("No files found in dataset directory: " + datasetPath);
        }

        for (int i = 0; i < allFiles.size(); i++) {
            if (i % totalWorkers == workerIndex) {
                Path file = allFiles.get(i);

                try {
                    // try reading file as UTF-8 text
                    String content = Files.readString(file);
                    documents.put(file.getFileName().toString(), content);

                } catch (IOException e) {
                    // ignore non-text/binary files
                    System.err.println("Skipping non-text or unreadable file: " + file.getFileName());
                }
            }
        }

        System.out.println("Loaded documents count: " + documents.size());
    }

    public WorkerResponse calculateStatistics(String query) {
        String[] terms = query.toLowerCase().split("\\s+");
        Map<String, Map<String, Double>> docToTermTf = new HashMap<>();
        Map<String, Integer> termToDocumentCount = new HashMap<>();

        for (String term : terms) {
            int globalTermCount = 0;

            for (Map.Entry<String, String> entry : documents.entrySet()) {
                String[] words = entry.getValue().toLowerCase().split("\\s+");
                long count = Arrays.stream(words).filter(w -> w.equals(term)).count();

                if (count > 0) {
                    globalTermCount++;
                    double tf = (double) count / words.length;
                    docToTermTf.computeIfAbsent(entry.getKey(), k -> new HashMap<>()).put(term, tf);
                }
            }

            termToDocumentCount.put(term, globalTermCount);
        }

        return new WorkerResponse(docToTermTf, termToDocumentCount);
    }

    public int getDocumentCount() {
        return documents.size();
    }
}
