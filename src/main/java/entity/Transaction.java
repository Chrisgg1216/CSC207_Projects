package entity;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

/** Immutable transaction with its original amount and optional CAD reference quote. */
public record Transaction(String id, LocalDate date, TransactionType type,
                          String categoryName, Money originalMoney,
                          Optional<ExchangeRate> exchangeRate, String note) {
    public Transaction {
        Objects.requireNonNull(id);
        Objects.requireNonNull(date);
        Objects.requireNonNull(type);
        categoryName = new Category(categoryName).getName();
        Objects.requireNonNull(originalMoney);
        Objects.requireNonNull(exchangeRate);
        note = note == null ? "" : note.trim();
        if (id.isBlank() || !originalMoney.isPositive()) {
            throw new IllegalArgumentException("A transaction needs an ID and a positive amount.");
        }
        if (exchangeRate.isPresent()) {
            ExchangeRate quote = exchangeRate.get();
            if (!quote.currency().equals(originalMoney.getCurrency()) || quote.observedDate().isAfter(date)) {
                throw new IllegalArgumentException("The quote must match the currency and not follow the transaction date.");
            }
            if (originalMoney.getCurrency().equals("CAD")
                    && quote.cadPerUnit().compareTo(java.math.BigDecimal.ONE) != 0) {
                throw new IllegalArgumentException("CAD uses an identity exchange rate.");
            }
        }
    }

    public Optional<Money> convertedMoney() {
        return exchangeRate.map(rate -> new Money(
                originalMoney.getAmount().multiply(rate.cadPerUnit()), "CAD"));
    }
}
