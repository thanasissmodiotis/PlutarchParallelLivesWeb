package daintiness.io.input.schemaevo;

import daintiness.utilities.Constants.AggregationType;

public class TableBeatMetrics {
    private int numberOfInsertions;
    private int numberOfDeletions;
    private int numberOfUpdates;

    public TableBeatMetrics() {
        numberOfInsertions = 0;
        numberOfDeletions = 0;
        numberOfUpdates = 0;
    }

    public int getNumberOfTableBeatEvents() {
        return numberOfInsertions + numberOfDeletions + numberOfUpdates;
    }

    @Override
    public String toString() {
        return "TableBeatMetrics{" +
                "numberOfInsertions=" + numberOfInsertions +
                ", numberOfDeletions=" + numberOfDeletions +
                ", numberOfUpdates=" + numberOfUpdates +
                '}';
    }

    public void addInsertion() {
        numberOfInsertions++;
    }

    public void addDeletion() {
        numberOfDeletions++;
    }

    public void addUpdate() {
        numberOfUpdates++;
    }

    public double getValueByAggregationType(AggregationType aggregationType) {
        double value = 0;

        if (aggregationType != AggregationType.SUM_OF_DELETIONS &&
                aggregationType != AggregationType.SUM_OF_UPDATES &&
                aggregationType != AggregationType.SUM_OF_DELETIONS_AND_UPDATES) {
            value += numberOfInsertions;
        }

        if (aggregationType != AggregationType.SUM_OF_INSERTIONS &&
                aggregationType != AggregationType.SUM_OF_UPDATES &&
                aggregationType != AggregationType.SUM_OF_INSERTIONS_AND_UPDATES) {
            value += numberOfDeletions;
        }

        if (aggregationType != AggregationType.SUM_OF_INSERTIONS &&
                aggregationType != AggregationType.SUM_OF_DELETIONS &&
                aggregationType != AggregationType.SUM_OF_INSERTIONS_AND_DELETIONS){
            value += numberOfUpdates;
        }

        return value;
    }


    public int getNumberOfInsertions() {
        return this.numberOfInsertions;
    }

    public int getNumberOfDeletions() {
        return this.numberOfDeletions;
    }

    public int getNumberOfUpdates() {
        return this.numberOfUpdates;
    }
}
