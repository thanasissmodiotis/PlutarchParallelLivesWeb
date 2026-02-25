package daintiness.io.input.schemaevo;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import daintiness.data.DataHandlerFactory;
import daintiness.data.IDataHandler;
import daintiness.io.input.CsvReader;
import daintiness.io.input.ILoader;
import daintiness.models.Beat;
import daintiness.models.Entity;
import daintiness.models.LifeDetails;
import daintiness.models.TimeEntityMeasurements;
import daintiness.models.measurement.IMeasurement;
import daintiness.models.measurement.MeasurementFactory;
import daintiness.utilities.Constants;
import daintiness.utilities.Constants.TransactionType;

public class SchemaEvoLoader implements ILoader {
    private File transitionsPath;
    private File timeLinePath;
    private File tablesPath;

    private File resultsFolder;
    private File figuresFolder;

    String projectName;

    private List<Beat> timeline;
    private List<Entity> entities;
    private HashMap<String, Integer> versionToBeatIdMap;
    private Map<String, Integer> entityNameToIndex;
    private List<TimeEntityMeasurements> timeEntityMeasurements;
    private Map<String, Map<Integer, TableBeatMetrics>> tableBeatMetricsMap;


    public SchemaEvoLoader(File projectFolder) {
        this.projectName = projectFolder.getName();
        resultsFolder = generateFolderPath(projectFolder, "results");
        figuresFolder = generateFolderPath(projectFolder, "figures");


        if (!findProjectFiles(resultsFolder)) {
            System.out.println("Corrupted folder. Couldn't find the files.");
        }
    }

    private File generateFolderPath(File projectFolder, String subFolderName) {
        return new File(projectFolder.getPath() + File.separator + subFolderName);
    }

    public File getPLDFile() {
        if (!figuresFolder.exists()) {
            figuresFolder.mkdirs();
        }

        File pplFolder = new File(figuresFolder.getPath() + File.separator + "PPL");
        if (!pplFolder.exists()) {
            pplFolder.mkdirs();
        }

        return new File(pplFolder.getPath() + File.separator + projectName + "_detailedPLD.tsv");
    }


    private boolean findProjectFiles(File resultsFolder) {
        if (!resultsFolder.exists()) {
            System.out.println("There is no resultFolder in the given project path.");
            return false;
        }

        File[] files= resultsFolder.listFiles();
        boolean timelineFound = false;
        boolean entitiesFound = false;
        boolean transitionsFound = false;

        if (files == null) {
            System.out.println("Empty folder.");
            return false;
        }

        if (files.length > 2) {

            for (File file : files) {

                if (file.getName().endsWith("SchemaHeartbeat.tsv")) {
                    timelineFound = true;
                    this.timeLinePath = file;
                } else if (file.getName().endsWith("tables_DetailedStats.tsv")) {
                    entitiesFound = true;
                    this.tablesPath = file;
                } else if (file.getName().endsWith("transitions.csv")) {
                    transitionsFound = true;
                    this.transitionsPath = file;
                }
            }
        }
        return timelineFound && entitiesFound && transitionsFound;
    }


    public IDataHandler load() {
        loadTimeLine();
        loadEntities();
        loadMetrics();
        generateTimeEntityMeasurements();

        DataHandlerFactory factory = new DataHandlerFactory();
        IDataHandler dataHandler = factory.getDataHandler("SIMPLE_DATA_HANDLER");
        dataHandler.init(timeline, entities, timeEntityMeasurements);

        return dataHandler;

    }


    public void loadTimeLine() {
        timeline = new ArrayList<>();
        versionToBeatIdMap = new HashMap<>();
        CsvReader csvReader = new CsvReader(timeLinePath, true, "\t");
        List<String[]> rawLines = csvReader.readAll();

        //int i = 0;
        for (String[] rawLine : rawLines) {
            Beat currentBeat = parseTimeBeat(rawLine);
            timeline.add(currentBeat);
            versionToBeatIdMap.put(currentBeat.getRawDate(), currentBeat.getBeatId());
          //  i++;
        }
    }


    private Beat parseTimeBeat(String[] rawLine) {
        // trId
        int id = Integer.parseInt(rawLine[0]);

        // newVer
        String rawDate = rawLine[3];
        LocalDateTime date = null;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            date = LocalDateTime.parse(rawLine[4], formatter);
        } catch (DateTimeParseException e) {
            System.out.println("Failed to parse DateTime from file");
        }

