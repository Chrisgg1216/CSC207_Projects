package app;

import adapter.BudgetController;
import adapter.BudgetPresenter;
import adapter.BudgetViewModel;
import adapter.TransactionController;
import adapter.TransactionPresenter;
import adapter.TransactionViewModel;
import framework.BankOfCanadaExchangeRateGateway;
import framework.InMemoryTransactionDataAccess;
import framework.TransactionSpendingDataAccess;
import framework.TransactionsPanel;
import framework.BudgetPanel;
import framework.InMemoryBudgetDataAccess;
import framework.InMemoryCategoryDataAccess;
import framework.PlaceholderPane;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import usecase.check_budget.CheckBudgetInteractor;
import usecase.port.BudgetDataAccess;
import usecase.port.CategoryDataAccess;
import usecase.port.SpendingDataAccess;
import usecase.set_budget.SetBudgetInteractor;
import usecase.add_transaction.AddTransactionInteractor;
import usecase.port.TransactionDataAccess;

import java.time.Clock;

/**
 * The only place that knows every concrete class. It creates the data access
 * objects, the use case Interactors, the Presenters/Controllers and the views,
 * and connects them (dependency injection). Everything else depends on
 * interfaces only.
 *
 * Each teammate adds their own wiring method here and swaps their tab in
 * buildTabs(). Keep your method small: create, connect, return the view.
 */
public class ApplicationBuilder {

    // TODO: read from Person A's SetBaseCurrency setting.
    private static final String BASE_CURRENCY = "CAD";

    // Shared data access. Person C replaces the in-memory versions with JSON storage.
    private final BudgetDataAccess budgetStorage = new InMemoryBudgetDataAccess();
    private final CategoryDataAccess categories = new InMemoryCategoryDataAccess();
    // Person A: replace with an adapter over TransactionDataAccess.
    private final TransactionDataAccess transactions = new InMemoryTransactionDataAccess();
    private final SpendingDataAccess spending = new TransactionSpendingDataAccess(transactions);

    public TabPane buildTabs() {
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().addAll(
                tab("Transactions", buildTransactionsView()),
                tab("Budget", buildBudgetView()),
                tab("Summary", buildSummaryView()));
        tabs.getSelectionModel().select(0);
        return tabs;
    }

    /** Person B (budgets and alerts). */
    private Node buildBudgetView() {
        BudgetViewModel viewModel = new BudgetViewModel();
        BudgetPresenter presenter = new BudgetPresenter(viewModel);
        BudgetController controller = new BudgetController(
                new SetBudgetInteractor(budgetStorage, categories, presenter),
                new CheckBudgetInteractor(budgetStorage, spending, presenter),
                BASE_CURRENCY);
        return new BudgetPanel(controller, viewModel, categories.findAllNames());
    }

    /** Person A: one shared repository supplies both Transactions and Budget. */
    private Node buildTransactionsView() {
        TransactionViewModel viewModel = new TransactionViewModel();
        TransactionPresenter presenter = new TransactionPresenter(viewModel);
        TransactionController controller = new TransactionController(new AddTransactionInteractor(
                transactions, categories, new BankOfCanadaExchangeRateGateway(), presenter,
                Clock.systemDefaultZone()));
        return new TransactionsPanel(controller, viewModel, categories.findAllNames());
    }

    /** Person C: replace with the real Summary panel (GetMonthlySummary, ExportCsv). */
    private Node buildSummaryView() {
        return new PlaceholderPane("Summary", "Person C",
                "Monthly income, spending, balance and category shares; export to CSV.");
    }

    private static Tab tab(String title, Node content) {
        Tab tab = new Tab(title, content);
        tab.setClosable(false);
        return tab;
    }

}
