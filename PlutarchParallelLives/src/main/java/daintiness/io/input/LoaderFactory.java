package daintiness.io.input;

import java.io.File;

import daintiness.io.input.schemaevo.SchemaEvoLoader;
import daintiness.utilities.Constants;

public class LoaderFactory {

    public ILoader getLoader(Constants.FileType fileType, File inputFile) {
        ILoader loader = null;
        switch (fileType) {
            case CSV:
                loader = new SimpleLoader(inputFile);
                break;
            case TSV:
            case TEM_GPM:
                loader = new SimpleLoader(inputFile, "\t");
                break;
            case SCHEMA_EVO:
                loader = new SchemaEvoLoader(inputFile);
                break;
            default:
                System.out.println("LoaderFactory.getLoader: Not supported fileType -> " + fileType);
        }
        return loader;
    }
}
