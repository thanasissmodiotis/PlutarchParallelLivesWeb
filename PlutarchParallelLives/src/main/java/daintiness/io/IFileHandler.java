package daintiness.io;

import java.io.File;
import java.util.List;
import java.util.Map;

import daintiness.data.IDataHandler;
import daintiness.utilities.Constants;

public interface IFileHandler {

    /**
     * This is the method initializes the suitable, for the given type of file, loader.<br>
     * The loader returns the Intermediate Representation of the file.
     * @return IDataHandler an intermediate representation
     */
    IDataHandler loadTEM();

    /**
     * The list element index is equal to the phase's id that the components belong to. <br>
     * Every int Array consists of the the PhaseComponents (Beats) IDs<br>
     * @return List of int Arrays that represent the phaseComponents (Beats) IDs.
     */
    List<int[]> getPhasesData();


    /**
     * Key(Integer): EntityGroupId <br>
     * Value(String[]): Array with the EntityNames that the EntityGroup consists of.<br>
     * @return Map entityGroupIds to EntityGroupComponentsNames Array
     */
    Map<Integer, String[]> getEntityGroupData();


    /**
     * Key(Integer): EntityGroupId <br>
     * Value(Map):
     * -->Key(Integer): PhaseId <br>
     * -->Value(Double): GroupPhaseMeasurement value<br>
     * @return Map (EntityGroupId, PhaseId) -> GroupPhaseMeasurement
     */
    Map<Integer, Map<Integer, Double>> getLoadedMeasurementMap();


    /**
     * This is a utility method that finds the necessary files <br>
     * when the type is TEM_GPM.
     */
    void parseProjectInfo();


    /**
     * @param projectFile File of the output folder
     * @param gpmData String of the Summarized Representation data
     * @param temData String of the Intermediate Representation data
     */
    void exportProject(File projectFile, String gpmData, String temData);


    /**
     * @param outputFile Output File
     * @param timeEntityMeasurementsAsString String of the Intermediate Representation
     */
    void writeDataToFile(File outputFile, String timeEntityMeasurementsAsString);

    /**
     * This method is used when the output File is already set.
     * @param timeEntityMeasurementsAsString String of the Intermediate Representation
     */
    void writeDataToFile(String timeEntityMeasurementsAsString);

    /**
     * @param file Input File
     */
    void setGivenFile(File file);

    /**
     * This is used only for the TEM_GPM kind of files.
     * @param file Input File
     * @param type Type of File
     */
    void setGivenFile(File file, Constants.FileType type);

    /**
     * @return Type of File
     */
    Constants.FileType getFileType();

    /**
     * @return Output File
     */
    File getOutputPath();
}
