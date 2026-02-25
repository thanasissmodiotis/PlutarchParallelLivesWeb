package daintiness.patterns;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import daintiness.clustering.Phase;
import daintiness.clustering.measurements.ChartGroupPhaseMeasurement;
import daintiness.models.*;
import daintiness.patterns.patternAlgos.IPatternComputationHandler;
import daintiness.patterns.patternAlgos.PatternComputationHandlerFactory;
import daintiness.utilities.Constants.PatternType;
import javafx.collections.ObservableList;

public class PatternManager implements IPatternManager {
	private IPatternComputationHandler patternComputationHandler;
	private Long patternsComputationTime;
	private int numberOfTotalRows;
	private int numberOfTotalColumns;
	

	public List<PatternData> getPatterns(ObservableList<ChartGroupPhaseMeasurement> totalValues, List<Phase> totalPhases, PatternType patternType) {
		
		PatternComputationHandlerFactory patternComputationHandlerFactory = new PatternComputationHandlerFactory();
		patternComputationHandler = patternComputationHandlerFactory.getPatternComputationHandler("SIMPLE_PATTERN_COMPUTATION_HANDLER");
		
		//Data for printing purposes
		numberOfTotalRows = totalValues.size();
		numberOfTotalColumns = totalPhases.size();
		
		
		List<PatternData> patternList = new ArrayList<PatternData>();
		
		Long start = System.nanoTime();
		patternList = patternComputationHandler.computePatterns(totalValues, totalPhases, patternType);
		Long end = System.nanoTime();
		
		patternsComputationTime = end - start;

        return patternList;
	}

	public void printPatterns(List<PatternData> patternList, File file, String projectName) {
		try {
			FileWriter fileWriter = new FileWriter(file);
			fileWriter.write("Project Name:\t" + projectName + "\n");  
			
			
			int birthsPatterns = 0;
			int deathsPatterns = 0;
			int updatesPatterns = 0;
			int ladderPatterns = 0;
			
			
			List<String> allEntities = new ArrayList<String>();
			List<Integer> allPhases = new ArrayList<Integer>();
			
			String patternDataInfo = "";
			
			for (var pattern : patternList) {
				
				patternDataInfo+=pattern.getPatternType().toString() + "\n";
				
				if(pattern.getPatternType() == PatternType.MULTIPLE_BIRTHS) {
					birthsPatterns+=1;
				
				}
				else if(pattern.getPatternType() == PatternType.MULTIPLE_DEATHS) {
					deathsPatterns+=1;
				
				}
				else if(pattern.getPatternType() == PatternType.MULTIPLE_UPDATES) {
					updatesPatterns+=1;
				
				}
				else if(pattern.getPatternType() == PatternType.LADDER) {
					ladderPatterns+=1;
				
				}
				
				if (pattern.getPatternCellsList().size() > 0) {
					
					patternDataInfo+="The pattern consists of " + pattern.getPatternCellsList().size() +" cells\n";
					
					for (var item : pattern.getPatternCellsList()) {
						allEntities.add(item.getEntityName());
						allPhases.add(item.getPhaseId());
						
						patternDataInfo+="Entity Name : " + item.getEntityName() + " PhaseId: " + item.getPhaseId() + "\n";
					}
				}
				patternDataInfo+="\n";
				
			}
			
			List<String> distinctAllEntities = allEntities.stream().distinct().collect(Collectors.toList());
			List<Integer> distinctAllPhases = allPhases.stream().distinct().collect(Collectors.toList());
			
			fileWriter.write(projectName + "\tNumber of columns:\t" + numberOfTotalColumns + "\n");
			fileWriter.write(projectName + "\tNumber of rows:\t" + numberOfTotalRows + "\n");
			
			fileWriter.write(projectName + "\tNumber of columns that participate in patterns:\t" + distinctAllPhases.size() + "\n");
			fileWriter.write(projectName + "\tNumber of rows that participate in patterns:\t" + distinctAllEntities.size() + "\n");
			
			fileWriter.write(projectName + "\tNumber of total patterns:\t" + patternList.size() + "\n"); 
			fileWriter.write(projectName + "\tNumber of births patterns:\t" + birthsPatterns + "\n");
			fileWriter.write(projectName + "\tNumber of deaths patterns:\t" + deathsPatterns + "\n");

			fileWriter.write(projectName + "\tNumber of updates patterns:\t" + updatesPatterns + "\n");

			fileWriter.write(projectName + "\tNumber of ladder patterns:\t" + ladderPatterns + "\n");

			if(patternList.size() > 0) {
				double patternsComputationTimeSeconds = (double) patternsComputationTime / 1_000_000_000;
				
				fileWriter.write(projectName + "\tPatterns computation(sec):\t" + patternsComputationTimeSeconds + "\n");
			}
			else {
				fileWriter.write(projectName + "\tPatterns computation(sec):\t0\n");
			}
						
			fileWriter.write("\n");
			/*
			 * for (var pattern : patternList) {
			 * fileWriter.write(pattern.getPatternType().toString() + "\n");
			 * 
			 * if (pattern.getPatternCellsList().size() > 0) {
			 * fileWriter.write("The pattern consists of " +
			 * pattern.getPatternCellsList().size() +" cells\n"); for (var item :
			 * pattern.getPatternCellsList()) { fileWriter.write("Entity Name : " +
			 * item.getEntityName() + " PhaseId: " + item.getPhaseId() + "\n"); } }
			 * fileWriter.write("\n");
			 * 
			 * }
			 */
						
			fileWriter.write(patternDataInfo);
			
			fileWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
