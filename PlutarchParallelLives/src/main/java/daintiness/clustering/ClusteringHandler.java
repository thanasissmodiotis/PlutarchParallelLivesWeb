package daintiness.clustering;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.text.NumberFormat;
import java.util.*;

import daintiness.clustering.measurements.ChartEntityGroupMeasurementsComparator;
import daintiness.clustering.measurements.ChartGroupPhaseMeasurement;
import daintiness.clustering.measurements.GroupMeasurements;
import daintiness.clustering.measurements.GroupPhaseMeasurement;
import daintiness.data.IDataHandler;
import daintiness.models.Entity;
import daintiness.models.LifeDetails;
import daintiness.models.measurement.IMeasurement;
import daintiness.utilities.Constants;

public class ClusteringHandler implements IClusteringHandler {
    private IDataHandler dataHandler;

    private List<Phase> phases;
    private List<EntityGroup> entityGroups;
    private List<GroupMeasurements> groupMeasurementsList;

    private Map<Constants.MeasurementType, Map<Constants.AggregationType, Double>> maxMeasurementMap;
    private Map<Constants.MeasurementType, Map<Constants.AggregationType, Double>> minMeasurementMap;

    private List<Constants.MeasurementType> availableMeasurementTypesList;
    private List<Constants.AggregationType> availableAggregationTypesList;

    private Constants.MeasurementType measurementType;
    private Constants.AggregationType aggregationType;

    private ObservableList<ChartGroupPhaseMeasurement> chartData;


    public ClusteringHandler() {
        maxMeasurementMap = new HashMap<>();
        minMeasurementMap = new HashMap<>();

        phases = new ArrayList<>();
        entityGroups = new ArrayList<>();
        groupMeasurementsList = new ArrayList<>();
    }

    @Override
    public void setDataHandler(IDataHandler dataHandler) {
        this.dataHandler = dataHandler;
        initializeMeasurementTypes(dataHandler.getType());
    }

    @Override
    public List<Phase> getPhases() {
        return phases;
    }

    @Override
    public List<EntityGroup> getEntityGroups() {
        return entityGroups;
    }

    @Override
    public List<Constants.MeasurementType> getAvailableMeasurementTypesList() {
        return availableMeasurementTypesList;
    }

    @Override
    public List<Constants.AggregationType> getAvailableAggregationTypesList() {
        return availableAggregationTypesList;
    }

    @Override
    public Constants.MeasurementType getMeasurementType() {
        return measurementType;
    }

    @Override
    public Constants.AggregationType getAggregationType() {
        return aggregationType;
    }

    @Override
    public ObservableList<ChartGroupPhaseMeasurement> getChartData() {
        return chartData;
    }

    @Override
    public void sortChartData(Constants.SortingType sortingType) {
        ChartEntityGroupMeasurementsComparator comparator = new ChartEntityGroupMeasurementsComparator(sortingType);
        chartData.sort(comparator);
    }

    @Override
    public void clusterData(ClusteringProfile profile) {
        generatePhases(profile.getBeatClusteringProfile());
        generateEntityGroups(profile.getEntityClusteringProfile());
        generateGroupMeasurements();
        generateChartData(measurementType, aggregationType);
    }

    @Override
    public void generateChartData(Constants.MeasurementType measurementType, Constants.AggregationType aggregationType) {
        this.measurementType = measurementType;
        this.aggregationType = aggregationType;

        List<ChartGroupPhaseMeasurement> chartDataList= new ArrayList<>();
        for (GroupMeasurements groupMeasurements: groupMeasurementsList) {
            ChartGroupPhaseMeasurement chartGroupPhaseMeasurement = new ChartGroupPhaseMeasurement(groupMeasurements.getEntityGroup());
            for (GroupPhaseMeasurement gpm: groupMeasurements.getGroupPhaseMeasurementsList()) {
                int measurementIndex = gpm.containsMeasurementType(measurementType, aggregationType);
                if(measurementIndex != -1) {
                    chartGroupPhaseMeasurement.addMeasurement(gpm.getPhase().getPhaseId(), gpm.getMeasurementList().get(measurementIndex));
                }
            }
            chartDataList.add(chartGroupPhaseMeasurement);
        }

        if (chartData == null) {
            chartData = FXCollections.observableArrayList(chartDataList);
        } else {
            chartData.setAll(chartDataList);
        }
    }


    private void generatePhases(BeatClusteringProfile profile) {
    	PhaseExtractorFactory factory = new PhaseExtractorFactory();
    	IPhaseExtractor phaseExtractor = factory.getPhaseExtractor("AGGLOMERATIVE", profile, dataHandler);

        phases = phaseExtractor.clusterData();
    }

    @Override
    public void loadClusteringData() {
        generateGroupMeasurements();
        generateChartData(measurementType, aggregationType);
    }

    @Override
    public void setMeasurementType(Constants.MeasurementType measurementType) {
        this.measurementType = measurementType;
    }

    @Override
    public void setAggregationType(Constants.AggregationType aggregationType) {
        this.aggregationType = aggregationType;
    }

