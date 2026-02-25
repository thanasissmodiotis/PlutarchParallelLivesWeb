package daintiness.maincontroller;

import javafx.collections.ObservableList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import daintiness.clustering.*;
import daintiness.clustering.measurements.ChartGroupPhaseMeasurement;
import daintiness.data.IDataHandler;
import daintiness.io.FileHandlerFactory;
import daintiness.io.IFileHandler;
import daintiness.models.PatternData;
import daintiness.patterns.IPatternManager;
import daintiness.patterns.PatternManagerFactory;
import daintiness.utilities.Constants;
import daintiness.utilities.Constants.PatternType;


public class MainController implements IMainController {
    private IFileHandler fileHandler;
    private IDataHandler dataHandler;
    private IClusteringHandler clusteringHandler;
    private IPatternManager patternManager;
    private List<PatternData> totalPatternList = new ArrayList<PatternData>();
    File selectedFile = new File("");
    boolean fileHasChanged;
    PatternType previousPatternType;


    @Override
    public void load(File inputFile){
    	if(inputFile != selectedFile) {
    		fileHasChanged = true;
    	}
    	
        FileHandlerFactory factory = new FileHandlerFactory();
        fileHandler = factory.getFileHandler("SIMPLE_FILE_HANDLER");
        fileHandler.setGivenFile(inputFile);
        dataHandler = fileHandler.loadTEM();
        if (fileHandler.getFileType() == Constants.FileType.SCHEMA_EVO) {
            fileHandler.writeDataToFile(dataHandler.getTimeEntityMeasurementAsString());
        }
        selectedFile = inputFile;
    }


    @Override
    public void save(File outputFile) {
        fileHandler.writeDataToFile(outputFile, dataHandler.getTimeEntityMeasurementAsString());
    }


    @Override
    public void save() {
        fileHandler.writeDataToFile(dataHandler.getTimeEntityMeasurementAsString());
    }


    @Override
    public void exportProject(File projectFolder) {
        fileHandler.exportProject(projectFolder,
                clusteringHandler.getGPMString(),
                dataHandler.getTimeEntityMeasurementAsString());
    }


    @Override
    public void importProject(File projectFolder) {
        FileHandlerFactory fileHandlerFactory = new FileHandlerFactory();
        fileHandler = fileHandlerFactory.getFileHandler("SIMPLE_FILE_HANDLER");

        fileHandler.setGivenFile(projectFolder, Constants.FileType.TEM_GPM);
        dataHandler = fileHandler.loadTEM();

        ClusteringHandlerFactory clusteringHandlerFactory = new ClusteringHandlerFactory();
        clusteringHandler = clusteringHandlerFactory.getClusteringHandler("SIMPLE_CLUSTERING_HANDLER");
        clusteringHandler.setDataHandler(dataHandler);

        clusteringHandler.loadPhases(fileHandler.getPhasesData());
        clusteringHandler.loadEntityGroup(fileHandler.getEntityGroupData());
        clusteringHandler.loadClusteringData();
    }

    @Override
    public boolean hasOutputPath() {
        return fileHandler.getOutputPath() != null;
    }

    @Override
    public void sortChartData(Constants.SortingType type) {
        clusteringHandler.sortChartData(type);
    }


    /**
     * The phases that are created after the data summarization (clustering)
     * @return the list of the phases
     */
    @Override
    public List<Phase> getPhases() {
        return clusteringHandler.getPhases();
    }


    @Override
    public List<EntityGroup> getEntityGroups() {
        return clusteringHandler.getEntityGroups();
    }

    @Override
    public List<Constants.MeasurementType> getAvailableMeasurementTypesList() {
        return clusteringHandler.getAvailableMeasurementTypesList();
    }

    @Override
    public Constants.MeasurementType getMeasurementType() {
        return clusteringHandler.getMeasurementType();
    }

    @Override
    public List<Constants.AggregationType> getAvailableAggregationTypesList() {
        return clusteringHandler.getAvailableAggregationTypesList();
    }

    @Override
    public Constants.AggregationType getAggregationType() {
        return clusteringHandler.getAggregationType();
    }

    @Override
    public ObservableList<ChartGroupPhaseMeasurement> getChartData() {
        if (clusteringHandler == null || clusteringHandler.getChartData() == null) {
            return javafx.collections.FXCollections.observableArrayList();
        }
        return clusteringHandler.getChartData();
    }

