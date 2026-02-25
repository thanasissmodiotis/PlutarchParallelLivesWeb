package daintiness.models;

import java.util.List;

import daintiness.utilities.Constants;

public class PatternData {
	private Constants.PatternType PatternType;
	private List<CellInfo> PatternCellInfoList;
	
	public PatternData(Constants.PatternType patternType, List<CellInfo> patternCellInfoList) {
        this.PatternType = patternType;
        this.PatternCellInfoList = patternCellInfoList;
    }
	
	public Constants.PatternType getPatternType() {
        return PatternType;
    }

    public List<CellInfo> getPatternCellsList() {
        return PatternCellInfoList;
    }
    
    public CellInfo getFirstCellOfPattern() {
    	return PatternCellInfoList.get(0);
    }
    
    public CellInfo getLastCellOfPattern() {
    	return PatternCellInfoList.get(PatternCellInfoList.size() - 1);
    }
    
    public CellInfo getCellByEntityAndPhaseId(String entityName, int phaseId) {
    	
    	for(CellInfo cell: PatternCellInfoList) {
    		if(cell.getEntityName() == entityName && cell.getPhaseId() == phaseId) {
    			return cell;
    		}
    	}
    	return null;
    }
    
    public void setPatternData(Constants.PatternType patternType,List<CellInfo> patternCellInfoList) {
    	this.PatternType = patternType;
    	this.PatternCellInfoList = patternCellInfoList;
    }

    
    @Override
    public String toString() {
        return  "Pattern Type: " + PatternType.toString() +
                ", First cell of pattern: " + PatternCellInfoList.get(0) +
                ", Last cell of pattern: " + PatternCellInfoList.get(PatternCellInfoList.size() - 1);
    }
}
