package daintiness.patterns;

import java.io.File;
import java.util.List;

import daintiness.clustering.Phase;
import daintiness.clustering.measurements.ChartGroupPhaseMeasurement;
import daintiness.models.PatternData;
import daintiness.utilities.Constants.PatternType;
import javafx.collections.ObservableList;


public interface IPatternManager {

	List<PatternData> getPatterns(ObservableList<ChartGroupPhaseMeasurement> totalValues, List<Phase> phases, PatternType patternType);
	
	void printPatterns(List<PatternData> patternDataList, File file, String projectName);
}
