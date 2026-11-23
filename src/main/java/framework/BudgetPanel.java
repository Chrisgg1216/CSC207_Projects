package framework;

import adapter.BudgetController;
import adapter.BudgetViewModel;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * The Budget tab: set a monthly limit per category and see spent / remaining /
 * status. Status is shown as text and a symbol, not only color.
 */
public class BudgetPanel extends VBox {

    private final BudgetController controller;
    private final BudgetViewModel viewModel;

    private final ComboBox<String> categoryBox = new ComboBox<>();
    private final TextField limitField = new TextField();
    private final TextField monthField = new TextField(YearMonth.now().toString());
    private final ListView<String> statusList = new ListView<>();
    private final Label messageLabel = new Label();

    public BudgetPanel(BudgetController controller, BudgetViewModel viewModel, List<String> categoryNames) {
        this.controller = controller;
        this.viewModel = viewModel;

        setSpacing(10);
        setPadding(new Insets(12));

        categoryBox.setItems(FXCollections.observableArrayList(categoryNames));
        categoryBox.setPromptText("Category");
        categoryBox.setAccessibleText("Category");

        limitField.setPromptText("Monthly limit (" + controller.getBaseCurrency() + ")");
        limitField.setAccessibleText("Monthly limit in " + controller.getBaseCurrency());

        monthField.setPrefColumnCount(7);
        monthField.setAccessibleText("Month in the form year-month, for example 2026-11");

        Button setButton = new Button("Set budget");
        setButton.setOnAction(e -> onSetBudget());
        Button refreshButton = new Button("Refresh status");
        refreshButton.setOnAction(e -> onRefresh());

        HBox form = new HBox(8, new Label("Month:"), monthField, categoryBox, limitField, setButton, refreshButton);

        statusList.setAccessibleText("Budget status for each category");
        statusList.setPlaceholder(new Label("No budget status to show yet."));
        VBox.setVgrow(statusList, Priority.ALWAYS);

        messageLabel.setWrapText(true);
        messageLabel.setAccessibleText("Message");

        getChildren().addAll(new Label("Monthly budgets"), form, statusList, messageLabel);

        viewModel.addPropertyChangeListener(evt -> Platform.runLater(this::render));
        render();
    }

    private void onSetBudget() {
        YearMonth month = readMonth();
        if (month == null) {
            return;
        }
        controller.setBudget(categoryBox.getValue(), month, limitField.getText());
        // Show the new status straight away so a warning appears immediately.
        controller.checkBudget(month);
    }

    private void onRefresh() {
        YearMonth month = readMonth();
        if (month != null) {
            controller.checkBudget(month);
        }
    }

    private YearMonth readMonth() {
        try {
            return YearMonth.parse(monthField.getText().trim());
        } catch (DateTimeParseException ex) {
            viewModel.setMessage("Month must look like 2026-11.", true);
            return null;
        }
    }

    private void render() {
        statusList.setItems(FXCollections.observableArrayList(viewModel.getLines()));
        String prefix = viewModel.isError() ? "Error: " : "";
        messageLabel.setText(viewModel.getMessage().isEmpty() ? "" : prefix + viewModel.getMessage());
    }
}
