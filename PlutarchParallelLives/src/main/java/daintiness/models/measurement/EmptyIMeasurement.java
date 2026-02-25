package daintiness.models.measurement;

import daintiness.utilities.Constants;

public class EmptyIMeasurement implements IMeasurement {
    private String color;
    private double value;


    public EmptyIMeasurement(Constants.GPMType type) {
        switch (type) {
            case BIRTH:
//                color = "#3CB371";
//                break;
            case DEATH:
                color = "#000000";
                break;
            case ACTIVE:
                color = "#9fffe0";
                break;
            case INACTIVE:
                color = "#717D7E";
                break;
            default:
        }
    }

    @Override
    public Constants.AggregationType getAggregationType() {
        return null;
    }

    @Override
    public Constants.MeasurementType getMeasurementType() {
        return null;
    }

    @Override
    public double getValue() {
        return value;
    }

    @Override
    public void addToValue(double newValue) {
    }

    @Override
    public void setColor(String hexColor) {
    }

    @Override
    public String getColor() {
        return color;
    }
}
