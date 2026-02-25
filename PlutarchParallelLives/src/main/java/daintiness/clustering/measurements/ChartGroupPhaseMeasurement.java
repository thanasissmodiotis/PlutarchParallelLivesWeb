package daintiness.clustering.measurements;

import java.util.HashMap;
import java.util.Map;

import daintiness.clustering.EntityGroup;
import daintiness.models.measurement.IMeasurement;


public class ChartGroupPhaseMeasurement {
    private final EntityGroup entityGroup;

    private Map<Integer, IMeasurement> measurementToPhaseMap;
    private int activity;

    public EntityGroup getEntityGroup() {
        return entityGroup;
    }

    public int getActivity() {
        return activity;
    }

    public ChartGroupPhaseMeasurement(EntityGroup entityGroup) {
        this.entityGroup = entityGroup;
        this.activity = 0;
        this.measurementToPhaseMap = new HashMap<>();
    }

    public void addMeasurement(int phaseId, IMeasurement measurement) {
        measurementToPhaseMap.put(phaseId, measurement);
        activity++;
    }

    public IMeasurement getMeasurement(int phaseId){
        return measurementToPhaseMap.get(phaseId);
    }

    public boolean containsMeasurementInPhase(int phaseId) {
        return measurementToPhaseMap.containsKey(phaseId);
    }

    public int getNumberOfMeasurements() {
        return measurementToPhaseMap.size();
    }

    public void setMeasurementToPhaseMap(Map<Integer, IMeasurement> measurementToPhaseMap) {
        this.measurementToPhaseMap = measurementToPhaseMap;
    }
}
