package framework;

import adapter.BudgetPresenter;
import adapter.BudgetViewModel;
import entity.Budget;
import entity.Category;
import entity.ExchangeRate;
import entity.Money;
import entity.Transaction;
import entity.TransactionType;
import org.junit.jupiter.api.Test;
import usecase.check_budget.CheckBudgetInputData;
import usecase.check_budget.CheckBudgetInteractor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TransactionSpendingDataAccessTest {
    private final InMemoryTransactionDataAccess store = new InMemoryTransactionDataAccess();
    private final TransactionSpendingDataAccess spending = new TransactionSpendingDataAccess(store);
    private final LocalDate date = LocalDate.of(2026, 11, 19);
    private final YearMonth month = YearMonth.of(2026, 11);

    private void add(String id, LocalDate day, TransactionType type, String category,
                     String currency, String amount, String rate) {
        store.save(new Transaction(id, day, type, category, new Money(new BigDecimal(amount), currency),
                rate == null ? Optional.empty() : Optional.of(new ExchangeRate(currency,
                        new BigDecimal(rate), day, "Test")), ""));
    }

    @Test
    void totalsFilterTypeCategoryMonthAndPending() {
        add("1", date, TransactionType.SPENDING, "Food", "CAD", "30", "1");
        add("2", date, TransactionType.SPENDING, "food", "USD", "20", "1.4");
        add("3", date, TransactionType.INCOME, "Food", "CAD", "100", "1");
        add("4", date, TransactionType.SPENDING, "Rent", "CAD", "200", "1");
        add("5", date.minusMonths(1), TransactionType.SPENDING, "Food", "CAD", "300", "1");
        add("6", date, TransactionType.SPENDING, "Food", "USD", "50", null);
        add("7", date.minusMonths(1), TransactionType.SPENDING, "Food", "USD", "50", null);
        assertEquals(new Money(new BigDecimal("58"), "CAD"), spending.totalSpent(" FOOD ", month, "CAD"));
        assertEquals(1, spending.countPendingConversion(month));
        assertEquals(Money.zero("CAD"), spending.totalSpent("Other", month, "CAD"));
    }

    @Test
    void sharedRecordsDriveRealBudgetStatus() {
        var budgets = new InMemoryBudgetDataAccess();
        budgets.save(new Budget(new Category("Food"), month, new Money(new BigDecimal("100"), "CAD")));
        var model = new BudgetViewModel();
        var check = new CheckBudgetInteractor(budgets, spending, new BudgetPresenter(model));
        add("1", date, TransactionType.SPENDING, "Food", "CAD", "30", "1");
        check.execute(new CheckBudgetInputData(month, "CAD"));
        assertTrue(model.getLines().get(0).contains("30.00 of 100.00"));
        assertTrue(model.getLines().get(0).contains("70.00 left"));
        add("2", date, TransactionType.SPENDING, "Food", "CAD", "60", "1");
        check.execute(new CheckBudgetInputData(month, "CAD"));
        assertTrue(model.getLines().get(0).contains("[WARNING]"));
    }

    @Test
    void refusesToRelabelCadAsAnotherCurrency() {
        assertThrows(IllegalArgumentException.class, () -> spending.totalSpent("Food", month, "USD"));
    }
}
