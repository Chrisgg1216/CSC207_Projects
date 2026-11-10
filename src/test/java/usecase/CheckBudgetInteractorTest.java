package usecase;

import entity.Budget;
import entity.BudgetStatus;
import entity.Category;
import entity.Money;
import framework.InMemoryBudgetDataAccess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import usecase.TestSupport.CheckCapture;
import usecase.TestSupport.FakeSpending;
import usecase.check_budget.BudgetLine;
import usecase.check_budget.CheckBudgetInputData;
import usecase.check_budget.CheckBudgetInteractor;

import java.math.BigDecimal;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static usecase.TestSupport.DEC;
import static usecase.TestSupport.NOV;

class CheckBudgetInteractorTest {

    private InMemoryBudgetDataAccess budgets;
    private FakeSpending spending;
    private CheckCapture out;
    private CheckBudgetInteractor interactor;

    @BeforeEach
    void setUp() {
        budgets = new InMemoryBudgetDataAccess();
        spending = new FakeSpending();
        out = new CheckCapture();
        interactor = new CheckBudgetInteractor(budgets, spending, out);
    }

    private void budget(String category, YearMonth month, String limit) {
        budgets.save(new Budget(new Category(category), month, new Money(new BigDecimal(limit), "CAD")));
    }

    private BudgetLine checkFood(String limit, String spent) {
        budget("Food", NOV, limit);
        spending.spend("Food", NOV, spent);
        interactor.execute(new CheckBudgetInputData(NOV, "CAD"));
        assertNull(out.error);
        assertEquals(1, out.success.getLines().size());
        return out.success.getLines().get(0);
    }

    @Test
    void below80PercentIsOk() {
        assertEquals(BudgetStatus.OK, checkFood("100", "79.99").getStatus());
    }

    @Test
    void exactly80PercentIsWarning() {
        assertEquals(BudgetStatus.WARNING, checkFood("100", "80.00").getStatus());
    }

    @Test
    void exactly100PercentIsStillWarning() {
        BudgetLine line = checkFood("100", "100.00");

        assertEquals(BudgetStatus.WARNING, line.getStatus());
        assertEquals(new Money(BigDecimal.ZERO, "CAD"), line.getRemaining());
    }

    @Test
    void justAbove100PercentIsOver() {
        assertEquals(BudgetStatus.OVER, checkFood("100", "100.01").getStatus());
    }

    @Test
    void overBudgetHasNegativeRemaining() {
        BudgetLine line = checkFood("100", "120.00");

        assertEquals(new Money(new BigDecimal("-20"), "CAD"), line.getRemaining());
        assertEquals(new BigDecimal("120.0"), line.getPercentUsed());
    }

    @Test
    void spentRemainingAndPercentAreCalculated() {
        BudgetLine line = checkFood("200", "170");

        assertEquals(new Money(new BigDecimal("170"), "CAD"), line.getSpent());
        assertEquals(new Money(new BigDecimal("30"), "CAD"), line.getRemaining());
        assertEquals(new BigDecimal("85.0"), line.getPercentUsed());
        assertEquals(BudgetStatus.WARNING, line.getStatus());
    }

    @Test
    void thresholdUsesExactDecimalsNotDoubles() {
        // limit 33.33 x 0.80 = 26.664 exactly: 26.66 is below it, 26.67 is above it.
        assertEquals(BudgetStatus.OK, checkFood("33.33", "26.66").getStatus());
    }

    @Test
    void thresholdBoundaryJustAbove() {
        assertEquals(BudgetStatus.WARNING, checkFood("33.33", "26.67").getStatus());
    }

    @Test
    void noSpendingMeansZeroPercentAndOk() {
        budget("Food", NOV, "100");
        interactor.execute(new CheckBudgetInputData(NOV, "CAD"));

        BudgetLine line = out.success.getLines().get(0);
        assertEquals(BudgetStatus.OK, line.getStatus());
        assertEquals(new BigDecimal("0.0"), line.getPercentUsed());
    }

    @Test
    void categoryWithoutBudgetIsNotReported() {
        spending.spend("Fun", NOV, "500");
        interactor.execute(new CheckBudgetInputData(NOV, "CAD"));

        assertTrue(out.success.getLines().isEmpty());
    }

    @Test
    void spendingInAnotherMonthIsNotCounted() {
        budget("Food", NOV, "100");
        spending.spend("Food", DEC, "500");
        interactor.execute(new CheckBudgetInputData(NOV, "CAD"));

        assertEquals(BudgetStatus.OK, out.success.getLines().get(0).getStatus());
    }

    @Test
    void budgetFromAnotherMonthIsNotReported() {
        budget("Food", DEC, "100");
        interactor.execute(new CheckBudgetInputData(NOV, "CAD"));

        assertTrue(out.success.getLines().isEmpty());
    }

    @Test
    void linesAreSortedByCategoryName() {
        budget("Rent", NOV, "1000");
        budget("food", NOV, "200");
        budget("Transport", NOV, "50");
        interactor.execute(new CheckBudgetInputData(NOV, "CAD"));

        assertEquals("food", out.success.getLines().get(0).getCategoryName());
        assertEquals("Rent", out.success.getLines().get(1).getCategoryName());
        assertEquals("Transport", out.success.getLines().get(2).getCategoryName());
    }

    @Test
    void pendingConversionCountIsPassedThrough() {
        spending.pending = 3;
        interactor.execute(new CheckBudgetInputData(NOV, "CAD"));

        assertEquals(3, out.success.getPendingConversionCount());
    }

    @Test
    void budgetInAnotherCurrencyIsSkippedNotMixed() {
        budgets.save(new Budget(new Category("Food"), NOV, new Money(new BigDecimal("100"), "USD")));
        interactor.execute(new CheckBudgetInputData(NOV, "CAD"));

        assertTrue(out.success.getLines().isEmpty());
    }

    @Test
    void missingMonthFails() {
        interactor.execute(new CheckBudgetInputData(null, "CAD"));

        assertNotNull(out.error);
    }

    @Test
    void missingBaseCurrencyFails() {
        interactor.execute(new CheckBudgetInputData(NOV, " "));

        assertNotNull(out.error);
    }

    @Test
    void customThresholdIsUsed() {
        interactor = new CheckBudgetInteractor(budgets, spending, out, new BigDecimal("0.50"));
        budget("Food", NOV, "100");
        spending.spend("Food", NOV, "50");
        interactor.execute(new CheckBudgetInputData(NOV, "CAD"));

        assertEquals(BudgetStatus.WARNING, out.success.getLines().get(0).getStatus());
    }

    @Test
    void invalidThresholdIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new CheckBudgetInteractor(budgets, spending, out, BigDecimal.ONE));
        assertThrows(IllegalArgumentException.class,
                () -> new CheckBudgetInteractor(budgets, spending, out, BigDecimal.ZERO));
    }
}
