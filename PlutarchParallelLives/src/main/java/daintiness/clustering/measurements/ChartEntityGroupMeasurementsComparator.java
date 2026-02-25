package daintiness.clustering.measurements;

import java.util.Comparator;

import daintiness.models.LifeDetails;
import daintiness.utilities.Constants;


public class ChartEntityGroupMeasurementsComparator implements Comparator<ChartGroupPhaseMeasurement> {

    private final Constants.SortingType sortingType;

    public ChartEntityGroupMeasurementsComparator(Constants.SortingType sortingType) {
        this.sortingType = sortingType;
    }

    private Comparator<LifeDetails> generateGroupLifeDetailsComparator() {
        switch (sortingType) {
            case BIRTH_ASCENDING:
                return Comparator.comparing(LifeDetails::getBirthBeatId);
            case BIRTH_DESCENDING:
                return Comparator.comparing(LifeDetails::getBirthBeatId).reversed();
            case LIFE_DURATION_ASCENDING:
                return Comparator.comparing(LifeDetails::getDuration);
            case LIFE_DURATION_DESCENDING:
                return Comparator.comparing(LifeDetails::getDuration).reversed();
            default:
                throw new IllegalStateException("Unexpected value: " + sortingType);
        }
    }

    private Comparator<ChartGroupPhaseMeasurement> generateActivityComparator() {
        switch (sortingType) {
            case ACTIVITY_ASCENDING:
                return Comparator.comparing(ChartGroupPhaseMeasurement::getActivity);
            case ACTIVITY_DESCENDING:
                return Comparator.comparing(ChartGroupPhaseMeasurement::getActivity).reversed();
            default:
                throw new IllegalStateException("Unexpected value: " + sortingType);
        }
    }

    @Override
    public int compare(ChartGroupPhaseMeasurement o1, ChartGroupPhaseMeasurement o2) {
        if (sortingType != Constants.SortingType.ACTIVITY_ASCENDING &&
            sortingType != Constants.SortingType.ACTIVITY_DESCENDING) {

            Comparator<LifeDetails> groupComparator = generateGroupLifeDetailsComparator();
            return groupComparator.compare(o1.getEntityGroup().getLifeDetails(), o2.getEntityGroup().getLifeDetails());

        } else {

            Comparator<ChartGroupPhaseMeasurement> groupMeasurementsComparator = generateActivityComparator();
            return groupMeasurementsComparator.compare(o1, o2);

        }
    }
}
