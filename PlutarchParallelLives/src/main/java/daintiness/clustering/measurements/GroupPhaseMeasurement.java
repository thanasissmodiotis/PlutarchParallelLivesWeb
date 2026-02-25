package daintiness.clustering.measurements;

import java.util.ArrayList;
import java.util.List;

import daintiness.clustering.EntityGroup;
import daintiness.clustering.Phase;
import daintiness.models.TimeEntityMeasurements;
import daintiness.models.measurement.IMeasurement;
import daintiness.utilities.Constants;


public class GroupPhaseMeasurement {
    private final EntityGroup entityGroup;
    private final Phase phase;
    private final List<TimeEntityMeasurements> temList;
    private final List<IMeasurement> measurementList;

    public GroupPhaseMeasurement(EntityGroup entityGroup, Phase phase) {
        this.phase = phase;
        this.entityGroup = entityGroup;
        temList = new ArrayList<>();
        measurementList = new ArrayList<>();
    }


    public EntityGroup getEntityGroup() {
        return entityGroup;
    }

    public Phase getPhase() {
        return phase;
    }

    public List<TimeEntityMeasurements> getTemList() {
        return temList;
    }

    public List<IMeasurement> getMeasurementList() {
        return measurementList;
    }

    public void addTEM(TimeEntityMeasurements tem) {
        temList.add(tem);

        for (IMeasurement newTEMMeasurement: tem.getMeasurements()) {

            boolean measurementTypeFound = false;
            for (IMeasurement gpmMeasurement: measurementList) {
                if ((gpmMeasurement.getMeasurementType() == newTEMMeasurement.getMeasurementType()) &&
                        (gpmMeasurement.getAggregationType() == newTEMMeasurement.getAggregationType())) {
                    measurementTypeFound = true;
                    gpmMeasurement.addToValue(newTEMMeasurement.getValue());
                }
            }
            if (!measurementTypeFound) {
                measurementList.add(newTEMMeasurement);
            }
        }
    }


    public int containsMeasurementType(Constants.MeasurementType measurementType, Constants.AggregationType aggregationType) {

        for (int i=0; i < measurementList.size(); i++) {
            if (measurementList.get(i).getMeasurementType().equals(measurementType) &&
                    measurementList.get(i).getAggregationType().equals(aggregationType)) {
                return i;
            }
        }
        return -1;
    }
}
