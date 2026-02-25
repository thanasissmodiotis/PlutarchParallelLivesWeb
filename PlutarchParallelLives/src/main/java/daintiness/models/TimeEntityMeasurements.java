package daintiness.models;


import java.util.List;

import daintiness.models.measurement.IMeasurement;
import daintiness.utilities.Constants;


public class TimeEntityMeasurements {
    private final Entity entity;
    private final Beat beat;
    private final List<IMeasurement> measurements;


    public TimeEntityMeasurements(Entity entity, Beat beat, List<IMeasurement> measurements) {
        this.entity = entity;
        this.beat = beat;
        this.measurements = measurements;
    }

    public Entity getEntity() {
        return entity;
    }

    public Beat getBeat() {
        return beat;
    }

    public List<IMeasurement> getMeasurements() {
        return measurements;
    }

    public int containsMeasurementType(Constants.MeasurementType measurementType, Constants.AggregationType aggregationType) {

        for (int i=0; i < measurements.size(); i++) {
            if (measurements.get(i).getMeasurementType() == measurementType &&
                    measurements.get(i).getAggregationType() == aggregationType) {
                return i;
            }
        }
        return -1;
    }

    public double getMeasurementOfType(Constants.MeasurementType measurementType, Constants.AggregationType aggregationType) {
        int resultIndex = containsMeasurementType(measurementType, aggregationType);
        if (resultIndex == -1) {
            return 0;
        } else {
            return measurements.get(resultIndex).getValue();
        }
    }
}
