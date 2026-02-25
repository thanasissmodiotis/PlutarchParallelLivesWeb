package daintiness.clustering;

import javafx.collections.ObservableList;

import java.util.List;
import java.util.Map;

import daintiness.clustering.measurements.ChartGroupPhaseMeasurement;
import daintiness.data.IDataHandler;
import daintiness.utilities.Constants;

public interface IClusteringHandler {

    /**
     * Sets the data input that is going to be summarized.
     * @param dataHandler Intermediate Representation
     */
    void setDataHandler(IDataHandler dataHandler);

    /**
     * The List of Phases that is created after the timeline(Beats) clustering.
     * @return List of Phases
     */
    List<Phase> getPhases();

    /**
     * The List of EntityGroups is created after the population(Entities) clustering
     * @return List of EntityGroups
     */
    List<EntityGroup> getEntityGroups();

    /**
     * List of available for the dataset MeasurementTypes.<br>
     * (RAW_VALUE, DELTA_VALUE)
     *
     * @return List of available for the dataset MeasurementTypes
     */
    List<Constants.MeasurementType> getAvailableMeasurementTypesList();

    /**
     * List of available for the dataset AggregationTypes.<br>
     * (SUM_OF_INSERTIONS, SUM_OF_DELETIONS, SUM_OF_UPDATES,<br>
     *  SUM_OF_INSERTIONS_AND_DELETIONS, <br>
     *  SUM_OF_INSERTIONS_AND_UPDATES, <br>
     *  SUM_OF_DELETIONS_UPDATES, <br>
     *  SUM_OF_ALL, NO_AGGREGATION)
     * @return List of available for the dataset AggregationTypes
     */
    List<Constants.AggregationType> getAvailableAggregationTypesList();

    /**
     * @return Current MeasurementType
     */
    Constants.MeasurementType getMeasurementType();

    /**
     * @return Current AggregationType
     */
    Constants.AggregationType getAggregationType();

    /**
     * This method returns the data structure that the gui uses.
     * @return ObservableList of ChartGroupPhaseMeasurement
     */
    ObservableList<ChartGroupPhaseMeasurement> getChartData();

    /**
     * This method sorts the chartData by the given sorting type. <br>
     * ACTIVITY_ASCENDING,<br>
     * ACTIVITY_DESCENDING,<br>
     * BIRTH_ASCENDING,<br>
     * BIRTH_DESCENDING,<br>
     * LIFE_DURATION_ASCENDING,<br>
     * LIFE_DURATION_DESCENDING<br>
     * @param sortingType SortingType
     */
    void sortChartData(Constants.SortingType sortingType);

    /**
     * This method performs the clustering in rows and columns
     * @param profile ClusteringProfile to be applied
     */
    void clusterData(ClusteringProfile profile);

    /**
     * We cannot show all the available Measurements, <br>
     * that's why select what we will show
     * @param measurementType Selected MeasurementType
     * @param aggregationType Selected AggregationType
     */
    void generateChartData(Constants.MeasurementType measurementType, Constants.AggregationType aggregationType);

    /**
     * This method generate the phases from the given components indexes. <br>
     * It's used when we load the summarized representation from file.
     * @param phasesInputList List of components ids arrays
     */
    void loadPhases(List<int[]> phasesInputList);


    /**
     * This method generate the entityGroups from the given components names. <br>
     * It's used when we load the summarized representation from file.
     * @param entityGroupData Map EntityGroupId to Component Name Arrays
     */
    void loadEntityGroup(Map<Integer, String[]> entityGroupData);

    /**
     * Set selected MeasurementType
     * @param measurementType MeasurementType
     */
    void setMeasurementType(Constants.MeasurementType measurementType);

    /**
     * Set selected AggregationType
     * @param aggregationType Selected AggregationType
     */
    void setAggregationType(Constants.AggregationType aggregationType);

    /**
     * Assemblies the GroupPhaseMeasurements and the chartData.<br>
     * It is when we import TimeEntityMeasurements and GroupPhaseMeasurements from File (Import Project)
     */
    void loadClusteringData();

    /**
     * @return Generates a String from the ChartData
     */
    String getGPMString();
}
