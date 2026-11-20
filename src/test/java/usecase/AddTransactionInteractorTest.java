package usecase;

import adapter.TransactionController;
import adapter.TransactionPresenter;
import adapter.TransactionViewModel;
import entity.ExchangeRate;
import entity.Money;
import entity.TransactionType;
import framework.InMemoryCategoryDataAccess;
import framework.InMemoryTransactionDataAccess;
import framework.TransactionSpendingDataAccess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import usecase.add_transaction.AddTransactionInputData;
import usecase.add_transaction.AddTransactionInteractor;
import usecase.port.ExchangeRateGateway;
import usecase.port.ExchangeRateUnavailableException;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AddTransactionInteractorTest {
    private static final LocalDate DATE = LocalDate.of(2026, 11, 19);
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-19T12:00:00Z"), ZoneOffset.UTC);
    private InMemoryTransactionDataAccess store;
    private TransactionViewModel model;

    @BeforeEach
    void setUp() {
        store = new InMemoryTransactionDataAccess();
        model = new TransactionViewModel();
    }

    private AddTransactionInteractor interactor(ExchangeRateGateway gateway) {
        return new AddTransactionInteractor(store, new InMemoryCategoryDataAccess(), gateway,
                new TransactionPresenter(model), clock);
    }

    private AddTransactionInputData input(String amount, String currency) {
        return new AddTransactionInputData(DATE, TransactionType.SPENDING, "Food",
                amount == null ? null : new BigDecimal(amount), currency, "Lunch");
    }

    @Test
    void cadAdditionFlowsThroughPresenterAndBudgetWithoutNetwork() {
        var useCase = interactor((currency, date) -> { throw new AssertionError("CAD must not call API"); });
        var controller = new TransactionController(useCase);
        controller.add(DATE, TransactionType.SPENDING, "Food", "30", "CAD", "Lunch");
        assertFalse(model.isError());
        assertEquals(1, store.findAll().size());
        assertEquals(1, model.getLines().size());
        assertTrue(model.getLines().get(0).contains("30.00 CAD"));
        assertEquals(new Money(new BigDecimal("30"), "CAD"),
                new TransactionSpendingDataAccess(store).totalSpent("Food", YearMonth.from(DATE), "CAD"));
    }

    @Test
    void foreignAmountKeepsQuoteDateAndOriginalMoneyAndRoundsOnce() {
        interactor((currency, date) -> Optional.of(new ExchangeRate(currency,
                new BigDecimal("1.4317"), DATE.minusDays(1), "Test quote"))).execute(input("50", "usd"));
        var transaction = store.findAll().get(0);
        assertEquals(new Money(new BigDecimal("50"), "USD"), transaction.originalMoney());
        assertEquals(new Money(new BigDecimal("71.59"), "CAD"), transaction.convertedMoney().orElseThrow());
        assertEquals(DATE.minusDays(1), transaction.exchangeRate().orElseThrow().observedDate());
        assertTrue(model.getLines().get(0).contains("2026-11-18"));
    }

    @Test
    void apiFailureSavesPendingInsteadOfFakeZero() {
        interactor((currency, date) -> { throw new ExchangeRateUnavailableException("offline", null); })
                .execute(input("20", "USD"));
        assertEquals(1, store.findAll().size());
        assertTrue(store.findAll().get(0).convertedMoney().isEmpty());
        assertFalse(model.isError());
        assertTrue(model.getMessage().contains("PENDING CONVERSION"));
        var spending = new TransactionSpendingDataAccess(store);
        assertEquals(Money.zero("CAD"), spending.totalSpent("Food", YearMonth.from(DATE), "CAD"));
        assertEquals(1, spending.countPendingConversion(YearMonth.from(DATE)));
    }

    @Test
    void emptyQuoteAlsoSavesPending() {
        interactor((currency, date) -> Optional.empty()).execute(input("20", "CNY"));
        assertTrue(store.findAll().get(0).convertedMoney().isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"abc", "0", "-1", "0.001", "1.234", "1,000", "NaN"})
    void invalidAmountsNeverSave(String amount) {
        var controller = new TransactionController(interactor((c, d) -> Optional.empty()));
        controller.add(DATE, TransactionType.SPENDING, "Food", amount, "CAD", "");
        assertTrue(model.isError());
        assertTrue(store.findAll().isEmpty());
    }

    @Test
    void unknownCategoryRejectedBeforeApiCall() {
        var useCase = interactor((c, d) -> { throw new AssertionError("invalid input must not call API"); });
        useCase.execute(new AddTransactionInputData(DATE, TransactionType.SPENDING, "Unknown",
                BigDecimal.ONE, "USD", ""));
        assertTrue(model.isError());
        assertTrue(store.findAll().isEmpty());
    }

    @Test
    void futureDateRejected() {
        interactor((c, d) -> Optional.empty()).execute(new AddTransactionInputData(DATE.plusDays(1),
                TransactionType.SPENDING, "Food", BigDecimal.ONE, "CAD", ""));
        assertTrue(model.isError());
        assertTrue(store.findAll().isEmpty());
    }

    @Test
    void missingDateAndTypeAndUnsupportedCurrencyRejected() {
        var useCase = interactor((c, d) -> Optional.empty());
        useCase.execute(new AddTransactionInputData(null, TransactionType.SPENDING, "Food", BigDecimal.ONE, "CAD", ""));
        assertTrue(model.isError());
        useCase.execute(new AddTransactionInputData(DATE, null, "Food", BigDecimal.ONE, "CAD", ""));
        assertTrue(model.isError());
        useCase.execute(input("1", "EUR"));
        assertTrue(model.isError());
        assertTrue(store.findAll().isEmpty());
    }

    @Test
    void invalidSubmissionKeepsExistingListAndRefreshLoadsSameRepository() {
        var useCase = interactor((c, d) -> Optional.empty());
        useCase.execute(input("30", "CAD"));
        useCase.execute(input("-1", "CAD"));
        assertTrue(model.isError());
        assertEquals(1, model.getLines().size());
        useCase.refresh();
        assertFalse(model.isError());
        assertEquals(1, model.getLines().size());
    }

    @Test
    void separateIdenticalPurchasesHaveDifferentIds() {
        var useCase = interactor((c, d) -> Optional.empty());
        useCase.execute(input("30", "CAD"));
        useCase.execute(input("30", "CAD"));
        assertEquals(2, store.findAll().size());
        assertNotEquals(store.findAll().get(0).id(), store.findAll().get(1).id());
    }
}
