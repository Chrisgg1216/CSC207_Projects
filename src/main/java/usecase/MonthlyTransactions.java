package usecase;

import entity.Transaction;
import usecase.port.TransactionDataAccess;

import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;

/** One selection and ordering rule for the summary and its CSV export. */
public final class MonthlyTransactions {
    private MonthlyTransactions() { }

    public static List<Transaction> select(TransactionDataAccess transactions, YearMonth month) {
        return transactions.findAll().stream()
                .filter(transaction -> YearMonth.from(transaction.date()).equals(month))
                .sorted(Comparator.comparing(Transaction::date).reversed().thenComparing(Transaction::id))
                .toList();
    }
}
