package app;

import javafx.application.Application;

/**
 * Starts MainApp. This class does not extend Application, so it can be run
 * directly from the IDE with JavaFX on the classpath.
 */
public class Launcher {

    public static void main(String[] args) {
        Application.launch(MainApp.class, args);
    }
}