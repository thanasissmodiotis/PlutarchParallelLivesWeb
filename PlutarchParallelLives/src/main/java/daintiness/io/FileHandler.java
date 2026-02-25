package daintiness.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import daintiness.data.IDataHandler;
import daintiness.io.input.CsvReader;
import daintiness.io.input.ILoader;
import daintiness.io.input.LoaderFactory;
import daintiness.io.input.schemaevo.SchemaEvoLoader;
import daintiness.utilities.Constants;


public class FileHandler implements IFileHandler {
    private File folderFile;
    private File temFile;
    private File gpmFile;
    private File outputPath;
    @SuppressWarnings("unused")
	private String projectName;
    private Constants.FileType fileType;


    // GPM-only structures
    private List<int[]> phasesData;
    private Map<Integer, String[]> entityGroupIdToComponentsNameMap;
    private Map<Integer, Map<Integer, Double>> entityGroupIdToGPMMap;


    @Override
    public void setGivenFile(File file) {
        parseFileInfo(file);
    }

    @Override
    public void setGivenFile(File file, Constants.FileType type) {
        if (type != Constants.FileType.TEM_GPM) {
            parseFileInfo(file);
        } else {
            fileType = type;
            folderFile = file;
            parseProjectInfo();
        }
    }


    @Override
    public IDataHandler loadTEM(){
        LoaderFactory factory = new LoaderFactory();
        ILoader loader = factory.getLoader(fileType, temFile);

        IDataHandler dataHandler = loader.load();

        if (loader instanceof SchemaEvoLoader) {
            outputPath = ((SchemaEvoLoader) loader).getPLDFile();
        }
        dataHandler.setType(fileType);
        return dataHandler;
    }

    @Override
    public void writeDataToFile(File outputFile, String data) {
        this.outputPath = outputFile;
        writeDataToFile(data);
    }

    @Override
    public void writeDataToFile(String data) {
        try (FileWriter fw = new FileWriter(outputPath)) {
            fw.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void exportProject(File projectFile, String gpmData, String temData) {
        if (!projectFile.exists()) {
            if(projectFile.mkdirs()) {
                System.out.println("Folder was successful created.");
            } else {
                System.out.println("Failed to create the folder.");
            }
        }

        File temFile = new File(projectFile.getPath() + Constants.FS + "tem.tsv");
        File gpmFile = new File(projectFile.getPath() + Constants.FS + "gpm.tsv");
        writeDataToFile(temFile, temData);
        writeDataToFile(gpmFile, gpmData);
    }

    @Override
    public void parseProjectInfo() {
        if (fileType != Constants.FileType.TEM_GPM) {
            System.out.println("This method cannot be executed with this type of file");
            return;
        }
        String[] filesWeNeed = {"tem.tsv", "gpm.tsv"};
        for (File file: Objects.requireNonNull(folderFile.listFiles())) {
            mapFileToParameter(file, filesWeNeed);
        }
        loadGPMData();
    }

    @Override
    public List<int[]> getPhasesData() {
        return phasesData;
    }

    @Override
    public Map<Integer, String[]> getEntityGroupData() {
        return entityGroupIdToComponentsNameMap;
    }

    @Override
    public Map<Integer, Map<Integer, Double>> getLoadedMeasurementMap() {
        return entityGroupIdToGPMMap;
    }

    private void loadGPMData() {
        CsvReader csvReader = new CsvReader(gpmFile, false, "\t");
        List<String[]> lines = csvReader.readAll();
        entityGroupIdToGPMMap = new HashMap<>();
        entityGroupIdToComponentsNameMap = new HashMap<>();


        int lineNumber = 0;
        for(String[] line: lines) {
            if (lineNumber == 0) {
                loadPhases(line);
            } else {
                int entityGroupId = Integer.parseInt(line[0]);
                entityGroupIdToComponentsNameMap.put(entityGroupId, parseEntityGroupComponents(line[1]));
                entityGroupIdToGPMMap.put(entityGroupId, parseGroupPhaseMeasurements(line[2]));
            }
            lineNumber++;
        }

    }

    private String[] parseEntityGroupComponents(String stringComponents) {
        return parseListFromString(stringComponents);
    }

    private Map<Integer, Double> parseGroupPhaseMeasurements(String measurementsString) {
        Map<Integer,Double> gpmMap = new HashMap<>();
        String[] measurementsArray =parseListFromString(measurementsString);
        if (measurementsArray.length > 0) {
            for(String text: measurementsArray) {
                String[] phaseValueString = text.split(":");
                int phaseId = Integer.parseInt(phaseValueString[0]);
                double value = Double.parseDouble(phaseValueString[1]);
                gpmMap.put(phaseId, value);
            }
        }

        return  gpmMap;
    }

    private void loadPhases(String[] line) {
        phasesData = new ArrayList<>();
        for (String s : line) {
            String[] components = parseListFromString(s);
            int phasesId = Integer.parseInt(components[0]);
            int firstBeat = Integer.parseInt(components[1]);
            int lastBeat = Integer.parseInt(components[2]);
            phasesData.add(new int[]{phasesId, firstBeat, lastBeat});
        }
    }

    private String[] parseListFromString(String text) {
        String [] componentsList = text.replaceAll("\\{", "").replaceAll("}","").split(",");
        if (componentsList.length == 1 && componentsList[0].isBlank()) {
            componentsList = new String[]{};
        }
        return componentsList;
    }


    private void parseFileInfo(File file) {
        String fileName = file.getName();

        if (!file.isDirectory()) {
            String[] filenameComponents = fileName.split("\\.");
            projectName = filenameComponents[0];
            fileType = Constants.FileType.valueOf(filenameComponents[1].toUpperCase());
            temFile = file;
        } else if (file.isDirectory() ) {
            projectName = fileName;
            fileType = Constants.FileType.SCHEMA_EVO;
            temFile = file;
        } else {
            System.out.println("Oops sth is wrong with the filename.");
        }
    }


    public File getOutputPath() {
        return this.outputPath;
    }


    public Constants.FileType getFileType() {
        return this.fileType;
    }

    private void mapFileToParameter(File file, String[] filesWeNeed) {
        if (file.getName().endsWith(filesWeNeed[0])) {
            temFile = file;
        } else if (file.getName().endsWith(filesWeNeed[1])) {
            gpmFile = file;
        } else {
            System.out.println(file.getName() +
                    " doesn't match to any of the files we want. (Should never get in here)");
        }
    }
}
