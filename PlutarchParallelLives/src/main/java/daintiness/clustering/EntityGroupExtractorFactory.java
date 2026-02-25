package daintiness.clustering;

import daintiness.data.IDataHandler;

public class EntityGroupExtractorFactory {
	public IEntityGroupExtractor getPhaseExtractor(
			String type, 
			EntityClusteringProfile profile, 
			IDataHandler dataHandler) {
		
		if (type.isBlank()) {
            return null;
        }

        if (type.equals("AGGLOMERATIVE")) {
            return new EntityGroupExtractor(profile, dataHandler);
        }

        return null;
	}
}