    @Override
    public void loadPhases(List<int[]> phasesInputList) {
        for (int[] rawPhase: phasesInputList) {
            int phaseId = rawPhase[0];
            int firstBeatId = rawPhase[1];
            int lastBeatId = rawPhase[2];

            Phase phase = new Phase(phaseId, dataHandler.getBeat(firstBeatId));

            for (int beatId= (firstBeatId + 1); beatId <= lastBeatId; beatId++) {
                phase.addBeat(dataHandler.getBeat(beatId));
            }
            phases.add(phase);
        }
    }

    @Override
    public void loadEntityGroup(Map<Integer, String[]> entityGroupData) {
        for(Integer groupId: entityGroupData.keySet()) {
            String[] componentsArray = entityGroupData.get(groupId);
            EntityGroup entityGroup = new EntityGroup(groupId, dataHandler.getEntityByName(componentsArray[0]));

            for (int i=1; i < componentsArray.length; i++) {
                entityGroup.addEntityInGroup(dataHandler.getEntityByName(componentsArray[i]));
            }
            entityGroups.add(entityGroup);
        }

    }

    private void generateEntityGroups(EntityClusteringProfile profile) {
        EntityGroupExtractor entityGroupExtractor = new EntityGroupExtractor(profile, dataHandler);
        
        entityGroups = entityGroupExtractor.clusterData();
    }


    private void generateGroupMeasurements() {
        groupMeasurementsList = new ArrayList<>();

        for (EntityGroup group : entityGroups) {

            int tmpActivity = 0;
            List<GroupPhaseMeasurement> tmpGroupPhaseMeasurementList = new ArrayList<>();

            for (Phase phase : phases) {
                GroupPhaseMeasurement groupPhaseMeasurement = generateGroupPhaseMeasurement(group, phase);

                if (groupPhaseMeasurement.getMeasurementList().size() > 0) {
                    //TODO: Could count only gpms of specific type e.g.
                    //  - (RAW_MEASUREMENT, SUM_OF_ALL) -> schema_evo
                    //  - (RAW_MEASUREMENT, NO_AGGREGATION) -> simple
                    tmpActivity ++;
                }

                if (!groupPhaseMeasurement.getMeasurementList().isEmpty()) {
                    tmpGroupPhaseMeasurementList.add(groupPhaseMeasurement);
                }
            }
            groupMeasurementsList.add(new GroupMeasurements(group, tmpActivity, tmpGroupPhaseMeasurementList));
        }
        colorizeGroupMeasurementsAllTypes();
    }


    private void initializeMeasurementTypes(Constants.FileType type) {
        availableMeasurementTypesList = List.of(Constants.MeasurementType.RAW_VALUE, Constants.MeasurementType.DELTA_VALUE);
        measurementType = Constants.MeasurementType.RAW_VALUE;

        switch (type) {
            case SCHEMA_EVO:
                aggregationType = Constants.AggregationType.SUM_OF_ALL;
                availableAggregationTypesList = List.of(Constants.AggregationType.SUM_OF_ALL,
                        Constants.AggregationType.SUM_OF_INSERTIONS,
                        Constants.AggregationType.SUM_OF_DELETIONS,
                        Constants.AggregationType.SUM_OF_UPDATES,
                        Constants.AggregationType.SUM_OF_INSERTIONS_AND_DELETIONS,
                        Constants.AggregationType.SUM_OF_INSERTIONS_AND_UPDATES,
                        Constants.AggregationType.SUM_OF_DELETIONS_AND_UPDATES);
                break;
            case CSV:
            case TSV:
            case TEM_GPM:
                aggregationType = Constants.AggregationType.NO_AGGREGATION;
                availableAggregationTypesList = List.of(Constants.AggregationType.NO_AGGREGATION);
                break;
            default:
                System.out.println("ClusteringHandler: unsupported FileType found");
        }
    }


    private GroupPhaseMeasurement generateGroupPhaseMeasurement(EntityGroup group, Phase phase) {
        GroupPhaseMeasurement groupPhaseMeasurement = new GroupPhaseMeasurement(group, phase);
        for (Entity entity: group.getGroupComponents()) {

            if (dataHandler.getEntityNameToTEMMap().containsKey(entity.getEntityName())) {

                for(int beatId: dataHandler.getEntityNameToTEMMap().get(entity.getEntityName()).keySet()) {
                    if (phase.containsBeat(beatId)) {
                        groupPhaseMeasurement.addTEM(dataHandler.getEntityNameToTEMMap().get(entity.getEntityName()).get(beatId));
                    }
                }
            }
        }

        updateMinMaxGroupMeasurementValues(groupPhaseMeasurement);

        return groupPhaseMeasurement;
    }


