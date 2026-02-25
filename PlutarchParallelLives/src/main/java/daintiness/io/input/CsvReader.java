package daintiness.io.input;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CsvReader {
    private final File path;
    private final String separator;
    private final boolean skipHeader;

    public CsvReader(File path, boolean skipHeader) {
        this.path = path;
        this.skipHeader = skipHeader;
        this.separator = ",";
    }

    public CsvReader(File path, boolean skipHeader, String separator) {
        this.path = path;
        this.separator = separator;
        this.skipHeader = skipHeader;
    }

    public List<String[]> readAll() {
        List<String[]> lines = new ArrayList<>();

        try {
            FileReader reader = new FileReader(path);
            BufferedReader bufferedReader = new BufferedReader(reader);
            if (skipHeader) {
                bufferedReader.lines().skip(1).forEach(e -> lines.add(e.split(separator)));
            } else {
                bufferedReader.lines().forEach(e -> lines.add(e.split(separator)));
            }

            reader.close();
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return lines;
    }
}
