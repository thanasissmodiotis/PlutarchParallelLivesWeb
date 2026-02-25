package daintiness.models;


public class Entity{
    private final int entityId;
    private final String entityName;
    private final LifeDetails lifeDetails;

    public Entity(int entityId, String entityName, LifeDetails lifeDetails) {
        this.entityId = entityId;
        this.entityName = entityName;
        this.lifeDetails = lifeDetails;
    }

    public int getEntityId() {
        return entityId;
    }

    public String getEntityName() {
        return entityName;
    }

    public LifeDetails getLifeDetails() {
        return lifeDetails;
    }

    @Override
    public String toString() {
        return  "id: " + entityId +
                ", name: " + entityName +
                ", birth: " + lifeDetails.getBirthBeatId() +
                ", death: " + lifeDetails.getDeathBeatId() +
                ", duration: " + lifeDetails.getDuration();
    }
}
