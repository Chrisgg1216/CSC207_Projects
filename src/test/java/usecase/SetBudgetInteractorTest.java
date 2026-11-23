package usecase;

import entity.Money;
import framework.InMemoryBudgetDataAccess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import usecase.TestSupport.FakeCategories;
import usecase.TestSupport.SetCapture;
import usecase.set_budget.SetBudgetInputData;
import usecase.set_budget.SetBudgetInteractor;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static usecase.TestSupport.DEC;
import static usecase.TestSupport.NOV;

class SetBudgetInteractorTest {

    private InMemoryBudgetDataAccess budgets;
    private SetCapture out;
    private SetBudgetInteractor interactor;

    @BeforeEach
    void setUp() {
        budgets = new InMemoryBudgetDataAccess();
        out = new SetCapture();
        interactor = new SetBudgetInteractor(budgets, new FakeCategories("Food", "Rent"), out);
    }

    private void run(String category, java.time.YearMonth month, String limit) {
        interactor.execute(new SetBudgetInputData(category, month,
                limit == null ? null : new BigDecimal(limit), "CAD"));
    }

    @Test
    void validBudgetIsSaved() {
        run("Food", NOV, "200");

        assertNull(out.error);
        assertNotNull(out.success);
        assertFalse(out.success.isReplacedExisting());
        assertEquals(new Money(new BigDecimal("200"), "CAD"),
                budgets.find("Food", NOV).orElseThrow().getLimit());
    }

    @Test
    void settingAgainReplacesTheOldLimit() {
        run("Food", NOV, "200");
        run("Food", NOV, "250");

        assertTrue(out.success.isReplacedExisting());
        assertEquals(1, budgets.findByMonth(NOV).size());
        assertEquals(new Money(new BigDecimal("250"), "CAD"),
                budgets.find("Food", NOV).orElseThrow().getLimit());
    }

    @Test
    void sameCategoryInAnotherMonthIsSeparate() {
        run("Food", NOV, "200");
        run("Food", DEC, "300");

        assertEquals(1, budgets.findByMonth(NOV).size());
        assertEquals(1, budgets.findByMonth(DEC).size());
    }

    @Test
    void categoryNameIsMatchedIgnoringCase() {
        run("food", NOV, "200");

        assertNull(out.error);
        assertTrue(budgets.find("Food", NOV).isPresent());
    }

    @Test
    void zeroLimitIsRejected() {
        run("Food", NOV, "0");

        assertNotNull(out.error);
        assertTrue(budgets.findByMonth(NOV).isEmpty());
    }

    @Test
    void negativeLimitIsRejected() {
        run("Food", NOV, "-5");

        assertNotNull(out.error);
        assertTrue(budgets.findByMonth(NOV).isEmpty());
    }

    @Test
    void missingOrUnreadableLimitIsRejected() {
        run("Food", NOV, null);

        assertNotNull(out.error);
        assertTrue(budgets.findByMonth(NOV).isEmpty());
    }

    @Test
    void limitThatRoundsToZeroIsRejected() {
        run("Food", NOV, "0.001");

        assertNotNull(out.error);
        assertTrue(budgets.findByMonth(NOV).isEmpty());
    }

    @Test
    void unknownCategoryIsRejected() {
        run("Gadgets", NOV, "100");

        assertNotNull(out.error);
        assertTrue(budgets.findByMonth(NOV).isEmpty());
    }

    @Test
    void blankCategoryIsRejected() {
        run("  ", NOV, "100");

        assertNotNull(out.error);
    }

    @Test
    void missingMonthIsRejected() {
        run("Food", null, "100");

        assertNotNull(out.error);
    }

    @Test
    void failedAttemptKeepsTheOldBudget() {
        run("Food", NOV, "200");
        run("Food", NOV, "-1");

        assertEquals(new Money(new BigDecimal("200"), "CAD"),
                budgets.find("Food", NOV).orElseThrow().getLimit());
    }
}
