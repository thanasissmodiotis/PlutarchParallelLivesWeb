package daintiness.models.measurement;

import daintiness.utilities.Constants.AggregationType;
import daintiness.utilities.Constants.MeasurementType;

public interface IMeasurement {

    AggregationType getAggregationType();

    MeasurementType getMeasurementType();

    double getValue();

    void addToValue(double newValue);

    void setColor(String hexColor);

    String getColor();
}
