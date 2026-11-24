package adapter;

import entity.Transaction;
import usecase.export_csv.ExportCsvOutputBoundary;
import usecase.monthly_summary.MonthlySummaryOutputBoundary;
import usecase.monthly_summary.MonthlySummaryOutputData;

import java.time.format.DateTimeFormatter;
import java.util.List;

public final class MonthlySummaryPresenter implements MonthlySummaryOutputBoundary, ExportCsvOutputBoundary {
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("MMMM uuuu");
    private final MonthlySummaryViewModel viewModel;

    public MonthlySummaryPresenter(MonthlySummaryViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void prepareSuccessView(MonthlySummaryOutputData summary) {
        String totals = "Income: " + summary.income()
                + "     Spending: " + summary.spending()
                + "     Net: " + summary.income().subtract(summary.spending());
        List<String> categories = summary.categoryTotals().stream()
                .map(category -> category.category() + "  |  " + category.spending())
                .toList();
        List<String> transactions = summary.transactions().stream()
                .map(MonthlySummaryPresenter::formatTransaction)
                .toList();
        String message = "";
        if (summary.pendingConversions() > 0) {
            message = "Totals are partial. Pending conversions excluded: " + summary.pendingConversions() + ".";
        } else if (transactions.isEmpty()) {
            message = "No transactions for this month.";
        }
        viewModel.setState(new MonthlySummaryViewModel.State(summary.month().format(MONTH_FORMAT),
                totals, categories, transactions, message));
    }

    @Override
    public void showExportResult(String message) {
        viewModel.showExportResult(message);
    }

    private static String formatTransaction(Transaction transaction) {
        String amount = transaction.convertedMoney()
                .map(converted -> transaction.originalMoney().getCurrency().equals(converted.getCurrency())
                        ? converted.toString()
                        : transaction.originalMoney() + " (~" + converted + ")")
                .orElseGet(() -> transaction.originalMoney() + " (conversion pending)");
        return transaction.date() + "  |  " + transaction.type() + "  |  "
                + transaction.categoryName() + "  |  " + amount
                + (transaction.note().isEmpty() ? "" : "  |  " + transaction.note());
    }

    @Override
    public void prepareFailView(String message) {
        viewModel.setState(new MonthlySummaryViewModel.State("Monthly summary", "", List.of(), List.of(), message));
    }
}
