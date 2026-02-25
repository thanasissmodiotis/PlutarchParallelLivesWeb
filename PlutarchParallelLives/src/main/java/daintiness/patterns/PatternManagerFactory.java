package daintiness.patterns;


public class PatternManagerFactory {
	public IPatternManager getPatternManager(String type) {
        if (type.isBlank()) {
            return null;
        }

        if (type.equals("SIMPLE_PATTERN_MANAGER")) {
            return new PatternManager();
        }
        return null;
    }
}

