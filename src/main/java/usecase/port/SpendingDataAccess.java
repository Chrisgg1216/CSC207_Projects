package usecase.port;

import entity.Money;

import java.time.YearMonth;

/**
 * Narrow port: only what budgets need to know about spending.
 *
 * Person A owns transactions. Their TransactionDataAccess (or a small adapter
 * on top of it) should implement this. totalSpent must return the sum of the
 * category's SPENDING transactions for the month, already converted to the
 * requested base currency. Transactions still waiting for an exchange rate are
 * left out of the total and counted by countPendingConversion instead.
 */
public interface SpendingDataAccess {

    Money totalSpent(String categoryName, YearMonth month, String baseCurrency);

    int countPendingConversion(YearMonth month);
}
