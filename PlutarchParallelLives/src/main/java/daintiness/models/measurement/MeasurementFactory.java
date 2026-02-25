package daintiness.models.measurement;

import daintiness.utilities.Constants;

public class MeasurementFactory {
    public IMeasurement getRawMeasurement(double value, Constants.AggregationType aggregationType) {
        return new Measurement(value, Constants.MeasurementType.RAW_VALUE, aggregationType);
    }

    public IMeasurement getDeltaMeasurement(double value, Constants.AggregationType aggregationType) {
        return new Measurement(value, Constants.MeasurementType.DELTA_VALUE, aggregationType);
    }

    public IMeasurement getEmptyMeasurement(Constants.GPMType type) {
        return new EmptyIMeasurement(type);
    }
}
