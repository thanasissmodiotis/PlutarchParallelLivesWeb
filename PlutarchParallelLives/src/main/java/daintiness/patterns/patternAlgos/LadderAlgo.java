package daintiness.patterns.patternAlgos;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import daintiness.models.CellInfo;
import daintiness.models.PatternData;
import daintiness.utilities.Constants;

public class LadderAlgo {
	
	public static List<PatternData> FindPattern(List<List<CellInfo>> listOfBirthsEachPhase, List<String> TotalEntitiesNamesAscOrder, int threshold){
		List<PatternData> patternList = new ArrayList<PatternData>();
		List<CellInfo> cellsLadderPattern = new ArrayList<CellInfo>();
		int totalCellsInRow = 1;
		
		for(int j = 0; j < listOfBirthsEachPhase.size() - 1;j++) {  
			int currentPhaseCellsSize = listOfBirthsEachPhase.get(j).size();

    		CellInfo currentLastCell = listOfBirthsEachPhase.get(j).get(currentPhaseCellsSize-1);
    		CellInfo nextfirstCell = listOfBirthsEachPhase.get(j+1).get(0);
    		
    		int currentCellPhaseId = currentLastCell.getPhaseId();

    		int difPhases = nextfirstCell.getPhaseId() - currentCellPhaseId;

    		//To achieve the Ladder pattern we need to rearrange the table based on the ascending order of births, so we have to find the new position of the entities in the table.
    		int currentCellEntityNameIndex = TotalEntitiesNamesAscOrder.indexOf(currentLastCell.getEntityName());
			int nextCellEntityNameIndex = TotalEntitiesNamesAscOrder.indexOf(nextfirstCell.getEntityName());
			
			int difEntities = nextCellEntityNameIndex - currentCellEntityNameIndex;

			if (difPhases <= 3 && difEntities <= 3 ) {
				for(CellInfo cell: listOfBirthsEachPhase.get(j)) {
					cellsLadderPattern.add(cell);
				}
				
				for(CellInfo cell: listOfBirthsEachPhase.get(j+1)) {
					cellsLadderPattern.add(cell);
				}

				totalCellsInRow = totalCellsInRow + 1;
				
			}
			else {
				List<CellInfo> distinctCellsLadderPattern = cellsLadderPattern.stream().distinct().collect(Collectors.toList());
		    	if(totalCellsInRow >= threshold) {
					PatternData patternLadder = new PatternData(Constants.PatternType.LADDER, distinctCellsLadderPattern);
					patternList.add(patternLadder);

				}
		    	totalCellsInRow = 1;
				cellsLadderPattern = new ArrayList<CellInfo>();
			}
			
    	}
		List<CellInfo> distinctCellsLadderPattern = cellsLadderPattern.stream().distinct().collect(Collectors.toList());
    	if(totalCellsInRow >= threshold) {
			PatternData patternLadder = new PatternData(Constants.PatternType.LADDER, distinctCellsLadderPattern);
			patternList.add(patternLadder);
		}
    	return patternList;
	}
}
