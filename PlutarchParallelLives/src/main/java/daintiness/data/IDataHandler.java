package daintiness.data;

import java.util.List;

import daintiness.models.Beat;
import daintiness.models.Entity;
import daintiness.models.TimeEntityMeasurements;
import daintiness.utilities.Constants;

public interface IDataHandler {

    /**
     * Returns an Entity by entering its index.
     *
     * @param index position of the desired Entity
     * @return the Entity that maps to the given index
     */
    Entity getEntity(int index);


    /**
     * Returns an Entity by entering its entityName
     *
     * @param entityName Name of the desired Entity
     * @return the Entity that maps to the given entityName
     */
    Entity getEntityByName(String entityName);


    /**
     * Returns a Beat by its index
     * (The index is also the beat's id)
     * @param index position of the desired Beat
     * @return the Beat that maps to the given index
     */
    Beat getBeat(int index);


    /**
     * Set the fileType of the loaded data
     * @param type fileType of the loaded data
     */
    void setType(Constants.FileType type);


    /**
     * Returns the loaded data type
     * @return fileType of loaded data
     */
    Constants.FileType getType();


    /**
     * Returns a sorted list that contains all the dataset's beats
     * @return List of Beats
     */
    java.util.List<Beat> getTimeline();


    /**
     * Returns a list that contains all the dataset's entities
     * @return List of Entities
     */
    java.util.List<Entity> getPopulation();


    /**
     * Returns a Map from entityName to (beatId to TimeEntityMeasurement) Map
     * @return Map from entityName to (beatId to TimeEntityMeasurement) Map
     */
    java.util.Map<String, java.util.Map<Integer,TimeEntityMeasurements>> getEntityNameToTEMMap();


    /**
     * Returns a Map from beatId to (entityName to TimeEntityMeasurement) Map
     * @return Map from beatId to (entityName to TimeEntityMeasurement) Map
     */
    java.util.Map<Integer, java.util.Map<String, TimeEntityMeasurements>> getBeatIdToTEMMap();


    /**
     * Returns a TimeEntityMeasurement by specifying the entity and the beat
     * @param entity Entity that contains the tem
     * @param beat Beat that contains the tem
     * @return TimeEntityMeasurement
     */
    TimeEntityMeasurements getTem(Entity entity, Beat beat);

    /**
     * Converts the whole intermediate representation to String.
     * @return Intermediate representation as string.
     */
    String getTimeEntityMeasurementAsString();


    /**
     * It works like a constructor of the DataHandler.
     * <p/>
     * It uses the basic data structures to initialize the extra data structures
     * that are needed.
     * <p/>
     * It's used when we load the intermediate representation
     * from file.
     * @param timeline List of Beats
     * @param population List of Entities
     * @param measurementsList List of TimeEntityMeasurements
     */
    void init(List<Beat> timeline, List<Entity> population, List<TimeEntityMeasurements> measurementsList);


    /**
     * @return Number of TimeEntityMeasurements (non-empty cells)
     */
    int getNumberOfTEMs();
}
