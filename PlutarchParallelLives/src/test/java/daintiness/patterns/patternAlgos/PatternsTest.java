package daintiness.patterns.patternAlgos;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import daintiness.clustering.BeatClusteringProfile;
import daintiness.clustering.ClusteringProfile;
import daintiness.clustering.EntityClusteringProfile;
import daintiness.clustering.IClusteringHandler;
import daintiness.clustering.Phase;
import daintiness.clustering.measurements.ChartGroupPhaseMeasurement;
import daintiness.data.IDataHandler;
import daintiness.io.input.ILoader;
import daintiness.maincontroller.MainController;
import daintiness.models.CellInfo;
import daintiness.models.PatternData;
import daintiness.patterns.IPatternManager;
import daintiness.patterns.PatternManagerFactory;
import daintiness.utilities.Constants;
import daintiness.utilities.Constants.PatternType;
import javafx.collections.ObservableList;

public class PatternsTest {
	MainController mainController = new MainController();
	File originalFile = new File(
			"src" + Constants.FS + "test" + Constants.FS + "resources" + Constants.FS + "biosql");
	String Path = "src" + Constants.FS + "test" + Constants.FS + "resources" + Constants.FS + "biosql"
			+ Constants.FS + "figures" + Constants.FS + "PPL";
	
	ILoader loader;
	IDataHandler dataHandler;
	IPatternManager patternManager;
	IClusteringHandler clusteringHandler;
	
	
	
	List<PatternData> expectedPatternList = new ArrayList<PatternData>();
	PatternData patternData;
	

	Constants.MeasurementType measurementType = Constants.MeasurementType.RAW_VALUE;
    Constants.AggregationType aggregationType = Constants.AggregationType.SUM_OF_ALL;
    
    

	
	@Test
	@DisplayName("Check if patterns are correctly test")
	public void patternTest() {

		PatternManagerFactory patternManagerFactory = new PatternManagerFactory();
		patternManager = patternManagerFactory.getPatternManager("SIMPLE_PATTERN_MANAGER");


		
		mainController.load(originalFile);
		mainController.fitDataToGroupPhaseMeasurements(
				new ClusteringProfile(new BeatClusteringProfile(mainController.getNumberOfBeats()),
						new EntityClusteringProfile(mainController.getNumberOfEntities())));
		

        mainController.generateChartDataOfType(measurementType, aggregationType);


        mainController.sortChartData(Constants.SortingType.BIRTH_ASCENDING);
		
		ObservableList<ChartGroupPhaseMeasurement> observableList = mainController.getChartData();
		
		List<Phase> phases = mainController.getPhases();
		
		List<PatternData> actualPatternList = patternManager.getPatterns(observableList, phases, Constants.PatternType.NO_TYPE);
		
		initializeExpectedPatterns();
		/*
		 * System.out.println("\n"); System.out.println("Testt"); for (var pattern :
		 * actualPatternList) { System.out.println(pattern.getPatternType().toString());
		 * 
		 * if (pattern.getPatternCellsList().size() > 0) { for (var item :
		 * pattern.getPatternCellsList()) {
		 * 
		 * System.out.println("Entity Name : " + item.getEntityName() + " PhaseId: " +
		 * item.getPhaseId());
		 * 
		 * } } System.out.println("\n"); }
		 */
		
		for(int i = 0; i < actualPatternList.size(); i++){
			PatternData expectedPatternData = expectedPatternList.get(i);
			PatternData actualPatternData = actualPatternList.get(i);
			
			Assertions.assertAll(
                    () -> Assertions.assertEquals(expectedPatternData.getPatternType(), actualPatternData.getPatternType()),
                    () -> testCellsList(expectedPatternData.getPatternCellsList(), actualPatternData.getPatternCellsList())
            );
        }

	}
	
	
	public void testCellsList(List<CellInfo> expectedCells, List<CellInfo> actualCells) {
		Assumptions.assumeTrue(expectedCells.size() == actualCells.size());
		
        for (int i=0; i < expectedCells.size(); i++) {
        	CellInfo expectedCell = expectedCells.get(i);
        	CellInfo actualCell = actualCells.get(i);
        	
        	testCell(expectedCell, actualCell);
        }
	}
	
