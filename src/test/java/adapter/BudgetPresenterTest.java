package adapter;

import entity.BudgetStatus;
import entity.Money;
import org.junit.jupiter.api.Test;
import usecase.check_budget.BudgetLine;
import usecase.check_budget.CheckBudgetOutputData;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BudgetPresenterTest {

    private static Money cad(String amount) {
        return new Money(new BigDecimal(amount), "CAD");
    }

    private static BudgetLine line(String limit, String spent, String percent, BudgetStatus status) {
        Money l = cad(limit);
        Money s = cad(spent);
        return new BudgetLine("Food", l, s, l.subtract(s), new BigDecimal(percent), status);
    }

    private static CheckBudgetOutputData output(int pending, BudgetLine... lines) {
        return new CheckBudgetOutputData(YearMonth.of(2026, 11), List.of(lines), pending);
    }

    @Test
    void warningIsWrittenAsAWordNotOnlyAColor() {
        BudgetViewModel vm = new BudgetViewModel();
        new BudgetPresenter(vm).prepareSuccessView(output(0, line("200", "170", "85.0", BudgetStatus.WARNING)));

        String text = vm.getLines().get(0);
        assertTrue(text.contains("[WARNING]"));
        assertTrue(text.contains("Food: 170.00 of 200.00 CAD used (85.0%)"));
        assertTrue(text.contains("30.00 left"));
    }

    @Test
    void overBudgetShowsHowMuchOver() {
        BudgetViewModel vm = new BudgetViewModel();
        new BudgetPresenter(vm).prepareSuccessView(output(0, line("100", "120", "120.0", BudgetStatus.OVER)));

        String text = vm.getLines().get(0);
        assertTrue(text.contains("[OVER]"));
        assertTrue(text.contains("over by 20.00"));
    }

    @Test
    void okLineSaysOk() {
        BudgetViewModel vm = new BudgetViewModel();
        new BudgetPresenter(vm).prepareSuccessView(output(0, line("100", "10", "10.0", BudgetStatus.OK)));

        assertTrue(vm.getLines().get(0).contains("[OK]"));
    }

    @Test
    void pendingTransactionsAreMentioned() {
        BudgetViewModel vm = new BudgetViewModel();
        new BudgetPresenter(vm).prepareSuccessView(output(2, line("100", "10", "10.0", BudgetStatus.OK)));

        assertTrue(vm.getMessage().contains("2 transaction(s) are waiting for currency conversion"));
        assertFalse(vm.isError());
    }

    @Test
    void emptyMonthSaysNoBudgets() {
        BudgetViewModel vm = new BudgetViewModel();
        new BudgetPresenter(vm).prepareSuccessView(output(0));

        assertTrue(vm.getLines().isEmpty());
        assertEquals("No budgets are set for 2026-11.", vm.getMessage());
    }

    @Test
    void failureIsFlaggedAsError() {
        BudgetViewModel vm = new BudgetViewModel();
        new BudgetPresenter(vm).prepareFailView("The limit must be a number greater than zero.");

        assertTrue(vm.isError());
        assertEquals("The limit must be a number greater than zero.", vm.getMessage());
    }
}
