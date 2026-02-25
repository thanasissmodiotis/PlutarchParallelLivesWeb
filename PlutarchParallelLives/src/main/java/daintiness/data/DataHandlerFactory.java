package daintiness.data;

public class DataHandlerFactory {
    public IDataHandler getDataHandler(String type) {
        if (type.isBlank()) {
            return null;
        }

        if (type.equals("SIMPLE_DATA_HANDLER")) {
            return new DataHandler();
        }
        return null;
    }
}
