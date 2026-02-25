package daintiness.clustering;

public class ClusteringHandlerFactory {
    public IClusteringHandler getClusteringHandler(String handlerType) {
        if (handlerType.isBlank()) {
            return null;
        }

        if (handlerType.equals("SIMPLE_CLUSTERING_HANDLER")) {
            return new ClusteringHandler();
        }

        return null;
    }
}
