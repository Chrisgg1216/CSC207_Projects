package usecase;

import entity.ExchangeRate;
import entity.TransactionType;
import framework.InMemoryCategoryDataAccess;
import framework.InMemoryTransactionDataAccess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import usecase.add_transaction.AddTransactionInputData;
import usecase.add_transaction.AddTransactionInteractor;
import usecase.add_transaction.AddTransactionOutputBoundary;
import usecase.add_transaction.AddTransactionOutputData;
import usecase.port.ExchangeRateGateway;
import usecase.port.SpendingChangeNotifier;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Adding a transaction must tell the notifier, so other screens (Budget) can update themselves. */
class AddTransactionNotificationTest {

    private final List<YearMonth> notified = new ArrayList<>();
    private final SpendingChangeNotifier notifier = new SpendingChangeNotifier() {
        @Override
        public void addListener(usecase.port.SpendingChangeListener listener) {
        }

        @Override
        public void spendingChanged(YearMonth month) {
            notified.add(month);
        }
    };
    private final List<String> errors = new ArrayList<>();
    private final AddTransactionOutputBoundary presenter = new AddTransactionOutputBoundary() {
        @Override
        public void prepareSuccessView(AddTransactionOutputData output) {
        }

        @Override
        public void prepareFailView(String message) {
            errors.add(message);
        }
    };
    private ExchangeRateGateway rates = (currency, date) ->
            Optional.of(new ExchangeRate(currency, new BigDecimal("1.5"), date, "Test"));
    private AddTransactionInteractor interactor;

    @BeforeEach
    void setUp() {
        build();
    }

    private void build() {
        interactor = new AddTransactionInteractor(new InMemoryTransactionDataAccess(),
                new InMemoryCategoryDataAccess(), rates, presenter,
                Clock.fixed(Instant.parse("2026-11-24T12:00:00Z"), ZoneOffset.UTC), notifier);
    }

    private void add(String date, String category, String amount, String currency) {
        interactor.execute(new AddTransactionInputData(LocalDate.parse(date), TransactionType.SPENDING,
                category, amount == null ? null : new BigDecimal(amount), currency, ""));
    }

    @Test
    void notifiesWithTheMonthOfTheTransaction() {
        add("2026-11-10", "Food", "20", "CAD");

        assertEquals(List.of(YearMonth.of(2026, 11)), notified);
    }

    @Test
    void usesTheTransactionDateNotToday() {
        add("2026-10-31", "Food", "20", "CAD");

        assertEquals(List.of(YearMonth.of(2026, 10)), notified);
    }

    @Test
    void notifiesOnceForEachSavedTransaction() {
        add("2026-11-10", "Food", "20", "CAD");
        add("2026-11-11", "Rent", "30", "USD");

        assertEquals(2, notified.size());
    }

    @Test
    void doesNotNotifyWhenValidationFails() {
        add("2026-11-10", "Food", "-5", "CAD");
        add("2026-11-10", "Nonexistent", "5", "CAD");
        add("2026-12-30", "Food", "5", "CAD"); // in the future

        assertTrue(notified.isEmpty());
        assertEquals(3, errors.size());
    }

    @Test
    void stillNotifiesWhenTheRateIsMissingBecauseThePendingCountChanged() {
        rates = (currency, date) -> Optional.empty();
        build();

        add("2026-11-10", "Food", "20", "USD");

        assertEquals(List.of(YearMonth.of(2026, 11)), notified);
    }

    @Test
    void worksWithoutANotifier() {
        AddTransactionInteractor plain = new AddTransactionInteractor(new InMemoryTransactionDataAccess(),
                new InMemoryCategoryDataAccess(), rates, presenter,
                Clock.fixed(Instant.parse("2026-11-24T12:00:00Z"), ZoneOffset.UTC));

        plain.execute(new AddTransactionInputData(LocalDate.parse("2026-11-10"), TransactionType.SPENDING,
                "Food", new BigDecimal("20"), "CAD", ""));

        assertTrue(errors.isEmpty());
    }
}
