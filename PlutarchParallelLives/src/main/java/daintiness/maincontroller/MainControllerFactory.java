package daintiness.maincontroller;

public class MainControllerFactory {
    public IMainController getMainController(String type) {
        if (type.isBlank()) {
            return null;
        }

        if (type.equals("SIMPLE_MAIN_CONTROLLER")) {
            return new MainController();
        }

        return null;
    }
}
