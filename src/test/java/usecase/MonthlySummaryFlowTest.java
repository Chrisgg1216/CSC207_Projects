package usecase;

import adapter.MonthlySummaryController;
import adapter.MonthlySummaryPresenter;
import adapter.MonthlySummaryViewModel;
import entity.ExchangeRate;
import entity.Money;
import entity.Transaction;
import entity.TransactionType;
import framework.InMemoryTransactionDataAccess;
import org.junit.jupiter.api.Test;
import usecase.monthly_summary.MonthlySummaryInteractor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static entity.TransactionType.INCOME;
import static entity.TransactionType.SPENDING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MonthlySummaryFlowTest {
    private final InMemoryTransactionDataAccess store = new InMemoryTransactionDataAccess();
    private final MonthlySummaryViewModel viewModel = new MonthlySummaryViewModel();
    private final MonthlySummaryController controller = new MonthlySummaryController(
            new MonthlySummaryInteractor(store, new MonthlySummaryPresenter(viewModel), "CAD"));

    @Test
    void reportsOnlySelectedMonthAndUsesTheSavedExchangeRate() {
        save("old", "2026-10-31", SPENDING, "Food", "100", "CAD", "1");
        save("first", "2026-11-01", SPENDING, "Food", "20", "CAD", "1");
        save("foreign", "2026-11-02", SPENDING, "food", "20", "USD", "1.50");
        save("transport", "2026-11-30", SPENDING, "Transport", "10", "CAD", "1");
        save("pay", "2026-11-30", INCOME, "Other", "100", "CAD", "1");
        save("later", "2026-12-01", SPENDING, "Other", "100", "CAD", "1");

        controller.showMonth(LocalDate.parse("2026-11-15"));

        var state = viewModel.getState();
        assertEquals("Income: 100.00 CAD     Spending: 60.00 CAD     Net: 40.00 CAD", state.totals());
        assertEquals(List.of("food  |  50.00 CAD", "Transport  |  10.00 CAD"), state.categoryLines());
        assertEquals(4, state.transactionLines().size());
        assertTrue(state.transactionLines().stream().anyMatch(line -> line.contains("20.00 USD (~30.00 CAD)")));
        assertEquals("", state.message());
    }

    @Test
    void keepsPendingRowsVisibleAndClearsTheSummaryWhenTheMonthChanges() {
        save("pending", "2026-11-11", SPENDING, "Food", "75", "USD", null);
        save("posted", "2026-11-12", SPENDING, "Food", "15", "CAD", "1");
        controller.showMonth(LocalDate.parse("2026-11-01"));
        assertEquals(List.of("Food  |  15.00 CAD"), viewModel.getState().categoryLines());
        assertTrue(viewModel.getState().message().contains("Pending conversions excluded: 1"));
        assertTrue(viewModel.getState().transactionLines().stream()
                .anyMatch(line -> line.contains("75.00 USD (conversion pending)")));

        controller.showMonth(LocalDate.parse("2026-12-01"));
        assertEquals("Income: 0.00 CAD     Spending: 0.00 CAD     Net: 0.00 CAD", viewModel.getState().totals());
        assertTrue(viewModel.getState().transactionLines().isEmpty());
        assertFalse(viewModel.getState().message().contains("Pending"));
        assertEquals("No transactions for this month.", viewModel.getState().message());
    }

    private void save(String id, String dateText, TransactionType type, String category,
                      String amount, String currency, String rate) {
        LocalDate date = LocalDate.parse(dateText);
        Optional<ExchangeRate> quote = Optional.ofNullable(rate)
                .map(value -> new ExchangeRate(currency, new BigDecimal(value), date, "Test quote"));
        store.save(new Transaction(id, date, type, category,
                new Money(new BigDecimal(amount), currency), quote, ""));
    }
}
