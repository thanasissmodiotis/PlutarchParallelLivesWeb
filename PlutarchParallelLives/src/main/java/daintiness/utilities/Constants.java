package daintiness.utilities;

import java.io.File;

public class Constants {
    public static final String FS = File.separator;

    public enum FileType {
        CSV, TSV, SCHEMA_EVO, TEM_GPM
    }

    public enum RowFormatType {
        NORMAL, MULTI_WORDS, ENTITY_INFO
    }

    public enum SortingType {
        ACTIVITY_ASCENDING,
        ACTIVITY_DESCENDING,
        BIRTH_ASCENDING,
        BIRTH_DESCENDING,
        LIFE_DURATION_ASCENDING,
        LIFE_DURATION_DESCENDING
    }

    public enum MeasurementType {
        DELTA_VALUE,
        RAW_VALUE
    }

    public enum AggregationType {
        SUM_OF_INSERTIONS,
        SUM_OF_DELETIONS,
        SUM_OF_UPDATES,
        SUM_OF_INSERTIONS_AND_DELETIONS,
        SUM_OF_INSERTIONS_AND_UPDATES,
        SUM_OF_DELETIONS_AND_UPDATES,
        SUM_OF_ALL,
        NO_AGGREGATION
    }

    public enum GPMType {
        BIRTH,
        DEATH,
        ACTIVE,
        INACTIVE
    }

    public enum TransactionType {
        INSERTION,
        DELETION,
        UPDATE,
        NOT_SUPPORTED
    }

    public enum SelectedCellType {
        ENTITY_GROUP,
        PHASE,
        MEASUREMENT,
        DEFAULT
    }
    
    public enum PatternType
    {
    	NO_TYPE,
        MULTIPLE_BIRTHS,
        MULTIPLE_UPDATES,
        MULTIPLE_DEATHS,
        LADDER, BIRTH
    }
    
    
}
