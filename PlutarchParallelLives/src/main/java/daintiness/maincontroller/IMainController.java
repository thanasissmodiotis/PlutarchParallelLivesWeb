package daintiness.maincontroller;

import javafx.collections.ObservableList;

import java.io.File;
import java.util.List;

import daintiness.clustering.ClusteringProfile;
import daintiness.clustering.EntityGroup;
import daintiness.clustering.Phase;
import daintiness.clustering.measurements.ChartGroupPhaseMeasurement;
import daintiness.models.PatternData;
import daintiness.utilities.Constants;
import daintiness.utilities.Constants.PatternType;

/**
 * This is the main API of the tool.
 * It exposes a number of methods to the developer/
 */
public interface IMainController {

    /**
     * Loads the specified dataset in the DataHandler (intermediate representation)
     *
     * @param inputFile the file that contains the dataset
     */
    void load(File inputFile);



    /**
     * Saves the current DataHandler (intermediate representation)
     * in the specified file
     *
     * @param outputFile the output file
     */
    void save(File outputFile);


    /**
     * Save without parameters is used to quick save our data.
     * The output file must be already set.
     */
    void save();


    /**
     * @return if the output file for the save method is set returns true, else false
     */
    boolean hasOutputPath();


    /**
     * Sorts data by the given sorting type.
     * <p/>
     * The current available sorting types can be found
     * in the Constants.SortingType enum.
     *
     * @param type enum sorting type
     */
    void sortChartData(Constants.SortingType type);


    /**
     * The phases that are created after the data summarization (clustering)
     * @return the list of the phases
     */
    List<Phase> getPhases();


    /**
     * The entity groups that are created after the data summarization (clustering)
     * @return the list of the entity groups
     */
    List<EntityGroup> getEntityGroups();


    /**
     * Returns a list of the dataset's available MeasurementTypes.
     * <p/>
     * The available measurement types can be found in Constants.MeasurementType enum.
     * @return list of MeasurementType
     */
    List<Constants.MeasurementType> getAvailableMeasurementTypesList();


    /**
     * @return the selected measurement type
     */
    Constants.MeasurementType getMeasurementType();


    /**
     * Returns a list of the dataset's available AggregationTypes
     * <p/>
     * This is mainly used for schema evolution data that consist of
     * transactions that need to be aggregated.
     * <p/>
     * The available types can be found in Constants.AggregationType
     *
     * @return list of aggregation type
     */
    List<Constants.AggregationType> getAvailableAggregationTypesList();


    /**
     * @return the selected aggregation data
     */
    Constants.AggregationType getAggregationType();


    /**
     * Returns an ObservableList of the summarized data.
     * This is the input of the PLDiagram.
     * @return an ObservableList of ChartGroupPhaseMeasurement
     */
    ObservableList<ChartGroupPhaseMeasurement> getChartData();


    /**
     * Creates the chart data of the specified measurement and aggregation types.
     * @param measurementType
     * @param aggregationType
     */
    void generateChartDataOfType(Constants.MeasurementType measurementType, Constants.AggregationType aggregationType);


    /**
     * Clusters data using the specified ClusteringProfile
     * @param profile ClusteringProfile
     */
    void fitDataToGroupPhaseMeasurements(ClusteringProfile profile);


    /**
     * @return dataset's number of Time Beats (columns)
     */
    int getNumberOfBeats();


    /**
     * @return dataset's number of entities (rows)
     */
    int getNumberOfEntities();


    /**
     * @return number of TimeEntityMeasurements (non-empty cells)
     */
    int getNumberOfTEMs();


    /**
     * Exports to the specified folder the TimeEntityMeasurements as tem.tsv
     * and the GroupPhaseData as gpm.tsv
     *
     * @param projectFolder output folder
     */
    void exportProject(File projectFolder);


    /**
     * Imports the TimeEntityMeasurements and the GroupPhaseData from
     * the specified folder.
     * <p/>
     * The specified folder must contain the files tem.tsv & gpm.tsv
     *
     * @param projectFolder input folder
     */
    void importProject(File projectFolder);



	List<PatternData> getPatterns(PatternType patternType);
	
	void printPatterns(File outputFile);

	// ============ CELL DETAILS METHODS ============

	/**
	 * Returns an EntityGroup by its ID
	 * @param entityGroupId the ID of the entity group
	 * @return EntityGroup or null if not found
	 */
	EntityGroup getEntityGroupById(int entityGroupId);

	/**
	 * Returns a Phase by its ID
	 * @param phaseId the ID of the phase
	 * @return Phase or null if not found
	 */
	Phase getPhaseById(int phaseId);

	/**
	 * Returns detailed breakdown of a cell (entity group + phase combination)
	 * showing individual entity contributions within the group
	 * @param entityGroupId the entity group ID
	 * @param phaseId the phase ID
	 * @return List of maps containing entity name and value
	 */
	List<java.util.Map<String, Object>> getCellBreakdown(int entityGroupId, int phaseId);

	/**
	 * Returns the IDataHandler for direct data access
	 * @return IDataHandler
	 */
	daintiness.data.IDataHandler getDataHandler();
}
