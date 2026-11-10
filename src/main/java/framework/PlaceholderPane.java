package framework;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Stand-in screen for a tab whose owner has not built it yet. Each owner replaces
 * it with their own panel in ApplicationBuilder.
 */
public class PlaceholderPane extends VBox {

    public PlaceholderPane(String title, String owner, String planned) {
        setSpacing(8);
        setPadding(new Insets(24));
        setAlignment(Pos.TOP_LEFT);

        Label heading = new Label(title + " (not built yet)");
        heading.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        Label who = new Label("Owner: " + owner);
        Label what = new Label("Planned: " + planned);
        what.setWrapText(true);

        getChildren().addAll(heading, who, what);
    }
}
