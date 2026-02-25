package daintiness.clustering;

/**
 * This class contains the clustering parameters
 * for the BeatClustering and the EntityClustering.
 */
public class ClusteringProfile {
    private final BeatClusteringProfile beatClusteringProfile;
    private final EntityClusteringProfile entityClusteringProfile;

    public ClusteringProfile(BeatClusteringProfile beatClusteringProfile, EntityClusteringProfile entityClusteringProfile) {
        this.beatClusteringProfile = beatClusteringProfile;
        this.entityClusteringProfile = entityClusteringProfile;
    }

    public BeatClusteringProfile getBeatClusteringProfile() {
        return beatClusteringProfile;
    }

    public EntityClusteringProfile getEntityClusteringProfile() {
        return entityClusteringProfile;
    }
}
