package daintiness.clustering;

/**
 * EntityClusteringProfile class contains the necessary parameters for the entity clustering
 */
public class EntityClusteringProfile {
    private final int desiredNumberOfEntityGroups;
    private final double birthWeight;
    private final double deathWeight;
    private final double changesWeight;

    /**
     * @param desiredNumberOfEntityGroups desired number of entityGroups
     * @param birthWeight weight of birth date
     * @param deathWeight weight of death data
     * @param changesWeight weight of changes
     */
    public EntityClusteringProfile(int desiredNumberOfEntityGroups,
                                   double birthWeight,
                                   double deathWeight,
                                   double changesWeight) {
        this.desiredNumberOfEntityGroups = desiredNumberOfEntityGroups;
        this.birthWeight = birthWeight;
        this.deathWeight = deathWeight;
        this.changesWeight = changesWeight;
    }

    /**
     * The birthWeight and the deathWeight are set to 0.25 and
     * the changesWeight is set to 0.5
     * @param desiredNumberOfEntityGroups desired number of entityGroups
     */
    public EntityClusteringProfile(int desiredNumberOfEntityGroups) {
        this.desiredNumberOfEntityGroups = desiredNumberOfEntityGroups;
        this.birthWeight = 0.25;
        this.deathWeight = 0.25;
        this.changesWeight = 0.5;
    }

    /**
     * @return desired number of entityGroups
     */
    public int getDesiredNumberOfEntityGroups() {
        return desiredNumberOfEntityGroups;
    }

    /**
     * @return birth date weight
     */
    public double getBirthWeight() {
        return birthWeight;
    }

    /**
     * @return death date weight
     */
    public double getDeathWeight() {
        return deathWeight;
    }

    /**
     * @return changes weight
     */
    public double getChangesWeight() {
        return changesWeight;
    }
}
