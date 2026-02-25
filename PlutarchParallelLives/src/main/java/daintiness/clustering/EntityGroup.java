package daintiness.clustering;

import java.util.ArrayList;
import java.util.List;

import daintiness.models.Entity;
import daintiness.models.LifeDetails;
import daintiness.utilities.Constants;


public class EntityGroup {
    private int entityGroupId;
    private List<Entity> groupComponents;
    private final List<String> groupComponentsNames;

    private LifeDetails lifeDetails;


    public EntityGroup(int entityGroupId, Entity firstEntity) {
        this.entityGroupId = entityGroupId;
        this.groupComponents = new ArrayList<>();
        groupComponents.add(firstEntity);
        this.groupComponentsNames = new ArrayList<>();
        groupComponentsNames.add(firstEntity.getEntityName());

        lifeDetails = firstEntity.getLifeDetails();
    }

    public EntityGroup(int entityGroupId, List<String> groupComponentsNames, LifeDetails lifeDetails) {
        this.entityGroupId = entityGroupId;
        this.groupComponentsNames = groupComponentsNames;
        this.lifeDetails = lifeDetails;
    }


    public int getEntityGroupId() {
        return entityGroupId;
    }

    public void setEntityGroupId(int entityGroupId) {
        this.entityGroupId = entityGroupId;
    }

    public List<Entity> getGroupComponents() {
        return groupComponents;
    }

    public List<String> getGroupComponentsNames() {
        return groupComponentsNames;
    }

    public LifeDetails getLifeDetails() {
        return lifeDetails;
    }

    public void addEntityInGroup(Entity newEntity) {
        groupComponents.add(newEntity);
        groupComponentsNames.add(newEntity.getEntityName());
        updateGroupLifeDetails(newEntity.getLifeDetails());
    }


    @SuppressWarnings("unused")
	private void updateGroupLifeDetails(LifeDetails newLifeDetails) {
        int birth = lifeDetails.getBirthBeatId();
        int death = lifeDetails.getDeathBeatId();
        boolean isAlive = lifeDetails.isAlive();
        int duration = lifeDetails.getDuration();

        if (birth > newLifeDetails.getBirthBeatId()) {
            birth = newLifeDetails.getBirthBeatId();
        }

        if (death < newLifeDetails.getDeathBeatId()) {
            death = newLifeDetails.getDeathBeatId();
        }


        if (newLifeDetails.isAlive()) {
            isAlive = true;
        }

        lifeDetails = new LifeDetails(birth, death, isAlive, death - birth + 1);
    }


    private boolean isAliveInPhase(int phaseLastBeatId) {
        int birth = this.getLifeDetails().getBirthBeatId();
        int death = this.getLifeDetails().getDeathBeatId();
        boolean isAlive = this.getLifeDetails().isAlive();


        if (birth < phaseLastBeatId) {
            if (isAlive) {
                return true;
            } else {
                return death > phaseLastBeatId;
            }
        }
        return false;
    }

    public void mergeWithEntityGroup(EntityGroup newEntityGroup) {
        for (Entity entity: newEntityGroup.getGroupComponents()) {
            addEntityInGroup(entity);
        }
    }

    public Constants.GPMType getGPMType(int phaseFirstBeatId, int phaseLastBeatId) {

        if (phaseContainsBeat(phaseFirstBeatId, phaseLastBeatId, lifeDetails.getBirthBeatId())) {
            return Constants.GPMType.BIRTH;
        } else {
            if (!lifeDetails.isAlive() && phaseContainsBeat(phaseFirstBeatId, phaseLastBeatId, lifeDetails.getDeathBeatId())) {
                return Constants.GPMType.DEATH;
            }
        }
        if (isAliveInPhase(phaseLastBeatId)) {
            return Constants.GPMType.ACTIVE;
        } else {
            return Constants.GPMType.INACTIVE;
        }
    }

    private boolean phaseContainsBeat(int phaseFirstBeatId, int phaseLastBeatId, int targetBeat) {
        return (phaseFirstBeatId < targetBeat && phaseLastBeatId > targetBeat) ||
                (phaseFirstBeatId == targetBeat) || (phaseLastBeatId == targetBeat);
    }
}
