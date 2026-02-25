package daintiness.io.input;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import daintiness.clustering.*;
import daintiness.data.IDataHandler;
import daintiness.io.FileHandler;
import daintiness.io.TestUtilities;
import daintiness.maincontroller.IMainController;
import daintiness.maincontroller.MainController;
import daintiness.utilities.Constants;

import java.io.File;

public class ProjectImporterTest {
    File projectWithClustering = new File(
            "src" + Constants.FS + "test" + Constants.FS + "resources" + Constants.FS + "projects" + Constants.FS + "biosql_with_clustering");
    File projectWithoutClustering = new File(
            "src" + Constants.FS + "test" + Constants.FS + "resources" + Constants.FS + "projects" + Constants.FS + "biosql_without_clustering");

    File originalFile = new File(
            "src" + Constants.FS + "test" + Constants.FS + "resources" + Constants.FS + "biosql");

    IMainController mainController = new MainController();

    TestUtilities utilities = new TestUtilities();

    @Test
    @DisplayName("Load project without clustering test")
    public void loadProjectWithoutClusteringTest() {
        mainController.load(originalFile);
        mainController.fitDataToGroupPhaseMeasurements(
                new ClusteringProfile(
                        new BeatClusteringProfile(mainController.getNumberOfBeats()),
                        new EntityClusteringProfile(mainController.getNumberOfEntities())
                )
        );
        mainController.generateChartDataOfType(Constants.MeasurementType.RAW_VALUE, Constants.AggregationType.SUM_OF_ALL);

        FileHandler actualFileHandler = new FileHandler();
        actualFileHandler.setGivenFile(projectWithoutClustering, Constants.FileType.TEM_GPM);
        IDataHandler actualDataHandler = actualFileHandler.loadTEM();
        IClusteringHandler actualClusteringHandler = new ClusteringHandler();
        actualClusteringHandler.setDataHandler(actualDataHandler);

        actualClusteringHandler.loadPhases(actualFileHandler.getPhasesData());
        actualClusteringHandler.loadEntityGroup(actualFileHandler.getEntityGroupData());
        actualClusteringHandler.loadClusteringData();

        Assertions.assertAll(
                () -> utilities.testPhases(mainController.getPhases(), actualClusteringHandler.getPhases()),
                () -> utilities.testEntityGroups(mainController.getEntityGroups(), actualClusteringHandler.getEntityGroups()),
                () -> utilities.testGPM(mainController.getChartData(), actualFileHandler.getLoadedMeasurementMap())
                );
    }

    @Test
    @DisplayName("Load project with clustering test")
    public void loadProjectWithClusteringTest() {
        mainController.load(originalFile);
        mainController.fitDataToGroupPhaseMeasurements(
                new ClusteringProfile(
                        new BeatClusteringProfile(20),
                        new EntityClusteringProfile(25)
                )
        );

        FileHandler actualFileHandler = new FileHandler();

        actualFileHandler.setGivenFile(projectWithClustering, Constants.FileType.TEM_GPM);

        IDataHandler actualDataHandler = actualFileHandler.loadTEM();
        IClusteringHandler actualClusteringHandler = new ClusteringHandler();
        actualClusteringHandler.setDataHandler(actualDataHandler);

        actualClusteringHandler.loadPhases(actualFileHandler.getPhasesData());
        actualClusteringHandler.loadEntityGroup(actualFileHandler.getEntityGroupData());
        actualClusteringHandler.loadClusteringData();

        Assertions.assertAll(
                () -> utilities.testPhases(mainController.getPhases(), actualClusteringHandler.getPhases()),
                () -> utilities.testEntityGroups(mainController.getEntityGroups(), actualClusteringHandler.getEntityGroups()),
                () -> utilities.testGPM(mainController.getChartData(), actualFileHandler.getLoadedMeasurementMap())
        );
    }


}