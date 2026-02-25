package daintiness.clustering;

import java.util.*;

import daintiness.data.DataHandler;
import daintiness.data.IDataHandler;
import daintiness.models.Beat;
import daintiness.models.TimeEntityMeasurements;
import daintiness.utilities.Constants;

public class AgglomerativePhaseExtractor implements IPhaseExtractor{
    private Constants.MeasurementType measurementType = Constants.MeasurementType.RAW_VALUE;
    private Constants.AggregationType aggregationType = Constants.AggregationType.SUM_OF_ALL;
    private final BeatClusteringProfile profile;

    private final IDataHandler dataHandler;

    private List<Phase> phaseList;
    private Map<Integer, Map<String, Double>> phaseToEntityValueMap;


    // Default constructor;
    public AgglomerativePhaseExtractor(BeatClusteringProfile profile, IDataHandler dataHandler) {
        this.profile = profile;
        this.dataHandler = dataHandler;

        if (dataHandler.getType() != Constants.FileType.SCHEMA_EVO) {
            aggregationType = Constants.AggregationType.NO_AGGREGATION;
        }
    }

    public AgglomerativePhaseExtractor(Constants.MeasurementType measurementType,
                          Constants.AggregationType aggregationType,
                          BeatClusteringProfile profile,
                          DataHandler dataHandler) {
        this.measurementType = measurementType;
        this.aggregationType = aggregationType;
        this.profile = profile;
        this.dataHandler = dataHandler;
    }

    public List<Phase> clusterData() {
        // 1. Create a phase for every Beat
        init();

        // 2. Repeat until there is the desired number of phases
        while(phaseList.size() > profile.getDesiredNumberOfPhases()) {

            double minDistance = Double.MAX_VALUE;
            int minIndex = 0;

            for (int i=0; i<(phaseList.size() - 1); i++) {
                int currentPhaseId = phaseList.get(i).getPhaseId();
                int nextPhaseId = phaseList.get(i+1).getPhaseId();


                Map<String, Double> currentTEMMap = phaseToEntityValueMap.get(currentPhaseId);
                Map<String, Double> nextTEMMap = phaseToEntityValueMap.get(nextPhaseId);

                double changeDistance = calculateDistance(currentTEMMap, nextTEMMap);
                double timeDistance = calculateTimeDistance(phaseList.get(i), phaseList.get(i+1));

                double distance = (profile.getChangesWeight() * changeDistance) + (profile.getTimeWeight() * timeDistance);
                if (minDistance > distance) {
                    minDistance = distance;
                    minIndex = i;
                }
            }
            mergePhases(minIndex);
        }

        // Rename the phases
        int i = 0;
        for (Phase phase : phaseList) {
            phase.setPhaseId(i);
            i++;
        }
        
        return phaseList;
    }

    private void mergePhases(int minIndex) {
        Phase currentPhase = phaseList.get(minIndex);
        Phase nextPhase = phaseList.get(minIndex + 1);
        currentPhase.mergeWithPhase(nextPhase);

        // Update map
        for (String entityName: phaseToEntityValueMap.get(nextPhase.getPhaseId()).keySet()) {

            double newValue = phaseToEntityValueMap.get(nextPhase.getPhaseId()).get(entityName);

            if (phaseToEntityValueMap.get(currentPhase.getPhaseId()).containsKey(entityName)) {
                newValue += phaseToEntityValueMap.get(currentPhase.getPhaseId()).get(entityName);
            }
            phaseToEntityValueMap.get(currentPhase.getPhaseId()).put(entityName, newValue);
        }

        // Remove merged phase
        phaseToEntityValueMap.remove(nextPhase.getPhaseId());
        phaseList.remove(minIndex + 1);
    }

    private double calculateTimeDistance(Phase currentPhase, Phase nextPhase) {
        return nextPhase.getPhaseId() - currentPhase.getPhaseId();
    }

    private void init() {
        phaseList = new ArrayList<>();
        phaseToEntityValueMap = new HashMap<>();

        Map<Integer, Map<String , TimeEntityMeasurements>> beatIdToTEMMap = dataHandler.getBeatIdToTEMMap();

        int phaseId = 0;
        for(Beat beat : dataHandler.getTimeline()) {
            Phase phase = new Phase(phaseId, beat);
            phaseList.add(phase);

            phaseToEntityValueMap.put(phaseId, new HashMap<>());
            if (beatIdToTEMMap.containsKey(beat.getBeatId())) {
                for (String entityName: beatIdToTEMMap.get(beat.getBeatId()).keySet()) {
                    double value = beatIdToTEMMap.get(beat.getBeatId()).get(entityName).getMeasurementOfType(measurementType, aggregationType);
                    phaseToEntityValueMap.get(phaseId).put(entityName, value);
                }
            }


            phaseId++;
        }
    }

    private double calculateDistance(Map<String, Double> currentTEMMap, Map<String, Double> nextTEMMap) {
        int currentMapSize = currentTEMMap.size();
        int nextMapSize = nextTEMMap.size();

        Map<String, Double> smallMap;
        Map<String, Double> bigMap;


        if (currentMapSize > nextMapSize) {
            smallMap = nextTEMMap;
            bigMap = currentTEMMap;
        } else {
            smallMap = currentTEMMap;
            bigMap = nextTEMMap;
        }


        double sum = smallMap.keySet().stream().mapToDouble(entityName -> {
            double firstMeasurement = smallMap.get(entityName);
            double secondMeasurement;
            if (bigMap.containsKey(entityName)) {
                secondMeasurement = bigMap.get(entityName);
            } else {
                secondMeasurement = 0;
            }
            return Math.pow(firstMeasurement - secondMeasurement, 2);
        }).sum();
        return Math.sqrt(sum);
    }

}
