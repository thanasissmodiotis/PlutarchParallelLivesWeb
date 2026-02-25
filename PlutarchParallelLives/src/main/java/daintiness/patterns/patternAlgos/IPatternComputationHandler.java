package daintiness.patterns.patternAlgos;

import java.util.List;

import daintiness.clustering.Phase;
import daintiness.clustering.measurements.ChartGroupPhaseMeasurement;
import daintiness.models.PatternData;
import daintiness.utilities.Constants.PatternType;
import javafx.collections.ObservableList;

public interface IPatternComputationHandler {
	List<PatternData> computePatterns(ObservableList<ChartGroupPhaseMeasurement> totalValues, List<Phase> totalPhases, PatternType patternType);
}
