package app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Entry point: the main window with one tab per feature.
 * Run with: mvn javafx:run
 */
public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("Budgeting App");
        stage.setScene(new Scene(new ApplicationBuilder().buildTabs(), 960, 520));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
