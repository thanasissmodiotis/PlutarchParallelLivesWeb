package daintiness.models.measurement;

import daintiness.utilities.Constants;

public class Measurement implements IMeasurement{
    private final Constants.MeasurementType measurementType;
    private final Constants.AggregationType aggregationType;
    private double value;
    private String color;


    public Measurement(double value,
                       Constants.MeasurementType measurementType,
                       Constants.AggregationType aggregationType) {
        this.value = value;
        this.measurementType = measurementType;
        this.aggregationType = aggregationType;
    }

    @Override
    public Constants.AggregationType getAggregationType() {
        return aggregationType;
    }

    @Override
    public Constants.MeasurementType getMeasurementType() {
        return measurementType;
    }

    @Override
    public double getValue() {
        return value;
    }

    @Override
    public void addToValue(double newValue) {
        value += newValue;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