    @Override
    public void generateChartDataOfType(Constants.MeasurementType measurementType, Constants.AggregationType aggregationType) {
        clusteringHandler.generateChartData(measurementType, aggregationType);
    }

    @Override
    public void fitDataToGroupPhaseMeasurements(ClusteringProfile profile) {
        ClusteringHandlerFactory factory = new ClusteringHandlerFactory();
        clusteringHandler = factory.getClusteringHandler("SIMPLE_CLUSTERING_HANDLER");
        clusteringHandler.setDataHandler(dataHandler);
        clusteringHandler.clusterData(profile);
    }

    @Override
    public int getNumberOfBeats() {
        return dataHandler.getTimeline().size();
    }

    @Override
    public int getNumberOfEntities() {
        return dataHandler.getPopulation().size();
    }

    @Override
    public int getNumberOfTEMs() {
        return dataHandler.getNumberOfTEMs();
    }
    
    
    @Override
    public List<PatternData> getPatterns(PatternType patternType) {
    	if(totalPatternList.isEmpty() || fileHasChanged || (patternType != previousPatternType)) {
    		PatternManagerFactory patternManagerFactory = new PatternManagerFactory();
        	patternManager = patternManagerFactory.getPatternManager("SIMPLE_PATTERN_MANAGER");

        	
        	ObservableList<ChartGroupPhaseMeasurement> TotalValues = clusteringHandler.getChartData();
        	List<Phase> TotalPhases = clusteringHandler.getPhases();
        	totalPatternList = patternManager.getPatterns(TotalValues, TotalPhases, patternType);
    	}
    	fileHasChanged = false;
    	previousPatternType = patternType;    			
    	//patternManager.printPatterns(totalPatternList, patternType.toString(), selectedFile.getName(), file);
    	return totalPatternList;
    }
    
    @Override
    public void printPatterns(File file) {
    	
    	patternManager.printPatterns(totalPatternList, file, selectedFile.getName());
    }

    // CELL DETAILS METHODS 

    @Override
    public EntityGroup getEntityGroupById(int entityGroupId) {
        if (clusteringHandler == null) return null;
        List<EntityGroup> groups = clusteringHandler.getEntityGroups();
        if (groups == null) return null;
        
        for (EntityGroup group : groups) {
            if (group.getEntityGroupId() == entityGroupId) {
                return group;
            }
        }
        return null;
    }

    @Override
    public Phase getPhaseById(int phaseId) {
        if (clusteringHandler == null) return null;
        List<Phase> phases = clusteringHandler.getPhases();
        if (phases == null) return null;
        
        for (Phase phase : phases) {
            if (phase.getPhaseId() == phaseId) {
                return phase;
            }
        }
        return null;
    }

    @Override
    public List<java.util.Map<String, Object>> getCellBreakdown(int entityGroupId, int phaseId) {
        List<java.util.Map<String, Object>> breakdown = new ArrayList<>();
        
        EntityGroup group = getEntityGroupById(entityGroupId);
        Phase phase = getPhaseById(phaseId);
        
        if (group == null || phase == null || dataHandler == null) {
            return breakdown;
        }
        
        List<Integer> beatIds = phase.getPhaseComponentsIdList();
        
        // for each entity in the group
        for (String entityName : group.getGroupComponentsNames()) {
            java.util.Map<String, Object> entityBreakdown = new java.util.HashMap<>();
            entityBreakdown.put("entityName", entityName);
            
            daintiness.models.Entity entity = dataHandler.getEntityByName(entityName);
            if (entity == null) continue;
            
            // calculate total value across all beats in the phase
            double total = 0;
            int count = 0;
            
            for (int beatId : beatIds) {
                daintiness.models.Beat beat = dataHandler.getBeat(beatId);
                if (beat == null) continue;
                
                daintiness.models.TimeEntityMeasurements tem = dataHandler.getTem(entity, beat);
                if (tem != null && tem.getMeasurements() != null && !tem.getMeasurements().isEmpty()) {
                    // sum all measurement values for this entity in this beat
                    for (daintiness.models.measurement.IMeasurement m : tem.getMeasurements()) {
                        total += m.getValue();
                        count++;
                    }
                }
            }
            
            entityBreakdown.put("value", total);
            entityBreakdown.put("beatCount", count);
            breakdown.add(entityBreakdown);
        }
        
        return breakdown;
    }

    @Override
    public daintiness.data.IDataHandler getDataHandler() {
        return dataHandler;
    }
}
