package usecase.monthly_summary;

import entity.Money;
import entity.Transaction;
import entity.TransactionType;
import usecase.port.TransactionDataAccess;

import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public final class MonthlySummaryInteractor implements MonthlySummaryInputBoundary {
    private final TransactionDataAccess transactions;
    private final MonthlySummaryOutputBoundary presenter;
    private final String baseCurrency;

    public MonthlySummaryInteractor(TransactionDataAccess transactions,
                                    MonthlySummaryOutputBoundary presenter, String baseCurrency) {
        this.transactions = transactions;
        this.presenter = presenter;
        this.baseCurrency = baseCurrency;
    }

    @Override
    public void execute(YearMonth month) {
        if (month == null) {
            presenter.prepareFailView("Choose a month to view.");
            return;
        }

        List<Transaction> monthlyTransactions = transactions.findAll().stream()
                .filter(transaction -> YearMonth.from(transaction.date()).equals(month))
                .sorted(Comparator.comparing(Transaction::date).reversed().thenComparing(Transaction::id))
                .toList();

        Money income = Money.zero(baseCurrency);
        Money spending = Money.zero(baseCurrency);
        int pendingConversions = 0;
        Map<String, Money> spendingByCategory = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Transaction transaction : monthlyTransactions) {
            Optional<Money> amount = transaction.convertedMoney();
            if (amount.isEmpty()) {
                pendingConversions++;
            } else if (transaction.type() == TransactionType.INCOME) {
                income = income.add(amount.get());
            } else {
                spending = spending.add(amount.get());
                spendingByCategory.merge(transaction.categoryName(), amount.get(), Money::add);
            }
        }

        List<MonthlySummaryOutputData.CategoryTotal> categoryTotals = spendingByCategory.entrySet().stream()
                .map(entry -> new MonthlySummaryOutputData.CategoryTotal(entry.getKey(), entry.getValue()))
                .toList();
        presenter.prepareSuccessView(new MonthlySummaryOutputData(
                month, income, spending, categoryTotals, monthlyTransactions, pendingConversions));
    }
}
