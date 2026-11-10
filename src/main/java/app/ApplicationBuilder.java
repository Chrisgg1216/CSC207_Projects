package app;

import adapter.BudgetController;
import adapter.BudgetPresenter;
import adapter.BudgetViewModel;
import framework.BudgetPanel;
import framework.InMemoryBudgetDataAccess;
import framework.InMemoryCategoryDataAccess;
import framework.InMemorySpendingDataAccess;
import framework.PlaceholderPane;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import usecase.check_budget.CheckBudgetInteractor;
import usecase.port.BudgetDataAccess;
import usecase.port.CategoryDataAccess;
import usecase.port.SpendingDataAccess;
import usecase.set_budget.SetBudgetInteractor;

import java.time.YearMonth;

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
    private final SpendingDataAccess spending = createDemoSpending();

    public TabPane buildTabs() {
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().addAll(
                tab("Transactions", buildTransactionsView()),
                tab("Budget", buildBudgetView()),
                tab("Summary", buildSummaryView()));
        tabs.getSelectionModel().select(1);
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

    /** Person A: replace with the real Transactions panel (AddTransaction, Edit/Delete, FX). */
    private Node buildTransactionsView() {
        return new PlaceholderPane("Transactions", "Person A",
                "Add, edit and delete income and spending in any currency, with the exchange rate "
                        + "and converted amount shown.");
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

    /** Fixed demo numbers so the Budget tab can show OK, WARNING and OVER. */
    private static SpendingDataAccess createDemoSpending() {
        InMemorySpendingDataAccess demo = new InMemorySpendingDataAccess();
        YearMonth now = YearMonth.now();
        demo.setSpent("Food", now, "170.00");
        demo.setSpent("Rent", now, "1250.00");
        demo.setSpent("Transport", now, "40.00");
        demo.setPendingConversion(1);
        return demo;
    }
}
