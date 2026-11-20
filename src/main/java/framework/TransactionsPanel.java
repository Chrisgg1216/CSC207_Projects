package framework;

import adapter.TransactionController;
import adapter.TransactionViewModel;
import entity.TransactionType;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;

/** Add transactions without blocking the JavaFX application thread during HTTP. */
public class TransactionsPanel extends VBox {
    private final TransactionViewModel viewModel;
    private final ListView<String> list = new ListView<>();
    private final Label message = new Label();

    public TransactionsPanel(TransactionController controller, TransactionViewModel viewModel,
                             List<String> categoryNames) {
        this.viewModel = viewModel;
        setSpacing(10);
        setPadding(new Insets(12));
        DatePicker date = new DatePicker(LocalDate.now());
        date.setEditable(false);
        date.setAccessibleText("Transaction date");
        ComboBox<TransactionType> type = new ComboBox<>(FXCollections.observableArrayList(TransactionType.values()));
        type.setValue(TransactionType.SPENDING);
        type.setAccessibleText("Income or spending");
        ComboBox<String> category = new ComboBox<>(FXCollections.observableArrayList(categoryNames));
        category.setPromptText("Category");
        category.setAccessibleText("Transaction category");
        TextField amount = new TextField();
        amount.setPromptText("Amount");
        amount.setPrefColumnCount(8);
        amount.setAccessibleText("Original amount");
        ComboBox<String> currency = new ComboBox<>(FXCollections.observableArrayList("CAD", "USD", "CNY"));
        currency.setValue("CAD");
        currency.setAccessibleText("Original currency");
        TextField note = new TextField();
        note.setPromptText("Note (optional)");
        note.setAccessibleText("Transaction note");
        Button add = new Button("Add transaction");
        Button refresh = new Button("Refresh list");
        refresh.setOnAction(event -> controller.refresh());
        FlowPane form = new FlowPane(8, 8, date, type, category, amount, currency, note, add, refresh);
        add.setOnAction(event -> {
            LocalDate selectedDate = date.getValue();
            TransactionType selectedType = type.getValue();
            String selectedCategory = category.getValue();
            String selectedAmount = amount.getText();
            String selectedCurrency = currency.getValue();
            String selectedNote = note.getText();
            add.setDisable(true);
            refresh.setDisable(true);
            message.setText("Saving transaction; checking historical exchange rate if needed…");
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() {
                    controller.add(selectedDate, selectedType, selectedCategory, selectedAmount,
                            selectedCurrency, selectedNote);
                    return null;
                }
            };
            task.setOnSucceeded(done -> {
                add.setDisable(false);
                refresh.setDisable(false);
                render();
                if (!viewModel.isError()) {
                    amount.clear();
                    note.clear();
                }
            });
            task.setOnFailed(done -> {
                add.setDisable(false);
                refresh.setDisable(false);
                viewModel.update(viewModel.getLines(), "Could not complete the operation. Refresh the list before retrying.", true);
            });
            Thread worker = new Thread(task, "add-transaction");
            worker.setDaemon(true);
            worker.start();
        });
        list.setPlaceholder(new Label("No transactions yet. Add your first income or spending record."));
        list.setAccessibleText("Transactions with original amount, reference CAD amount and conversion status");
        message.setWrapText(true);
        VBox.setVgrow(list, Priority.ALWAYS);
        getChildren().addAll(new Label("Transactions — base currency CAD"),
                new Label("Milestone prototype: records are kept in memory for this session."), form, list, message);
        viewModel.addPropertyChangeListener(event -> Platform.runLater(this::render));
        controller.refresh();
    }

    private void render() {
        list.setItems(FXCollections.observableArrayList(viewModel.getLines()));
        message.setText((viewModel.isError() ? "Error: " : "") + viewModel.getMessage());
    }
}
