package daintiness.patterns.patternAlgos;

import java.util.ArrayList;
import java.util.List;

import daintiness.clustering.Phase;
import daintiness.clustering.measurements.ChartGroupPhaseMeasurement;
import daintiness.models.CellInfo;
import daintiness.models.PatternData;
import daintiness.utilities.Constants;
import daintiness.utilities.Constants.PatternType;
import javafx.collections.ObservableList;

public class PatternComputationHandler implements IPatternComputationHandler{
	
	private final int _threshold = 3;
	private List<List<CellInfo>> listOfBirthsEachPhase = new ArrayList<List<CellInfo>>();
	private List<String> TotalEntitiesNamesAscOrder = new ArrayList<String>(); 
	
	public List<PatternData> computePatterns(ObservableList<ChartGroupPhaseMeasurement> totalValues, List<Phase> totalPhases, PatternType patternType){
		
		preProcessing(totalValues, totalPhases);
		List<PatternData> listToReturn = new ArrayList<PatternData>();
		
		if(patternType == PatternType.LADDER) {
			listToReturn = LadderAlgo.FindPattern(listOfBirthsEachPhase, TotalEntitiesNamesAscOrder, _threshold);
		}
		else if(patternType == PatternType.MULTIPLE_BIRTHS) {
			listToReturn = BDUAlgo.FindPattern(totalValues, totalPhases, _threshold, PatternType.MULTIPLE_BIRTHS);
		}
		else if(patternType == PatternType.MULTIPLE_UPDATES) {
			listToReturn = BDUAlgo.FindPattern(totalValues, totalPhases, _threshold, PatternType.MULTIPLE_UPDATES);
		}
		else if(patternType == PatternType.MULTIPLE_DEATHS) {
			listToReturn = BDUAlgo.FindPattern(totalValues, totalPhases, _threshold, PatternType.MULTIPLE_DEATHS);
		}
		else {
			listToReturn.addAll(BDUAlgo.FindPattern(totalValues, totalPhases, _threshold, PatternType.NO_TYPE));
			listToReturn.addAll(LadderAlgo.FindPattern(listOfBirthsEachPhase, TotalEntitiesNamesAscOrder, _threshold));
		}
		
		return listToReturn;
		
	}
	
	private void preProcessing(ObservableList<ChartGroupPhaseMeasurement> totalValues, List<Phase> totalPhases){

		for(Phase phase: totalPhases) {  		
    		List<CellInfo> cellsMultipleBirths = new ArrayList<CellInfo>();
    		
    		for(ChartGroupPhaseMeasurement tab: totalValues) {
    			
    			var cellMeasurementType = tab.getEntityGroup().getGPMType(phase.getFirstPhaseBeat().getBeatId(), phase.getLastPhaseBeat().getBeatId());    			
    			
    			int phaseId = phase.getPhaseId();
    			String entityName = tab.getEntityGroup().getGroupComponentsNames().get(0);
    			
    			
    			if(phaseId == 0) {
    				TotalEntitiesNamesAscOrder.add(entityName);
    			}
    			
    			CellInfo cell = new CellInfo(entityName, phaseId);
    			
    			if(cellMeasurementType == Constants.GPMType.BIRTH){
    				cellsMultipleBirths.add(cell);
    			}
            }		
    		if(cellsMultipleBirths.size() > 0) {
    			listOfBirthsEachPhase.add(cellsMultipleBirths);
    		}    		    		
    	}
	}
}


