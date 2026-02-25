package daintiness.clustering;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import daintiness.clustering.measurements.ChartGroupPhaseMeasurement;
import daintiness.maincontroller.MainController;
import daintiness.utilities.Constants;

import java.io.File;

public class SortingTests {
    File originalFile = new File(
            "src" + Constants.FS + "test" + Constants.FS + "resources" + Constants.FS + "torrentpier__torrentpier");

    MainController mainController = new MainController();


    @Test
    @DisplayName("Sorting by ascending activity test")
    public void sortingByAscendingActivityTest() {
        mainController.load(originalFile);
        mainController.fitDataToGroupPhaseMeasurements(
                new ClusteringProfile(
                        new BeatClusteringProfile(mainController.getNumberOfBeats()),
                        new EntityClusteringProfile(mainController.getNumberOfEntities())
                )
        );
        mainController.generateChartDataOfType(Constants.MeasurementType.RAW_VALUE, Constants.AggregationType.SUM_OF_ALL);
        mainController.sortChartData(Constants.SortingType.ACTIVITY_ASCENDING);

        for(int i=1; i < mainController.getChartData().size(); i++) {
            ChartGroupPhaseMeasurement previousChartRow = mainController.getChartData().get(i-1);
            ChartGroupPhaseMeasurement currentChartRow = mainController.getChartData().get(i);

            int previousActivity = previousChartRow.getActivity();
            int currentActivity = currentChartRow.getActivity();

            Assertions.assertTrue(previousActivity <= currentActivity);
        }
    }

    @Test
    @DisplayName("Sorting by descending activity test")
    public void sortingByDescendingActivityTest() {
        mainController.load(originalFile);
        mainController.fitDataToGroupPhaseMeasurements(
                new ClusteringProfile(
                        new BeatClusteringProfile(mainController.getNumberOfBeats()),
                        new EntityClusteringProfile(mainController.getNumberOfEntities())
                )
        );
        mainController.generateChartDataOfType(Constants.MeasurementType.RAW_VALUE, Constants.AggregationType.SUM_OF_ALL);
        mainController.sortChartData(Constants.SortingType.ACTIVITY_DESCENDING);

        for(int i=1; i < mainController.getChartData().size(); i++) {
            ChartGroupPhaseMeasurement previousChartRow = mainController.getChartData().get(i-1);
            ChartGroupPhaseMeasurement currentChartRow = mainController.getChartData().get(i);

            int previousActivity = previousChartRow.getActivity();
            int currentActivity = currentChartRow.getActivity();

            Assertions.assertTrue(previousActivity >= currentActivity);
        }
    }

    @Test
    @DisplayName("Sorting by ascending birth test")
    public void sortingByAscendingBirthTest() {
        mainController.load(originalFile);
        mainController.fitDataToGroupPhaseMeasurements(
                new ClusteringProfile(
                        new BeatClusteringProfile(mainController.getNumberOfBeats()),
                        new EntityClusteringProfile(mainController.getNumberOfEntities())
                )
        );
        mainController.generateChartDataOfType(Constants.MeasurementType.RAW_VALUE, Constants.AggregationType.SUM_OF_ALL);
        mainController.sortChartData(Constants.SortingType.BIRTH_ASCENDING);

        for(int i=1; i < mainController.getChartData().size(); i++) {
            ChartGroupPhaseMeasurement previousChartRow = mainController.getChartData().get(i-1);
            ChartGroupPhaseMeasurement currentChartRow = mainController.getChartData().get(i);

            int previousBirthId = previousChartRow.getEntityGroup().getLifeDetails().getBirthBeatId();
            int currentBirthId = currentChartRow.getEntityGroup().getLifeDetails().getBirthBeatId();

            Assertions.assertTrue(previousBirthId <= currentBirthId);
        }
    }

    @Test
    @DisplayName("Sorting by descending birth test")
    public void sortingByDescendingDeathTest() {
        mainController.load(originalFile);
        mainController.fitDataToGroupPhaseMeasurements(
                new ClusteringProfile(
                        new BeatClusteringProfile(mainController.getNumberOfBeats()),
                        new EntityClusteringProfile(mainController.getNumberOfEntities())
                )
        );
        mainController.generateChartDataOfType(Constants.MeasurementType.RAW_VALUE, Constants.AggregationType.SUM_OF_ALL);
        mainController.sortChartData(Constants.SortingType.BIRTH_DESCENDING);

        for(int i=1; i < mainController.getChartData().size(); i++) {
            ChartGroupPhaseMeasurement previousChartRow = mainController.getChartData().get(i-1);
            ChartGroupPhaseMeasurement currentChartRow = mainController.getChartData().get(i);

            int previousBirthId = previousChartRow.getEntityGroup().getLifeDetails().getBirthBeatId();
            int currentBirthId = currentChartRow.getEntityGroup().getLifeDetails().getBirthBeatId();

            Assertions.assertTrue(previousBirthId >= currentBirthId);
        }
    }

    @Test
    @DisplayName("Sorting by ascending life duration test")
    public void sortingByAscendingLifeDurationTest() {
        mainController.load(originalFile);
        mainController.fitDataToGroupPhaseMeasurements(
                new ClusteringProfile(
                        new BeatClusteringProfile(mainController.getNumberOfBeats()),
                        new EntityClusteringProfile(mainController.getNumberOfEntities())
                )
        );
        mainController.generateChartDataOfType(Constants.MeasurementType.RAW_VALUE, Constants.AggregationType.SUM_OF_ALL);
        mainController.sortChartData(Constants.SortingType.LIFE_DURATION_ASCENDING);

        for(int i=1; i < mainController.getChartData().size(); i++) {
            ChartGroupPhaseMeasurement previousChartRow = mainController.getChartData().get(i-1);
            ChartGroupPhaseMeasurement currentChartRow = mainController.getChartData().get(i);

            int previousLifeDuration = previousChartRow.getEntityGroup().getLifeDetails().getDuration();
            int currentLifeDuration = currentChartRow.getEntityGroup().getLifeDetails().getDuration();

            Assertions.assertTrue(previousLifeDuration <= currentLifeDuration);
        }
    }

    @Test
    @DisplayName("Sorting by descending life duration test")
    public void sortingByDescendingLifeDurationTest() {
        mainController.load(originalFile);
        mainController.fitDataToGroupPhaseMeasurements(
                new ClusteringProfile(
                        new BeatClusteringProfile(mainController.getNumberOfBeats()),
                        new EntityClusteringProfile(mainController.getNumberOfEntities())
                )
        );
        mainController.generateChartDataOfType(Constants.MeasurementType.RAW_VALUE, Constants.AggregationType.SUM_OF_ALL);
        mainController.sortChartData(Constants.SortingType.LIFE_DURATION_DESCENDING);

        for(int i=1; i < mainController.getChartData().size(); i++) {
            ChartGroupPhaseMeasurement previousChartRow = mainController.getChartData().get(i-1);
            ChartGroupPhaseMeasurement currentChartRow = mainController.getChartData().get(i);

            int previousLifeDuration = previousChartRow.getEntityGroup().getLifeDetails().getDuration();
            int currentLifeDuration = currentChartRow.getEntityGroup().getLifeDetails().getDuration();

            Assertions.assertTrue(previousLifeDuration >= currentLifeDuration);
        }
    }
}