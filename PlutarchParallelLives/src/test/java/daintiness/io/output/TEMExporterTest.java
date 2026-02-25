package daintiness.io.output;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import daintiness.data.IDataHandler;
import daintiness.io.FileHandler;
import daintiness.io.IFileHandler;
import daintiness.models.Beat;
import daintiness.models.Entity;
import daintiness.models.TimeEntityMeasurements;
import daintiness.utilities.Constants;

import java.io.File;
import java.util.List;
import java.util.Map;

public class TEMExporterTest {

    private final String projectName = "schema_evo_mock_data";
    private final File inputPath = new File("src" + Constants.FS + "test" + Constants.FS + "resources" +
            Constants.FS + projectName);
    private final File outputPath = new File("src" + Constants.FS + "test" + Constants.FS + "resources" + Constants.FS + "converted_data" +
            Constants.FS + projectName + ".tsv");

    IFileHandler expectedFileHandler = new FileHandler();
    IFileHandler actualFileHandler = new FileHandler();

    @Test
    @DisplayName("Export PopulationHistory to .tsv Test")
    public void exportPopulationHistoryToTsvTest() {
        // Load and save mock data
        expectedFileHandler.setGivenFile(inputPath);
        IDataHandler expectedDataHandler = expectedFileHandler.loadTEM();
        expectedFileHandler.writeDataToFile(outputPath, expectedDataHandler.getTimeEntityMeasurementAsString());

        // Load converted mock data
        actualFileHandler.setGivenFile(outputPath);
        IDataHandler actualDataHandler = actualFileHandler.loadTEM();

        Assertions.assertAll(
                () -> testEntities(actualDataHandler.getPopulation(), expectedDataHandler.getPopulation()),
                () -> testTimeLine(actualDataHandler.getTimeline(), expectedDataHandler.getTimeline()),
                () -> testMeasurements(actualDataHandler.getEntityNameToTEMMap(), expectedDataHandler.getEntityNameToTEMMap())
        );
    }

    private void testMeasurements(Map<String, Map<Integer, TimeEntityMeasurements>> actualEntityIdToTEMMap,
                                  Map<String, Map<Integer, TimeEntityMeasurements>> expectedEntityIdToTEMMap) {

        for (String entityName: expectedEntityIdToTEMMap.keySet()) {
            for (Integer beatId: expectedEntityIdToTEMMap.get(entityName).keySet()) {
                Assertions.assertEquals(
                        actualEntityIdToTEMMap.get(entityName).get(beatId).getMeasurementOfType(Constants.MeasurementType.RAW_VALUE, Constants.AggregationType.NO_AGGREGATION),
                        expectedEntityIdToTEMMap.get(entityName).get(beatId).getMeasurementOfType(Constants.MeasurementType.RAW_VALUE, Constants.AggregationType.SUM_OF_ALL)
                );
            }
        }

    }

    private void testEntities(List<Entity> actualPopulation, List<Entity> expectedPopulation){

        Assumptions.assumeTrue(actualPopulation.size() == expectedPopulation.size());

        for(int i = 0; i < actualPopulation.size(); i++){
            Assertions.assertEquals(actualPopulation.get(i).getEntityId(), expectedPopulation.get(i).getEntityId());
            Assertions.assertEquals(actualPopulation.get(i).getEntityName(), expectedPopulation.get(i).getEntityName());

            // LifeDetails (Probably it's gonna fail because of the lazarus effect in our data)
            Assertions.assertEquals(actualPopulation.get(i).getLifeDetails().getBirthBeatId(), expectedPopulation.get(i).getLifeDetails().getBirthBeatId());
            Assertions.assertEquals(actualPopulation.get(i).getLifeDetails().getDeathBeatId(), expectedPopulation.get(i).getLifeDetails().getDeathBeatId());
            Assertions.assertEquals(actualPopulation.get(i).getLifeDetails().getDuration(), expectedPopulation.get(i).getLifeDetails().getDuration());
            Assertions.assertEquals(actualPopulation.get(i).getLifeDetails().isAlive(), expectedPopulation.get(i).getLifeDetails().isAlive());
        }

    }


    private void testTimeLine(List<Beat> actualTimeline, List<Beat> expectedTimeline) {

        Assumptions.assumeTrue(actualTimeline.size() == expectedTimeline.size());

        for(int i = 0; i < actualTimeline.size(); i++){
            Assertions.assertEquals(actualTimeline.get(i).getBeatId(), expectedTimeline.get(i).getBeatId());
            Assertions.assertEquals(actualTimeline.get(i).getDate(), expectedTimeline.get(i).getDate());
        }
    }
}