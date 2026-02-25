package daintiness.patterns.patternAlgos;

public class PatternComputationHandlerFactory {
	public IPatternComputationHandler getPatternComputationHandler(String type) {
        if (type.isBlank()) {
            return null;
        }

        if (type.equals("SIMPLE_PATTERN_COMPUTATION_HANDLER")) {
            return new PatternComputationHandler();
        }
        return null;
    }
}

