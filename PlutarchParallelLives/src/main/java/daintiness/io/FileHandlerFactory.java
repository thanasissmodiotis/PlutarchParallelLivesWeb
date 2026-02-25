package daintiness.io;

public class FileHandlerFactory {

    public IFileHandler getFileHandler(String type) {
        if (type.isBlank()) {
            return null;
        }

        if (type.equals("SIMPLE_FILE_HANDLER")) {
            return new FileHandler();
        }

        return null;
    }
}
