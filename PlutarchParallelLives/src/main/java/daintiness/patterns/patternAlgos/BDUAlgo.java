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

public class BDUAlgo {

	public static List<PatternData> FindPattern(ObservableList<ChartGroupPhaseMeasurement> totalValues, List<Phase> totalPhases, int threshold, PatternType patternType){
		List<PatternData> patternList = new ArrayList<PatternData>();
		for(Phase phase: totalPhases) {  		
    		List<CellInfo> cellsMultipleBirths = new ArrayList<CellInfo>();
    		List<CellInfo> cellsMultipleUpdates = new ArrayList<CellInfo>();
    		List<CellInfo> cellsMultipleDeaths = new ArrayList<CellInfo>();
    		
    		for(ChartGroupPhaseMeasurement tab: totalValues) {
    			
    			var cellMeasurementType = tab.getEntityGroup().getGPMType(phase.getFirstPhaseBeat().getBeatId(), phase.getLastPhaseBeat().getBeatId());    			
    			
    			int phaseId = phase.getPhaseId();
    			String entityName = tab.getEntityGroup().getGroupComponentsNames().get(0);
    			
    			CellInfo cell = new CellInfo(entityName, phaseId);
    			
    			if(cellMeasurementType == Constants.GPMType.BIRTH){
    				cellsMultipleBirths.add(cell);
    			}
    			else if(cellMeasurementType == Constants.GPMType.ACTIVE && tab.getMeasurement(phase.getPhaseId()) != null) {			
    				cellsMultipleUpdates.add(cell);
    			}
    			else if(cellMeasurementType == Constants.GPMType.DEATH) {
    				cellsMultipleDeaths.add(cell);
    			}

            }
    		
    		
    		PatternData patternMultipleBirths = new PatternData(Constants.PatternType.MULTIPLE_BIRTHS,cellsMultipleBirths);
    		PatternData patternMultipleUpdates = new PatternData(Constants.PatternType.MULTIPLE_UPDATES,cellsMultipleUpdates);
    		PatternData patternMultipleDeaths = new PatternData(Constants.PatternType.MULTIPLE_DEATHS,cellsMultipleDeaths);
    		
    		if(patternType == PatternType.MULTIPLE_BIRTHS) {
    			if(cellsMultipleBirths.size() > threshold) {
        			patternList.add(patternMultipleBirths);        			
        		}
    		}
    		
    		else if(patternType == PatternType.MULTIPLE_UPDATES) {
    			if(cellsMultipleUpdates.size() > threshold){
        			patternList.add(patternMultipleUpdates);
        		}
    		}
    		
    		else if(patternType == PatternType.MULTIPLE_DEATHS) {
    			if(cellsMultipleDeaths.size() > threshold){    			
        			patternList.add(patternMultipleDeaths);
        		}  
    		}
    		else {
    			if(cellsMultipleBirths.size() > threshold) {
        			patternList.add(patternMultipleBirths);        			
        		}
    			if(cellsMultipleUpdates.size() > threshold){
        			patternList.add(patternMultipleUpdates);
        		}
    			if(cellsMultipleDeaths.size() > threshold){    			
        			patternList.add(patternMultipleDeaths);
        		}  
    		}	    		
    	}
		
		return patternList;
	}
}
