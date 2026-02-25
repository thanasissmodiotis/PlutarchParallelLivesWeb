package daintiness.clustering;

import daintiness.data.IDataHandler;

public class PhaseExtractorFactory {
	public IPhaseExtractor getPhaseExtractor(
			String type, 
			BeatClusteringProfile profile, 
			IDataHandler dataHandler) {
		
		if (type.isBlank()) {
            return null;
        }

        if (type.equals("AGGLOMERATIVE")) {
            return new AgglomerativePhaseExtractor(profile, dataHandler);
        }

        return null;
	}
}