	public void testCell(CellInfo expectedCell, CellInfo actualCell) {
        Assertions.assertAll(
                () -> Assertions.assertEquals(expectedCell.getPhaseId(), actualCell.getPhaseId()),
                () -> Assertions.assertEquals(expectedCell.getEntityName(), actualCell.getEntityName())
        );
    }
	
	
	//The initialization is based on _threshold = 3, if that change the data should change too
	public void initializeExpectedPatterns() {
    	List<CellInfo> expected_multiple_Births1 = new ArrayList<CellInfo>() {private static final long serialVersionUID = 1L;

		{
    		add(new CellInfo("biodatabase", 0));
    		add(new CellInfo("bioentry", 0));
    		add(new CellInfo("bioentry_date", 0));
    		add(new CellInfo("bioentry_description", 0));
    		add(new CellInfo("bioentry_direct_links", 0));
    		add(new CellInfo("bioentry_keywords", 0));
    		add(new CellInfo("bioentry_reference", 0));
    		add(new CellInfo("bioentry_taxa", 0));
    		add(new CellInfo("biosequence", 0));
    		add(new CellInfo("cache_corba_support", 0));
    		add(new CellInfo("comment", 0));
    		add(new CellInfo("location_qualifier_value", 0));
    		add(new CellInfo("reference", 0));
    		add(new CellInfo("remote_seqfeature_name", 0));
    		add(new CellInfo("seqfeature", 0));
    		add(new CellInfo("seqfeature_key", 0));
    		add(new CellInfo("seqfeature_location", 0));
    		add(new CellInfo("seqfeature_qualifier", 0));
    		add(new CellInfo("seqfeature_qualifier_value", 0));
    		add(new CellInfo("seqfeature_source", 0));
    		add(new CellInfo("taxa", 0));
        }};
        patternData = new PatternData(PatternType.MULTIPLE_BIRTHS,expected_multiple_Births1);
        
        expectedPatternList.add(patternData);
        
        
        List<CellInfo> expected_multiple_Deaths = new ArrayList<CellInfo>() {private static final long serialVersionUID = 1L;

		{
        	add(new CellInfo("bioentry_date", 2));
    		add(new CellInfo("bioentry_description", 2));
    		add(new CellInfo("bioentry_keywords", 2));
    		add(new CellInfo("seqfeature_key", 2));
    		add(new CellInfo("seqfeature_qualifier", 2));
        }};
        
        
        patternData = new PatternData(PatternType.MULTIPLE_DEATHS,expected_multiple_Deaths);
        expectedPatternList.add(patternData);
        
        List<CellInfo> expected_multiple_Births2 = new ArrayList<CellInfo>() {private static final long serialVersionUID = 1L;

		{
        	add(new CellInfo("bioentry_qualifier_value", 3));
        	add(new CellInfo("dbxref", 3));
        	add(new CellInfo("dbxref_qualifier_value", 3));
        	add(new CellInfo("ontology_term", 3));
        }};
        patternData = new PatternData(PatternType.MULTIPLE_BIRTHS,expected_multiple_Births2);
        expectedPatternList.add(patternData);
        

        
        List<CellInfo> expected_multiple_Updates1 = new ArrayList<CellInfo>() {private static final long serialVersionUID = 1L;

		{
        	add(new CellInfo("bioentry_direct_links", 3));
        	add(new CellInfo("biosequence", 3));
    		add(new CellInfo("location_qualifier_value", 3));
    		add(new CellInfo("reference", 3));
    		add(new CellInfo("seqfeature_qualifier_value", 3));
        }};
        patternData = new PatternData(PatternType.MULTIPLE_UPDATES,expected_multiple_Updates1);
        expectedPatternList.add(patternData);
        
        List<CellInfo> expected_multiple_Updates2 = new ArrayList<CellInfo>() {private static final long serialVersionUID = 1L;

		{
        	add(new CellInfo("bioentry", 10));
        	add(new CellInfo("bioentry_reference", 10));
        	add(new CellInfo("biosequence", 10));
        	add(new CellInfo("comment", 10));
        	add(new CellInfo("location_qualifier_value", 10));
        	add(new CellInfo("reference", 10));
        	add(new CellInfo("remote_seqfeature_name", 10));
        	add(new CellInfo("seqfeature", 10));
        	add(new CellInfo("seqfeature_location", 10));
            add(new CellInfo("seqfeature_qualifier_value", 10));
            add(new CellInfo("bioentry_qualifier_value", 10));
            add(new CellInfo("dbxref_qualifier_value", 10));
            add(new CellInfo("ontology_term", 10));
            add(new CellInfo("seqfeature_relationship", 10));
        }};
        patternData = new PatternData(PatternType.MULTIPLE_UPDATES,expected_multiple_Updates2);
        expectedPatternList.add(patternData);
        
        List<CellInfo> expected_multiple_Births3 = new ArrayList<CellInfo>() {private static final long serialVersionUID = 1L;

		{
        	add(new CellInfo("ontology", 21));
    		add(new CellInfo("ontology_dbxref", 21));
    		add(new CellInfo("ontology_path", 21));
    		add(new CellInfo("ontology_relationship", 21));
    		add(new CellInfo("taxon_name", 21));
        }};
        patternData = new PatternData(PatternType.MULTIPLE_BIRTHS,expected_multiple_Births3);
        expectedPatternList.add(patternData);
        
        List<CellInfo> expected_multiple_Updates3 = new ArrayList<CellInfo>() {private static final long serialVersionUID = 1L;

		{
        	add(new CellInfo("bioentry",21));
        	add(new CellInfo("bioentry_reference", 21));
        	add(new CellInfo("biosequence", 21));
        	add(new CellInfo("comment",21));
        	add(new CellInfo("location_qualifier_value", 21));
        	add(new CellInfo("reference", 21));
        	add(new CellInfo("seqfeature", 21));
        	add(new CellInfo("seqfeature_location", 21));
        	add(new CellInfo("seqfeature_qualifier_value", 21));
        	add(new CellInfo("bioentry_qualifier_value", 21));
        	add(new CellInfo("dbxref", 21));
            add(new CellInfo("dbxref_qualifier_value",21));
            add(new CellInfo("ontology_term", 21));
            add(new CellInfo("seqfeature_relationship", 21));
            add(new CellInfo("taxon", 21));
            add(new CellInfo("bioentry_relationship", 21));
        }};
        patternData = new PatternData(PatternType.MULTIPLE_UPDATES,expected_multiple_Updates3);
        expectedPatternList.add(patternData);
        
        List<CellInfo> expected_multiple_Updates4 = new ArrayList<CellInfo>() {private static final long serialVersionUID = 1L;

		{
        	add(new CellInfo("biodatabase", 23));
        	add(new CellInfo("bioentry", 23));
        	add(new CellInfo("biosequence", 23));
        	add(new CellInfo("seqfeature", 23));
        	add(new CellInfo("taxon", 23));
        	add(new CellInfo("taxon_name", 23));
        }};
        patternData = new PatternData(PatternType.MULTIPLE_UPDATES,expected_multiple_Updates4);
        expectedPatternList.add(patternData);
        
        List<CellInfo> expected_multiple_Updates5 = new ArrayList<CellInfo>() {private static final long serialVersionUID = 1L;

		{
        	add(new CellInfo("biosequence", 24));
        	add(new CellInfo("dbxref_qualifier_value", 24));
        	add(new CellInfo("ontology_relationship", 24));
        	add(new CellInfo("bioentry_dbxref", 24));
        }};
        patternData = new PatternData(PatternType.MULTIPLE_UPDATES,expected_multiple_Updates5);
        expectedPatternList.add(patternData);
        
        List<CellInfo> expected_multiple_Updates6 = new ArrayList<CellInfo>() {private static final long serialVersionUID = 1L;

		{
        	add(new CellInfo("location_qualifier_value", 27));
        	add(new CellInfo("seqfeature_qualifier_value", 27));
        	add(new CellInfo("bioentry_qualifier_value", 27));
        	add(new CellInfo("dbxref_qualifier_value", 27));
        	add(new CellInfo("seqfeature_relationship", 27));
        	add(new CellInfo("bioentry_relationship", 27));
        	add(new CellInfo("bioentry_path", 27));
        	add(new CellInfo("seqfeature_path", 27));
        }};
        patternData = new PatternData(PatternType.MULTIPLE_UPDATES,expected_multiple_Updates6);
        expectedPatternList.add(patternData);
        
        List<CellInfo> expected_multiple_Updates7 = new ArrayList<CellInfo>() {private static final long serialVersionUID = 1L;

		{
        	add(new CellInfo("seqfeature_relationship", 33));
        	add(new CellInfo("bioentry_relationship", 33));
        	add(new CellInfo("bioentry_path", 33));
        	add(new CellInfo("seqfeature_path", 33));
        }};
        patternData = new PatternData(PatternType.MULTIPLE_UPDATES,expected_multiple_Updates7);
        expectedPatternList.add(patternData);
        
        List<CellInfo> expected_ladder1 = new ArrayList<CellInfo>() {private static final long serialVersionUID = 1L;

		{
        	add(new CellInfo("biodatabase", 0));
        	add(new CellInfo("bioentry", 0));
        	add(new CellInfo("bioentry_date", 0));
        	add(new CellInfo("bioentry_description", 0));
        	add(new CellInfo("bioentry_direct_links", 0));
        	add(new CellInfo("bioentry_keywords", 0));
        	add(new CellInfo("bioentry_reference", 0));
        	add(new CellInfo("bioentry_taxa", 0));
        	add(new CellInfo("biosequence", 0));
        	add(new CellInfo("cache_corba_support", 0));
        	add(new CellInfo("comment", 0));
        	add(new CellInfo("location_qualifier_value", 0));
        	add(new CellInfo("reference", 0));
        	add(new CellInfo("remote_seqfeature_name", 0));
        	add(new CellInfo("seqfeature", 0));
        	add(new CellInfo("seqfeature_key", 0));
        	add(new CellInfo("seqfeature_location", 0));
        	add(new CellInfo("seqfeature_qualifier", 0));
        	add(new CellInfo("seqfeature_qualifier_value", 0));
        	add(new CellInfo("seqfeature_source", 0));
        	add(new CellInfo("taxa", 0));
        	add(new CellInfo("bioentry_qualifier_value", 3));
        	add(new CellInfo("dbxref", 3));
        	add(new CellInfo("dbxref_qualifier_value", 3));
        	add(new CellInfo("ontology_term", 3));
        	add(new CellInfo("seqfeature_relationship", 5));
        }};
        patternData = new PatternData(PatternType.LADDER,expected_ladder1);
        expectedPatternList.add(patternData);
        
        List<CellInfo> expected_ladder2 = new ArrayList<CellInfo>() {private static final long serialVersionUID = 1L;

		{
        	add(new CellInfo("ontology", 21));
        	add(new CellInfo("ontology_dbxref", 21));
        	add(new CellInfo("ontology_path", 21));
        	add(new CellInfo("ontology_relationship", 21));
        	add(new CellInfo("taxon_name", 21));
        	add(new CellInfo("bioentry_dbxref", 23));
        	add(new CellInfo("bioentry_path", 24));
        	add(new CellInfo("seqfeature_path", 24));
        	add(new CellInfo("seqfeature_dbxref", 26));
        	add(new CellInfo("location", 27));
        	add(new CellInfo("term", 27));
        	add(new CellInfo("term_dbxref", 27));
        	add(new CellInfo("term_path", 28));
        	add(new CellInfo("term_relationship", 28));
        	add(new CellInfo("term_synonym", 29));
        }};
        patternData = new PatternData(PatternType.LADDER,expected_ladder2);
        expectedPatternList.add(patternData);

    }
}
