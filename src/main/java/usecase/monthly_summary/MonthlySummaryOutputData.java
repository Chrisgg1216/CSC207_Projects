package usecase.monthly_summary;

import entity.Money;
import entity.Transaction;

import java.time.YearMonth;
import java.util.List;

public record MonthlySummaryOutputData(YearMonth month, Money income, Money spending,
                                       List<CategoryTotal> categoryTotals, List<Transaction> transactions,
                                       int pendingConversions) {
    public MonthlySummaryOutputData {
        categoryTotals = List.copyOf(categoryTotals);
        transactions = List.copyOf(transactions);
    }

    public record CategoryTotal(String category, Money spending) { }
}
