package daintiness.io.input;

import org.junit.jupiter.api.*;

import daintiness.data.IDataHandler;
import daintiness.io.input.schemaevo.SchemaEvoLoader;
import daintiness.models.Beat;
import daintiness.models.Entity;
import daintiness.models.TimeEntityMeasurements;
import daintiness.utilities.Constants;

import java.io.File;
import java.util.List;
import java.util.Map;


public class LoaderTest {

    // Test data
    private final File csvFilePath = new File("src" + Constants.FS + "test" + Constants.FS + "resources" + Constants.FS + "test_data.csv");
    private final File tsvFilePath = new File("src" + Constants.FS + "test" + Constants.FS + "resources" + Constants.FS + "test_data.tsv");
    private final File schemaEvoPath = new File("src" + Constants.FS + "test" + Constants.FS + "resources" + Constants.FS + "schema_evo_mock_data");

    private ILoader loader;

    // Expected SimpleLoader data
    double[] expectedGreekMeasurements = { 1.2, Double.NaN, 2.3};
    double[] expectedMicronesiaMeasurements = {3.5, 5.6, 1.2};
    String[] expectedEntitiesSL = {"Greece", "Micronesia, Fed. Sts."};
    String[] expectedTimeLineSL = {"2000-01-01T00:00", "2001-01-01T00:00", "2002-01-01T00:00"};

    // Expected SchemaEvoLoader
    String[] expectedEntitiesSEL = {"t1", "t2", "t3", "t4"};
    String[] expectedTimelineSEL = {
            "2009-02-11T17:25:48",
            "2009-03-16T00:43:59", "2009-03-16T13:01:18",
            "2009-03-16T15:44:37", "2009-03-17T01:34:08", "2009-03-18T15:56:09",
            "2009-03-18T16:08:40", "2009-03-19T14:11:22", "2009-03-19T16:58:12",
            "2009-03-20T18:11", "2009-03-20T18:13:28"};
    double[] expectedT1Measurements = {
            Double.NaN,
            Double.NaN, Double.NaN,
            1.0, Double.NaN,
            Double.NaN, Double.NaN,
            Double.NaN, Double.NaN,
            Double.NaN, Double.NaN};
    double[] expectedT2Measurements = {
            Double.NaN,
            Double.NaN, 1.0,
            Double.NaN, 1.0,
            Double.NaN, Double.NaN,
            Double.NaN, Double.NaN,
            Double.NaN, Double.NaN};


    @Test
    @DisplayName("CSV Loader Test")
    public void csvLoaderTest() {
        loader = new SimpleLoader(csvFilePath);
        IDataHandler dataHandler = loader.load();

        Assumptions.assumeTrue(dataHandler != null);
        Assertions.assertAll(
                () -> testEntities(dataHandler, expectedEntitiesSL),
                () -> testTimeLine(dataHandler, expectedTimeLineSL),
                () -> testMeasurements(dataHandler, "Greece", expectedGreekMeasurements),
                () -> testMeasurements(dataHandler, "Micronesia, Fed. Sts.", expectedMicronesiaMeasurements));
    }


    @Test
    @DisplayName("TSV Loader Test")
    public void tsvLoaderTest() {
        loader = new SimpleLoader(tsvFilePath, "\t");
        IDataHandler dataHandler = loader.load();

        Assumptions.assumeTrue(dataHandler != null);
        Assertions.assertAll(
                () -> testEntities(dataHandler, expectedEntitiesSL),
                () -> testTimeLine(dataHandler, expectedTimeLineSL),
                () -> testMeasurements(dataHandler, "Greece", expectedGreekMeasurements),
                () -> testMeasurements(dataHandler, "Micronesia, Fed. Sts.", expectedMicronesiaMeasurements));
    }

    @Test
    @DisplayName("SchemaEvo Loader Test")
    public void schemaEvoLoaderTest() {
        loader = new SchemaEvoLoader(schemaEvoPath);
        IDataHandler dataHandler = loader.load();

        Assumptions.assumeTrue(dataHandler != null);
        Assertions.assertAll(
                () -> testEntities(dataHandler, expectedEntitiesSEL),
                () -> testTimeLine(dataHandler, expectedTimelineSEL),
                () -> testMeasurementsSE(dataHandler, "t1", expectedT1Measurements),
                () -> testMeasurementsSE(dataHandler, "t2", expectedT2Measurements));
    }


    private void testEntities(IDataHandler dataHandler, String[] expectedEntities){
        List<Entity> actualEntities = dataHandler.getPopulation();

        Assumptions.assumeTrue(expectedEntities.length == actualEntities.size());

        for(int i = 0; i < actualEntities.size(); i++){
            Assertions.assertEquals(expectedEntities[i], actualEntities.get(i).getEntityName());
        }

    }


    private void testTimeLine(IDataHandler dataHandler, String[] expectedTimeLine) {
        List<Beat> actualTimeLine = dataHandler.getTimeline();

        Assumptions.assumeTrue(expectedTimeLine.length == actualTimeLine.size());

        for(int i = 0; i < actualTimeLine.size(); i++){
            Assertions.assertEquals(expectedTimeLine[i], actualTimeLine.get(i).getDate().toString());
        }
    }


    private void testMeasurements(IDataHandler dataHandler, String entityName, double[] expectedMeasurements) {

        Map<Integer, TimeEntityMeasurements> actualMeasurements = dataHandler.getEntityNameToTEMMap().get(entityName);

        for (Integer i: actualMeasurements.keySet()){
            Assertions.assertEquals(expectedMeasurements[i], actualMeasurements.get(i).getMeasurementOfType(Constants.MeasurementType.RAW_VALUE, Constants.AggregationType.NO_AGGREGATION));
        }
    }

    private void testMeasurementsSE(IDataHandler dataHandler, String entityName, double[] expectedMeasurements) {

        Map<Integer, TimeEntityMeasurements> actualMeasurements = dataHandler.getEntityNameToTEMMap().get(entityName);

        for (Integer i: actualMeasurements.keySet()){
            Assertions.assertEquals(expectedMeasurements[i], actualMeasurements.get(i).getMeasurementOfType(Constants.MeasurementType.RAW_VALUE, Constants.AggregationType.SUM_OF_ALL));
        }
    }
}