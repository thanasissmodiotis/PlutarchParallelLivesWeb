package daintiness.clustering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import daintiness.data.IDataHandler;
import daintiness.models.Entity;
import daintiness.models.TimeEntityMeasurements;
import daintiness.utilities.Constants;

public class EntityGroupExtractor implements IEntityGroupExtractor{
    private Constants.MeasurementType measurementType = Constants.MeasurementType.RAW_VALUE;
    private Constants.AggregationType aggregationType = Constants.AggregationType.SUM_OF_ALL;
    private final EntityClusteringProfile profile;

    private final IDataHandler dataHandler;

    private List<EntityGroup> entityGroupList;
    private Map<Integer, Map<Integer, Double>> entityGroupToBeatValueMap;

    public EntityGroupExtractor(EntityClusteringProfile profile, IDataHandler dataHandler) {
        this.profile = profile;
        this.dataHandler = dataHandler;

        if (dataHandler.getType() != Constants.FileType.SCHEMA_EVO) {
            aggregationType = Constants.AggregationType.NO_AGGREGATION;
        }
    }

    public EntityGroupExtractor(Constants.MeasurementType measurementType,
                                Constants.AggregationType aggregationType,
                                EntityClusteringProfile profile,
                                IDataHandler dataHandler) {
        this.measurementType = measurementType;
        this.aggregationType = aggregationType;
        this.profile = profile;
        this.dataHandler = dataHandler;
    }

    public List<EntityGroup> clusterData() {
        // 1. Create an EntityGroup for every Entity
        init();

        // 2. Repeat until there is the desired number of entityGroups
        while (entityGroupList.size() > profile.getDesiredNumberOfEntityGroups()) {
            double minDistance = Double.MAX_VALUE;
            int entityGroupAIndex = 0;
            int entityGroupBIndex = 0;

            for (int i = 0; i < entityGroupList.size(); i++) {
                EntityGroup firstEntityGroup = entityGroupList.get(i);
                for (int j = 0; j < i; j++) {
                    EntityGroup secondEntityGroup = entityGroupList.get(j);

                    // 3. Calculate distance for each pair
                    double birthDistance = calculateBirthDistance(firstEntityGroup.getLifeDetails().getBirthBeatId(),
                                                                  secondEntityGroup.getLifeDetails().getBirthBeatId());

                    double deathDistance = calculateDeathDistance(firstEntityGroup.getLifeDetails().getDeathBeatId(),
                            secondEntityGroup.getLifeDetails().getDeathBeatId());

                    double changesDistance = calculateChangesDistance(
                            entityGroupToBeatValueMap.get(firstEntityGroup.getEntityGroupId()),
                            entityGroupToBeatValueMap.get(secondEntityGroup.getEntityGroupId()));

                    double distance  = (profile.getBirthWeight() * birthDistance) +
                                       (profile.getDeathWeight() * deathDistance) +
                                       (profile.getChangesWeight() * changesDistance);

                    // 4. Update min-distance pair
                    if (minDistance > distance) {
                        minDistance = distance;
                        entityGroupAIndex = i;
                        entityGroupBIndex = j;
                    }
                }
            }
            // 5. Merge the min-distance pair
            mergeEntityGroups(entityGroupAIndex, entityGroupBIndex);
        }

        renameEntityGroups();
        
        return entityGroupList;
    }

    private void renameEntityGroups() {
        int i = 0;
        for (EntityGroup group: entityGroupList) {
            group.setEntityGroupId(i);
            i++;
        }
    }

    private void mergeEntityGroups(int entityGroupAIndex, int entityGroupBIndex) {
        EntityGroup firstEntityGroup = entityGroupList.get(entityGroupAIndex);
        EntityGroup secondEntityGroup = entityGroupList.get(entityGroupBIndex);
        firstEntityGroup.mergeWithEntityGroup(secondEntityGroup);

        for (Integer beatId: entityGroupToBeatValueMap.get(secondEntityGroup.getEntityGroupId()).keySet()) {
            double newValue = entityGroupToBeatValueMap.get(secondEntityGroup.getEntityGroupId()).get(beatId);

            if (entityGroupToBeatValueMap.get(firstEntityGroup.getEntityGroupId()).containsKey(beatId)) {
                newValue += entityGroupToBeatValueMap.get(firstEntityGroup.getEntityGroupId()).get(beatId);
            }
            entityGroupToBeatValueMap.get(firstEntityGroup.getEntityGroupId()).put(beatId, newValue);
        }

        // Remove merged entityGroup
        entityGroupToBeatValueMap.remove(secondEntityGroup.getEntityGroupId());
        entityGroupList.remove(entityGroupBIndex);
    }

    // TODO: Try to make this generic to remove duplicate code
    private double calculateChangesDistance(Map<Integer, Double> firstTEMMap, Map<Integer, Double> secondTEMMap) {
        int firstMapSize = firstTEMMap.size();
        int secondMapSize = secondTEMMap.size();

        Map<Integer, Double> smallMap;
        Map<Integer, Double> bigMap;

        if (firstMapSize > secondMapSize) {
            smallMap = secondTEMMap;
            bigMap = firstTEMMap;
        } else {
            smallMap = firstTEMMap;
            bigMap = secondTEMMap;
        }

        double sum = smallMap.keySet().stream().mapToDouble(beatId -> {
            double firstMeasurement = smallMap.get(beatId);
            double secondMeasurement;
            if (bigMap.containsKey(beatId)) {
                secondMeasurement = bigMap.get(beatId);
            } else {
                secondMeasurement = 0;
            }
            return Math.pow(firstMeasurement - secondMeasurement, 2);
        }).sum();

        return Math.sqrt(sum);
    }

    private double calculateDeathDistance(int beatIdA, int beatIdB) {
        int timelineSize = dataHandler.getTimeline().size();
        int firstBeatId = (beatIdA == -1) ? timelineSize : beatIdA;
        int secondBeatId = (beatIdB == -1) ? timelineSize : beatIdB;

        return (double)Math.abs(firstBeatId - secondBeatId) / timelineSize;
    }

    private double calculateBirthDistance(int beatIdA, int beatIdB) {

        // Normalize the birth distance with the dataset's timeline length
        return (double)Math.abs(beatIdA - beatIdB) / dataHandler.getTimeline().size();
    }

    private void init() {
        entityGroupList = new ArrayList<>();
        entityGroupToBeatValueMap = new HashMap<>();

        Map<String, Map<Integer, TimeEntityMeasurements>> entityNameToTEMMap = dataHandler.getEntityNameToTEMMap();

        int entityGroupId = 0;
        for(Entity entity: dataHandler.getPopulation()) {
            EntityGroup entityGroup = new EntityGroup(entityGroupId, entity);
            entityGroupList.add(entityGroup);

            entityGroupToBeatValueMap.put(entityGroupId, new HashMap<>());
            if (entityNameToTEMMap.containsKey(entity.getEntityName())) {
                for (Integer beatId: entityNameToTEMMap.get(entity.getEntityName()).keySet()) {
                    double value = entityNameToTEMMap.get(entity.getEntityName()).get(beatId).getMeasurementOfType(measurementType, aggregationType);
                    entityGroupToBeatValueMap.get(entityGroupId).put(beatId, value);
                }
            }

            entityGroupId++;
        }
    }

}
