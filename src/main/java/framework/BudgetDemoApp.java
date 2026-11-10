package framework;

import adapter.BudgetController;
import adapter.BudgetPresenter;
import adapter.BudgetViewModel;
import entity.Money;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import usecase.check_budget.CheckBudgetInteractor;
import usecase.port.CategoryDataAccess;
import usecase.port.SpendingDataAccess;
import usecase.set_budget.SetBudgetInteractor;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

/**
 * Stand-alone demo of the Budget tab with fake spending and categories, so you
 * can run and show your part before Person A's transactions and Person C's
 * categories exist. In the real app, ApplicationBuilder wires the real ones.
 */
public class BudgetDemoApp extends Application {

    private static final String BASE_CURRENCY = "CAD";

    @Override
    public void start(Stage stage) {
        List<String> categories = List.of("Food", "Rent", "Transport", "Fun");

        CategoryDataAccess categoryAccess = new CategoryDataAccess() {
            @Override
            public boolean exists(String name) {
                return categories.stream().anyMatch(c -> c.equalsIgnoreCase(name));
            }

            @Override
            public List<String> findAllNames() {
                return categories;
            }
        };

        SpendingDataAccess fakeSpending = new SpendingDataAccess() {
            @Override
            public Money totalSpent(String category, YearMonth month, String currency) {
                switch (category.toLowerCase()) {
                    case "food":
                        return new Money(new BigDecimal("170.00"), currency);
                    case "rent":
                        return new Money(new BigDecimal("1250.00"), currency);
                    case "transport":
                        return new Money(new BigDecimal("40.00"), currency);
                    default:
                        return Money.zero(currency);
                }
            }

            @Override
            public int countPendingConversion(YearMonth month) {
                return 1;
            }
        };

        InMemoryBudgetDataAccess budgets = new InMemoryBudgetDataAccess();
        BudgetViewModel viewModel = new BudgetViewModel();
        BudgetPresenter presenter = new BudgetPresenter(viewModel);
        BudgetController controller = new BudgetController(
                new SetBudgetInteractor(budgets, categoryAccess, presenter),
                new CheckBudgetInteractor(budgets, fakeSpending, presenter),
                BASE_CURRENCY);

        stage.setTitle("Budget (demo)");
        stage.setScene(new Scene(new BudgetPanel(controller, viewModel, categories), 900, 420));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
