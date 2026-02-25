package daintiness.io.input;

import java.io.File;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import daintiness.data.DataHandlerFactory;
import daintiness.data.IDataHandler;
import daintiness.models.Beat;
import daintiness.models.Entity;
import daintiness.models.TimeEntityMeasurements;
import daintiness.models.measurement.IMeasurement;
import daintiness.models.measurement.MeasurementFactory;
import daintiness.utilities.Constants;


public class SimpleLoader implements ILoader{

    private final File dataPath;
    private final CsvReader csvReader;

    public SimpleLoader(File path) {
        this.dataPath = path;
        this.csvReader = new CsvReader(dataPath, false);
    }


    public SimpleLoader(File path, String delimiter) {
        this.dataPath = path;
        this.csvReader = new CsvReader(dataPath, false, delimiter);
    }


    public IDataHandler load() {
        List<Entity> entities = new ArrayList<>();
        List<Beat> timeLine = new ArrayList<>();
        List<TimeEntityMeasurements> measurements = new ArrayList<>();

        List<String[]> lines = csvReader.readAll();
        SimpleRowParser rowHandler;
        for (int i = 0; i < lines.size(); i++) {
            if (i == 0) {
                timeLine = parseTimeLine(lines.get(i));
            } else {
                rowHandler = new SimpleRowParser(lines.get(i), i-1);
                rowHandler.splitNameAndValue(timeLine.size());

                entities.add(rowHandler.getEntity());
                measurements.addAll(parseMeasurements(rowHandler.getEntity(), rowHandler.getRowValue(), timeLine));
            }
        }
        DataHandlerFactory factory = new DataHandlerFactory();
        IDataHandler dataHandler = factory.getDataHandler("SIMPLE_DATA_HANDLER");
        dataHandler.init(timeLine, entities, measurements);
        return dataHandler;
    }


    private ArrayList<TimeEntityMeasurements> parseMeasurements(Entity entity, String[] rowData, List<Beat> timeLine) {
        MeasurementFactory factory = new MeasurementFactory();
        ArrayList<TimeEntityMeasurements> temMeasurements = new ArrayList<>();
        double value;
        double previousValue = 0;


        for(int i=0; i < rowData.length; i++) {
            List<IMeasurement> measurementList = new ArrayList<>();
            if (!rowData[i].isBlank()) {
                NumberFormat format = NumberFormat.getInstance(Locale.US);
                Number number = -Double.MIN_NORMAL;
                try {
                    number = format.parse(rowData[i]);
                } catch (ParseException e) {
                    System.out.println("Current system locale:" + Locale.getDefault());
                    System.out.println("String tried to parse: " + rowData[i]);
                }
                value = number.doubleValue();
//                value = Double.parseDouble(rowData[i]);


                measurementList.add(factory.getRawMeasurement(value,Constants.AggregationType.NO_AGGREGATION));
                if (i != 0) {
                    measurementList.add(factory.getDeltaMeasurement(value - previousValue, Constants.AggregationType.NO_AGGREGATION));
                    previousValue = value;
                }

                temMeasurements.add(new TimeEntityMeasurements(entity, timeLine.get(i), measurementList));
            }
        }
        return temMeasurements;
    }


    private ArrayList<Beat> parseTimeLine(String[] rowData) {
        ArrayList<Beat> timeline = new ArrayList<>();

        for (int i = 1; i < rowData.length; i++) {
            timeline.add(new Beat(i - 1, rowData[i], formatToFullDateTime(rowData[i])));
        }

        return timeline;
    }


    public LocalDateTime formatToFullDateTime(String data) {
        // TODO: This could be a little more sophisticated in the future
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (data.length() == 4) {
            return LocalDateTime.parse(data + "-01-01 00:00:00", formatter);
        } else {
            return LocalDateTime.parse(data, formatter);
        }
    }
}
