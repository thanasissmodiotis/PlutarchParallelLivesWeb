package daintiness.clustering;

/**
 * BeatClusteringProfile class contains the necessary parameters for the beat clustering
 */
public class BeatClusteringProfile {
//  PHASE EXTRACTOR PROFILE
    private final int desiredNumberOfPhases;
    private final double changesWeight;
    private final boolean withPreprocessing;

    /**
     * @param desiredNumberOfPhases number of desired phases
     * @param changesWeight changes weight
     * @param withPreprocessing boolean
     */
    public BeatClusteringProfile(int desiredNumberOfPhases, double changesWeight, boolean withPreprocessing) {
        this.desiredNumberOfPhases = desiredNumberOfPhases;
        this.changesWeight = changesWeight;
        this.withPreprocessing = withPreprocessing;
    }

    /**
     * The weights are set to 0.5
     * @param desiredNumberOfPhases number of desired phases
     */
    public BeatClusteringProfile(int desiredNumberOfPhases) {
        this.desiredNumberOfPhases = desiredNumberOfPhases;
        this.changesWeight = 0.5f;
        this.withPreprocessing = false;
    }

    /**
     * @return the desired number of phases
     */
    public int getDesiredNumberOfPhases() {
        return desiredNumberOfPhases;
    }

    /**
     * @return the weight of time in the distance calculation
     */
    public double getTimeWeight() {
        return 1 - changesWeight;
    }

    /**
     * @return the weight of changes in the distance calculation
     */
    public double getChangesWeight() {
        return changesWeight;
    }

    /**
     * @return True if preprocessing is enabled.
     */
    public boolean isWithPreprocessing() {
        return withPreprocessing;
    }
}
