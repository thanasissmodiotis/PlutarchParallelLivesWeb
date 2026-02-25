package daintiness.data;

import java.text.NumberFormat;
import java.util.*;

import daintiness.models.Beat;
import daintiness.models.Entity;
import daintiness.models.TimeEntityMeasurements;
import daintiness.utilities.Constants;

public class DataHandler implements IDataHandler {
    private Constants.FileType type;

    private List<Beat> timeline;
    private List<Entity> population;
    private List<TimeEntityMeasurements> measurementsList;

    private Map<String, Map<Integer,TimeEntityMeasurements>> entityIdToTEMMap;
    private Map<Integer, Map<String, TimeEntityMeasurements>> beatIdToTEMMap;


    @Override
    public void init(List<Beat> timeline, List<Entity> population, List<TimeEntityMeasurements> measurementsList) {
        this.timeline = timeline;
        this.population = population;
        this.measurementsList = measurementsList;

        mapTEMs();
    }

    @Override
    public Constants.FileType getType() {
        return type;
    }

    @Override
    public int getNumberOfTEMs() {
        return measurementsList.size();
    }

    public void setType(Constants.FileType type) {
        this.type = type;
    }

    public List<Beat> getTimeline() {
        return timeline;
    }

    public List<Entity> getPopulation() {
        return population;
    }

    @Override
    public Map<String, Map<Integer, TimeEntityMeasurements>> getEntityNameToTEMMap() {
        return entityIdToTEMMap;
    }

    @Override
    public Map<Integer, Map<String, TimeEntityMeasurements>> getBeatIdToTEMMap() {
        return beatIdToTEMMap;
    }

    @Override
    public TimeEntityMeasurements getTem(Entity entity, Beat beat) {
        if (entityIdToTEMMap.containsKey(entity.getEntityName())) {
            if (entityIdToTEMMap.get(entity.getEntityName()).containsKey(beat.getBeatId())) {
                return entityIdToTEMMap.get(entity.getEntityName()).get(beat.getBeatId());
            }
        }
        return new TimeEntityMeasurements(null, null, null);
    }

    private void mapTEMs() {
        entityIdToTEMMap = new HashMap<>();
        beatIdToTEMMap = new HashMap<>();

        for (TimeEntityMeasurements tem : measurementsList) {
            if (!entityIdToTEMMap.containsKey(tem.getEntity().getEntityName())) {
                entityIdToTEMMap.put(tem.getEntity().getEntityName(), new HashMap<>());
            }
            entityIdToTEMMap.get(tem.getEntity().getEntityName()).put(tem.getBeat().getBeatId(), tem);

            if (!beatIdToTEMMap.containsKey(tem.getBeat().getBeatId())) {
                beatIdToTEMMap.put(tem.getBeat().getBeatId(), new HashMap<>());
            }
            beatIdToTEMMap.get(tem.getBeat().getBeatId()).put(tem.getEntity().getEntityName(), tem);
        }
    }

    @Override
    public Entity getEntity(int index) {
        return population.get(index);
    }

    @Override
    public Entity getEntityByName(String entityName) {
        Optional<Entity> result = population.stream().filter(entity -> entityName.equals(entity.getEntityName())).findFirst();
        if (result.isPresent()) {
            return result.get();
        } else {
            System.out.println("Oops the entity you are looking doesn't exist");
            return null;
        }
    }

    @Override
    public Beat getBeat(int index) {
        return timeline.get(index);
    }


    @Override
    public String getTimeEntityMeasurementAsString() {
        String semiHeader = "{name, birthId, deathId, status}\t";
        return semiHeader.concat(getTimelineAsString()).concat(getMeasurementsAsString());
    }

    public String getTimelineAsString() {
        String timelineString = "";
        int i = 0;
        for (Beat beat : timeline) {
            timelineString = timelineString.concat(beat.getDateAsString());
            if (i <timeline.size() - 1) {
                timelineString = timelineString.concat("\t");
            } else {
                timelineString = timelineString.concat("\n");
            }
            i++;
        }
        return timelineString;
    }


    private String getMeasurementsAsString() {
        Constants.MeasurementType measurementType = Constants.MeasurementType.RAW_VALUE;
        Constants.AggregationType aggregationType;
        if (type == Constants.FileType.SCHEMA_EVO) {
            aggregationType = Constants.AggregationType.SUM_OF_ALL;
        } else  {
            aggregationType = Constants.AggregationType.NO_AGGREGATION;
        }


        StringBuilder temSB = new StringBuilder();
        for (Entity entity : population) {
            String entityName = entity.getEntityName();
            int birth = entity.getLifeDetails().getBirthBeatId();
            int death = entity.getLifeDetails().getDeathBeatId();
            int status = entity.getLifeDetails().isAlive() ? 1 : 0;
            temSB.append(String.format("{%s,%d,%d,%d}\t", entityName, birth, death, status));

            for (Beat beat: timeline) {
                double value = Double.NaN;

                if (entityIdToTEMMap.containsKey(entityName) &&
                        entityIdToTEMMap.get(entityName).containsKey(beat.getBeatId())) {
                    int measurementIndex =getTem(entity, beat).containsMeasurementType(measurementType, aggregationType);

                    if (measurementIndex != -1) {
                        value = getTem(entity, beat).getMeasurements().get(measurementIndex).getValue();
                    }
                }
                if (!Double.isNaN(value)) {
                    temSB.append(NumberFormat.getInstance(Locale.US).format(value));
                }
                temSB.append("\t");
            }
            temSB.append("\n");
        }
        return temSB.toString();
    }
}
