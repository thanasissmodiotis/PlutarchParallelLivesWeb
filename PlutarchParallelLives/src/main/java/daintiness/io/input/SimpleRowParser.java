package daintiness.io.input;

import java.util.Arrays;

import daintiness.models.Entity;
import daintiness.models.LifeDetails;
import daintiness.utilities.Constants;

public class SimpleRowParser {
    private final String[] line;
    private String rowName = "";
    private String[] rowValue;
    private int splitPoint;
    private Entity entity;
    private final int entityId;
    private Constants.RowFormatType formatType;


    public SimpleRowParser(String[] line, int entityId) {
        this.line = line;
        this.entityId = entityId;
        splitPoint = 1;
    }

    public Entity getEntity() {
        return entity;
    }

    public void splitNameAndValue(int expectedTimeline) {
        findFormatType(expectedTimeline);
        findSplitPoint();
        parseRowValue();
        if (formatType.equals(Constants.RowFormatType.ENTITY_INFO)) {
            parseEntity();
        } else {
            parseRowName();
            generateEntity(expectedTimeline);
        }

    }

    private void parseEntity() {
        String rawEntity = line[0].substring(1,line[0].length() - 1);
        String[] components = rawEntity.split(",");

        String entityName = components[0];

        int indexCorrector = components.length - 4;
        for (int i=1; i < indexCorrector; i ++) {
            entityName = entityName.concat(",").concat(line[i]);
        }
        int birth = Integer.parseInt(components[indexCorrector + 1]);
        int death = Integer.parseInt(components[indexCorrector + 2]);
        boolean isAlive = components[indexCorrector + 3].equals("1");
        int duration = death - birth + 1;

        entity = new Entity(entityId, entityName, new LifeDetails(birth, death, isAlive, duration));
    }

    private void findFormatType(int expectedTimelineSize) {
        if (line[0].startsWith("{") && line[0].endsWith("}")) {
            formatType = Constants.RowFormatType.ENTITY_INFO;

        } else if (line[0].contains(",") || line[0].contains(".") || line[0].contains("\"")){
            formatType = Constants.RowFormatType.MULTI_WORDS;

        } else {//if (line[0].matches("\\S+")) {
            formatType = Constants.RowFormatType.NORMAL;
        }
//        } else {
//            System.out.println("Not supported line format.");
//            System.out.println(line[0]);
//        }
    }

    private void parseRowValue() {
        rowValue = Arrays.copyOfRange(line, splitPoint, line.length);
    }



    private void parseRowName() {

        if (!formatType.equals(Constants.RowFormatType.MULTI_WORDS)) {
            rowName = line[0];
        } else {
            for (int i = 0; i < splitPoint; i++) {
                rowName  = rowName.concat(line[i]);
                if (i < splitPoint - 1) {
                    rowName = rowName.concat(",");
                }
            }
            rowName = rowName.replaceAll("\"","");
//            rowName = rowName.substring(1, rowName.length() - 2);
        }
    }

    public void findSplitPoint() {
        if (formatType.equals(Constants.RowFormatType.MULTI_WORDS)) {
            for (int i = 1; i < line.length; i++) {
                if (line[i].contains("\"")) {
                    splitPoint = i + 1;
                    break;
                }
            }
        }
    }


    public String[] getRowValue() {
        return rowValue;
    }

    private void generateEntity(int expectedTimeline) {
        int birthBeatId = getBirthBeatId();
        int deathBeatId = getDeathBeatId(expectedTimeline);
        int duration =  deathBeatId - birthBeatId + 1;
        LifeDetails lifeDetails = new LifeDetails(birthBeatId,deathBeatId, true, duration);


        entity = new Entity(entityId, rowName, lifeDetails);
    }

    private int getBirthBeatId() {
        for (int i = 0; i < rowValue.length; i++) {

            if (rowValue[i] != null && !rowValue[i].isBlank()) {
                return i;
            }
        }

        return -1;
    }

    private int getDeathBeatId(int expectedTimeline) {
        // This block of code handles some gapminder rows with missing columns.
        int missingColumns = 0;
        int numOfColumns = rowValue.length;

        while (expectedTimeline > (numOfColumns + missingColumns)) {
            missingColumns++;
        }
        //
        for (int i = (rowValue.length - 1); i >= 0; i--) {

            if (rowValue[i] != null && !rowValue[i].isBlank()) {
                return i + missingColumns;
            }
        }

        return expectedTimeline;
    }
}