    private void updateMinMaxGroupMeasurementValues(GroupPhaseMeasurement gpm) {
        for (IMeasurement measurement: gpm.getMeasurementList()) {
            Constants.MeasurementType measurementType = measurement.getMeasurementType();
            Constants.AggregationType aggregationType = measurement.getAggregationType();

            if (!maxMeasurementMap.containsKey(measurementType)) {
                maxMeasurementMap.put(measurementType, new HashMap<>());
                minMeasurementMap.put(measurementType, new HashMap<>());
            }

            if (!maxMeasurementMap.get(measurementType).containsKey(aggregationType)) {
                maxMeasurementMap.get(measurementType).put(aggregationType, measurement.getValue());
                minMeasurementMap.get(measurementType).put(aggregationType, measurement.getValue());
                continue;
            }


            if (measurement.getValue() >= maxMeasurementMap.get(measurementType).get(aggregationType)) {
                maxMeasurementMap.get(measurementType).put(aggregationType, measurement.getValue());
            }

            if (measurement.getValue() <= minMeasurementMap.get(measurementType).get(aggregationType)) {
                minMeasurementMap.get(measurementType).put(aggregationType, measurement.getValue());
            }
        }
    }


    private void colorizeGroupMeasurementsAllTypes() {
        for (Constants.MeasurementType measurementType: maxMeasurementMap.keySet()) {
            for (Constants.AggregationType aggregationType: maxMeasurementMap.get(measurementType).keySet()) {
                colorizeGroupMeasurementsOfSpecificType(measurementType, aggregationType);
            }
        }
    }

    @Override
    public String getGPMString() {
        StringBuilder stringData = new StringBuilder(getPhasesString());
        for (ChartGroupPhaseMeasurement gpm: chartData) {
            stringData.append(generateGPMString(gpm)).append("\n");
        }
        return stringData.toString();
    }


    private String generateGPMString(ChartGroupPhaseMeasurement gpm) {
        String gpmString = String.format("%d\t{",gpm.getEntityGroup().getEntityGroupId());

        int i = 0;
        for (Entity componentName: gpm.getEntityGroup().getGroupComponents()) {
            gpmString = gpmString.concat(componentName.getEntityName());
            if (i < gpm.getEntityGroup().getGroupComponentsNames().size() - 1) {
                gpmString = gpmString.concat(",");
            } else {
                gpmString = gpmString.concat("}\t{");
            }
            i++;
        }
        i = 0;
        for (Phase phase: phases) {
            int phaseId = phase.getPhaseId();
            if (gpm.containsMeasurementInPhase(phaseId)) {
                gpmString = gpmString.concat(String.format("%d:", phaseId))
                        .concat(NumberFormat.getInstance(Locale.US).format(gpm.getMeasurement(phaseId).getValue()));
                if ( i < gpm.getNumberOfMeasurements() - 1){
                    gpmString = gpmString.concat(",");
                }
                i++;
            }
        }
        gpmString = gpmString.concat("}");
        return gpmString;
    }


    private String getPhasesString() {
        String header = "";

        int i = 0;
        for (Phase phase: phases) {
            header = header.concat(phase.toString());
            if (i < phases.size() - 1) {
                header = header.concat("\t");
            }
            i++;
        }

        return header.concat("\n");
    }

    // TODO: CLEAN THIS
    private void colorizeGroupMeasurementsOfSpecificType(Constants.MeasurementType measurementType,
                                           Constants.AggregationType aggregationType) {

        int numberOfColoringGroups = 3;
        double maxGroupMeasurementValue = maxMeasurementMap.get(measurementType).get(aggregationType);
        double minGroupMeasurementValue = minMeasurementMap.get(measurementType).get(aggregationType);

        double valueRange = maxGroupMeasurementValue - minGroupMeasurementValue;
        if (valueRange > 1) {
            valueRange += 1;
        }
        double colorGroupRange = valueRange / numberOfColoringGroups;
        Map<Integer, String> groupToColorMap = Map.of(
                10, "#9fffe0",
                0, "#69f0ae",
                1, "#2bbd7e",
                2, "#009933"
        );

        for (GroupMeasurements groupMeasurement: groupMeasurementsList) {
            LifeDetails lifeDetails = groupMeasurement.getEntityGroup().getLifeDetails();
            int birth = lifeDetails.getBirthBeatId();
            int death = lifeDetails.getDeathBeatId();
            boolean isAlive = lifeDetails.isAlive();

            for (GroupPhaseMeasurement groupPhaseMeasurement: groupMeasurement.getGroupPhaseMeasurementsList()) {
                int measurementIndex = groupPhaseMeasurement.containsMeasurementType(measurementType, aggregationType);
                if (measurementIndex != -1) {
                    String color;
                    double value = groupPhaseMeasurement.getMeasurementList().get(measurementIndex).getValue();
                    Phase currentPhase = groupPhaseMeasurement.getPhase();

                    if (((currentPhase.getFirstPhaseBeat().getBeatId() > birth) && isAlive) ||
                        (!isAlive && (currentPhase.getLastPhaseBeat().getBeatId() < death) &&
                                (currentPhase.getFirstPhaseBeat().getBeatId() > birth))) {

                        int coloringGroup;
                        if (value != 0) {
                            coloringGroup = (int) ((value - minGroupMeasurementValue) / colorGroupRange);
                        } else {
                            coloringGroup = 10;
                        }
                        color = groupToColorMap.get(coloringGroup);
                        groupPhaseMeasurement.getMeasurementList().get(measurementIndex).setColor(color);
                    }
                }
            }
        }
    }
}