        return new Beat(id, rawDate, date);
    }


    public void loadEntities() {
        entityNameToIndex = new HashMap<>();
        entities = new ArrayList<>();
        CsvReader csvReader = new CsvReader(tablesPath, true, "\t");
        List<String[]> rawLines = csvReader.readAll();

        int i = 0;
        for (String[] rawLine : rawLines) {
            entities.add(parseTable(rawLine, i));
            entityNameToIndex.put(entities.get(entities.size()-1).getEntityName(), i);
            i++;
        }
    }


    private Entity parseTable(String[] rawLine, int entityId) {
        String tableName = rawLine[0];

        int duration = Integer.parseInt(rawLine[1]);
        int birth = Integer.parseInt(rawLine[2]);
        int death;
        boolean isAlive = false;

        if (rawLine[3].equals("-")) {
            death = Integer.parseInt(rawLine[4]);
            isAlive = true;
        } else {
            death = Integer.parseInt(rawLine[3]);
        }

        LifeDetails tableLife = new LifeDetails(birth, death, isAlive, duration);
        return new Entity(entityId, tableName, tableLife);
    }


    public void loadMetrics() {

        CsvReader csvReader = new CsvReader(transitionsPath, true, ";");
        List<String[]> rawLines = csvReader.readAll();


        initTablesInMap();

        int i = 0;
        for (String[] line : rawLines) {
            if (line.length < 5) {
                System.out.println("Line: " + i + " in transitions.csv is corrupted.");
                i++;
                continue;
            }
            handleAtomicChange(line);
            i++;
        }
    }


    private void generateTimeEntityMeasurements() {
        timeEntityMeasurements = new ArrayList<>();

        for (String entityId: tableBeatMetricsMap.keySet()){
            int previousBeatId = -1;
            for (int currentBeatId: tableBeatMetricsMap.get(entityId).keySet()){

                timeEntityMeasurements.add(new TimeEntityMeasurements(
                        entities.get(entityNameToIndex.get(entityId)),
                        timeline.get(currentBeatId),
                        createTEMCombinations(entityId, currentBeatId, previousBeatId)));
                previousBeatId = currentBeatId;
            }
        }
    }


    private List<IMeasurement> createTEMCombinations(String entityId, int currentBeatId, int previousBeatId) {
        MeasurementFactory factory = new MeasurementFactory();
        Constants.AggregationType[] aggregationTypes = {Constants.AggregationType.SUM_OF_ALL,
                Constants.AggregationType.SUM_OF_INSERTIONS,
                Constants.AggregationType.SUM_OF_DELETIONS,
                Constants.AggregationType.SUM_OF_UPDATES,
                Constants.AggregationType.SUM_OF_INSERTIONS_AND_DELETIONS,
                Constants.AggregationType.SUM_OF_INSERTIONS_AND_UPDATES,
                Constants.AggregationType.SUM_OF_DELETIONS_AND_UPDATES
        };

        List<IMeasurement> measurementList = new ArrayList<>();
        double currentValue;
        double previousValue;
        for (Constants.AggregationType type : aggregationTypes) {
            currentValue = tableBeatMetricsMap.get(entityId).get(currentBeatId).getValueByAggregationType(type);
            measurementList.add(factory.getRawMeasurement(currentValue, type));
            if (previousBeatId != -1) {
                previousValue = tableBeatMetricsMap.get(entityId).get(previousBeatId).getValueByAggregationType(type);
                measurementList.add(factory.getDeltaMeasurement(currentValue - previousValue, type));
            }
        }


        return measurementList;
    }


    private void initTablesInMap() {
        tableBeatMetricsMap = new HashMap<>();
        for (Entity entity : entities) {
            String tableName = entity.getEntityName();
            tableBeatMetricsMap.put(tableName, new HashMap<>());
        }
    }


    private void handleAtomicChange(String[] rawLine) {

        TransactionType transactionType = parseTransactionType(rawLine[4]);
        if (transactionType != Constants.TransactionType.NOT_SUPPORTED) {
            int beatId = Integer.parseInt(rawLine[0]);
            String tableName = rawLine[3];

            mapToMetrics(tableName, beatId, transactionType);
        }
    }


    private TransactionType parseTransactionType(String s) {
        TransactionType transactionType;
        if (s.contains("NewTable") || s.contains("DeleteTable")) {
            return Constants.TransactionType.NOT_SUPPORTED;
        }

        switch (s) {
            case "Insertion:UpdateTable":
                transactionType = Constants.TransactionType.INSERTION;
                break;
            case "Deletion:UpdateTable":
                transactionType = Constants.TransactionType.DELETION;
                break;
            case "Update:KeyChange":
            case "Update:TypeChange":
                transactionType = Constants.TransactionType.UPDATE;
                break;
            default:
                transactionType = Constants.TransactionType.NOT_SUPPORTED;
                break;
        }
        return transactionType;
    }


    private void mapToMetrics(String tableName, int beatId, TransactionType transactionType) {
        if (!tableBeatMetricsMap.get(tableName).containsKey(beatId)) {
            tableBeatMetricsMap.get(tableName).put(beatId, new TableBeatMetrics());
        }

        switch (transactionType) {
            case INSERTION:
                tableBeatMetricsMap.get(tableName).get(beatId).addInsertion();
                break;
            case DELETION:
                tableBeatMetricsMap.get(tableName).get(beatId).addDeletion();
                break;
            case UPDATE:
                tableBeatMetricsMap.get(tableName).get(beatId).addUpdate();
                break;
            default:
                System.out.println("mapToMetrics: Not supported type.");
        }
    }

}
