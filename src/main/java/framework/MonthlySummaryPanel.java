package framework;

import adapter.MonthlySummaryController;
import adapter.MonthlySummaryViewModel;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;

public final class MonthlySummaryPanel extends VBox {
    private final DatePicker date = new DatePicker(LocalDate.now());
    private final Label heading = new Label();
    private final Label totals = new Label();
    private final Label message = new Label();
    private final ListView<String> categories = new ListView<>();
    private final ListView<String> transactions = new ListView<>();
    private final MonthlySummaryController controller;

    public MonthlySummaryPanel(MonthlySummaryController controller, MonthlySummaryViewModel viewModel) {
        this.controller = controller;
        setSpacing(10);
        setPadding(new Insets(12));
        date.setEditable(false);
        Label dateLabel = new Label("Choose any date in the month:");
        dateLabel.setLabelFor(date);
        Button refresh = new Button("Refresh summary");
        date.setOnAction(event -> refresh());
        refresh.setOnAction(event -> refresh());

        categories.setPlaceholder(new Label("No converted spending"));
        transactions.setPlaceholder(new Label("No transactions to show"));
        Label categoryLabel = new Label("Spending by category");
        categoryLabel.setLabelFor(categories);
        Label transactionLabel = new Label("Transactions in this month");
        transactionLabel.setLabelFor(transactions);
        VBox categoryPanel = new VBox(6, categoryLabel, categories);
        VBox transactionPanel = new VBox(6, transactionLabel, transactions);
        VBox.setVgrow(categories, Priority.ALWAYS);
        VBox.setVgrow(transactions, Priority.ALWAYS);
        SplitPane groups = new SplitPane(categoryPanel, transactionPanel);
        groups.setDividerPositions(0.32);
        VBox.setVgrow(groups, Priority.ALWAYS);
        message.setWrapText(true);

        getChildren().addAll(new HBox(8, dateLabel, date, refresh), heading, totals, message, groups);
        viewModel.addStateListener(event -> render(viewModel.getState()));
        refresh();
    }

    public void refresh() {
        controller.showMonth(date.getValue());
    }

    private void render(MonthlySummaryViewModel.State state) {
        heading.setText(state.heading());
        totals.setText(state.totals());
        message.setText(state.message());
        message.setVisible(!state.message().isEmpty());
        message.setManaged(message.isVisible());
        categories.getItems().setAll(state.categoryLines());
        transactions.getItems().setAll(state.transactionLines());
    }
}
