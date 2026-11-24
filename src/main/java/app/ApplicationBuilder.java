package app;

import adapter.BudgetController;
import adapter.BudgetPresenter;
import adapter.BudgetViewModel;
import adapter.CategoryController;
import adapter.CategoryPresenter;
import adapter.CategoryViewModel;
import adapter.ExportCsvController;
import adapter.MonthlySummaryController;
import adapter.MonthlySummaryPresenter;
import adapter.MonthlySummaryViewModel;
import adapter.TransactionController;
import adapter.TransactionPresenter;
import adapter.TransactionViewModel;
import framework.BankOfCanadaExchangeRateGateway;
import framework.BudgetPanel;
import framework.CategoriesPanel;
import framework.CsvTransactionWriter;
import framework.JsonLedgerStore;
import framework.MonthlySummaryPanel;
import framework.SpendingEventBus;
import framework.TransactionSpendingDataAccess;
import framework.TransactionsPanel;
import usecase.add_transaction.AddTransactionInteractor;
import usecase.check_budget.CheckBudgetInteractor;
import usecase.export_csv.ExportCsvInteractor;
import usecase.manage_categories.CategoryManagementInteractor;
import usecase.monthly_summary.MonthlySummaryInteractor;
import usecase.set_budget.SetBudgetInteractor;

import javax.swing.JTabbedPane;
import java.nio.file.Path;
import java.time.Clock;

/** Creates data access, use cases, presenters, controllers and views, and connects them. */
public class ApplicationBuilder {
    private static final String BASE_CURRENCY = "CAD";

    private final JsonLedgerStore ledger;
    private final SpendingEventBus spendingEvents = new SpendingEventBus();

    public ApplicationBuilder() {
        this(Path.of(System.getProperty("user.home"), ".csc207-budget", "ledger.json"));
    }

    public ApplicationBuilder(Path ledgerPath) {
        ledger = new JsonLedgerStore(ledgerPath);
    }

    public JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        TransactionsPanel transactions = buildTransactionsView();
        BudgetPanel budget = buildBudgetView();
        MonthlySummaryPanel summary = buildSummaryView();

        tabs.addTab("Transactions", transactions);
        tabs.addTab("Budget", budget);
        tabs.addTab("Summary", summary);
        tabs.addTab("Categories", buildCategoriesView());
        tabs.addChangeListener(event -> {
            if (tabs.getSelectedComponent() == summary) {
                summary.refresh();
            } else if (tabs.getSelectedComponent() == transactions) {
                transactions.refresh(ledger.findAllNames());
            } else if (tabs.getSelectedComponent() == budget) {
                budget.refresh(ledger.findAllNames());
            }
        });
        return tabs;
    }

    private BudgetPanel buildBudgetView() {
        BudgetViewModel viewModel = new BudgetViewModel();
        BudgetPresenter presenter = new BudgetPresenter(viewModel);
        BudgetController controller = new BudgetController(
                new SetBudgetInteractor(ledger, ledger, presenter),
                new CheckBudgetInteractor(ledger, new TransactionSpendingDataAccess(ledger), presenter),
                BASE_CURRENCY);
        BudgetPanel panel = new BudgetPanel(controller, viewModel, ledger.findAllNames());
        spendingEvents.addListener(panel::onSpendingChanged);
        return panel;
    }

    private TransactionsPanel buildTransactionsView() {
        TransactionViewModel viewModel = new TransactionViewModel();
        TransactionPresenter presenter = new TransactionPresenter(viewModel);
        TransactionController controller = new TransactionController(new AddTransactionInteractor(
                ledger, ledger, new BankOfCanadaExchangeRateGateway(), presenter, Clock.systemDefaultZone(), spendingEvents));
        return new TransactionsPanel(controller, viewModel, ledger.findAllNames());
    }

    private MonthlySummaryPanel buildSummaryView() {
        MonthlySummaryViewModel viewModel = new MonthlySummaryViewModel();
        MonthlySummaryPresenter presenter = new MonthlySummaryPresenter(viewModel);
        MonthlySummaryController controller = new MonthlySummaryController(
                new MonthlySummaryInteractor(ledger, presenter, BASE_CURRENCY));
        ExportCsvController export = new ExportCsvController(
                new ExportCsvInteractor(ledger, new CsvTransactionWriter(), presenter));
        return new MonthlySummaryPanel(controller, viewModel, export);
    }

    private CategoriesPanel buildCategoriesView() {
        CategoryViewModel viewModel = new CategoryViewModel();
        CategoryPresenter presenter = new CategoryPresenter(viewModel);
        CategoryController controller = new CategoryController(new CategoryManagementInteractor(ledger, presenter));
        return new CategoriesPanel(controller, viewModel);
    }
}
