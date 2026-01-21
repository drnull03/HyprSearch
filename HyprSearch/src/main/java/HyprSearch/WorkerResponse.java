package HyprSearch;

import java.io.Serializable;
import java.util.Map;


public record WorkerResponse(
    Map<String, Map<String, Double>> docToTermTf,
    Map<String, Integer> termToDocumentCount      
) implements Serializable {}