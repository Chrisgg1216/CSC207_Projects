package usecase.add_transaction;

import entity.ExchangeRate;
import entity.Money;
import entity.Transaction;
import usecase.port.CategoryDataAccess;
import usecase.port.ExchangeRateGateway;
import usecase.port.ExchangeRateUnavailableException;
import usecase.port.SpendingChangeNotifier;
import usecase.port.LedgerAccessException;
import usecase.port.TransactionDataAccess;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Add one transaction; an unavailable quote never silently becomes a zero amount. */
public class AddTransactionInteractor implements AddTransactionInputBoundary {
    private final TransactionDataAccess transactions;
    private final CategoryDataAccess categories;
    private final ExchangeRateGateway rates;
    private final AddTransactionOutputBoundary presenter;
    private final Clock clock;
    private final SpendingChangeNotifier notifier;

    public AddTransactionInteractor(TransactionDataAccess transactions, CategoryDataAccess categories,
                                    ExchangeRateGateway rates, AddTransactionOutputBoundary presenter,
                                    Clock clock) {
        this(transactions, categories, rates, presenter, clock, SpendingChangeNotifier.NONE);
    }

    /** The notifier is told after each saved transaction, so other screens can update themselves. */
    public AddTransactionInteractor(TransactionDataAccess transactions, CategoryDataAccess categories,
                                    ExchangeRateGateway rates, AddTransactionOutputBoundary presenter,
                                    Clock clock, SpendingChangeNotifier notifier) {
        this.transactions = transactions;
        this.categories = categories;
        this.rates = rates;
        this.presenter = presenter;
        this.clock = clock;
        this.notifier = notifier;
    }

    @Override
    public void execute(AddTransactionInputData input) {
        String error = validate(input);
        if (error != null) {
            presenter.prepareFailView(error);
            return;
        }
        String currency = input.currency().trim().toUpperCase(Locale.ROOT);
        Optional<ExchangeRate> quote;
        if (currency.equals("CAD")) {
            quote = Optional.of(new ExchangeRate("CAD", BigDecimal.ONE, input.date(), "Identity"));
        } else {
            try {
                quote = rates.findRate(currency, input.date());
            } catch (ExchangeRateUnavailableException ex) {
                quote = Optional.empty();
            }
        }
        Transaction transaction = new Transaction(UUID.randomUUID().toString(), input.date(), input.type(),
                input.categoryName(), new Money(input.amount(), currency), quote, input.note());
        try {
            transactions.save(transaction);
        } catch (LedgerAccessException ex) {
            presenter.prepareFailView(ex.getMessage());
            return;
        }
        String message = quote.isPresent() ? "Transaction added. CAD amounts are reference estimates."
                : "Transaction added [PENDING CONVERSION]. No quote available; excluded from budget totals.";
        presenter.prepareSuccessView(new AddTransactionOutputData(transactions.findAll(), message));
        notifier.spendingChanged(YearMonth.from(input.date()));
    }

    private String validate(AddTransactionInputData input) {
        if (input == null || input.date() == null || input.date().isAfter(LocalDate.now(clock))) {
            return "Choose a valid transaction date, not in the future.";
        }
        if (input.type() == null) {
            return "Choose income or spending.";
        }
        if (input.categoryName() == null || input.categoryName().isBlank()
                || !categories.exists(input.categoryName().trim())) {
            return "Choose an existing category.";
        }
        if (input.amount() == null || input.amount().signum() <= 0
                || input.amount().stripTrailingZeros().scale() > 2) {
            return "Amount must be positive with at most two decimal places.";
        }
        if (input.currency() == null
                || !Set.of("CAD", "USD", "CNY").contains(input.currency().trim().toUpperCase(Locale.ROOT))) {
            return "Choose CAD, USD or CNY.";
        }
        return null;
    }

    @Override
    public void refresh() {
        presenter.prepareSuccessView(new AddTransactionOutputData(transactions.findAll(), ""));
    }
}
