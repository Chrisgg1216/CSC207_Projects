package framework;

import entity.Money;
import entity.TransactionType;
import usecase.port.SpendingDataAccess;
import usecase.port.TransactionDataAccess;
import java.time.YearMonth;

/** Supplies Budget with actual transaction records, rather than fixed demo values. */
public class TransactionSpendingDataAccess implements SpendingDataAccess {
    private final TransactionDataAccess transactions;

    public TransactionSpendingDataAccess(TransactionDataAccess transactions) {
        this.transactions = transactions;
    }

    @Override
    public Money totalSpent(String categoryName, YearMonth month, String baseCurrency) {
        if (!"CAD".equalsIgnoreCase(baseCurrency)) {
            throw new IllegalArgumentException("The milestone prototype uses CAD as its base currency.");
        }
        return transactions.findAll().stream()
                .filter(t -> t.type() == TransactionType.SPENDING)
                .filter(t -> t.categoryName().equalsIgnoreCase(categoryName.trim()))
                .filter(t -> YearMonth.from(t.date()).equals(month))
                .flatMap(t -> t.convertedMoney().stream())
                .reduce(Money.zero("CAD"), Money::add);
    }

    @Override
    public int countPendingConversion(YearMonth month) {
        return (int) transactions.findAll().stream()
                .filter(t -> YearMonth.from(t.date()).equals(month))
                .filter(t -> t.convertedMoney().isEmpty()).count();
    }
}
