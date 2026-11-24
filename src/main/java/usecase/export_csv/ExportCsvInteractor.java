package usecase.export_csv;

import entity.Transaction;
import usecase.MonthlyTransactions;
import usecase.port.LedgerAccessException;
import usecase.port.TransactionCsvWriter;
import usecase.port.TransactionDataAccess;

import java.time.YearMonth;
import java.util.List;

public final class ExportCsvInteractor implements ExportCsvInputBoundary {
    private final TransactionDataAccess transactions;
    private final TransactionCsvWriter writer;
    private final ExportCsvOutputBoundary presenter;

    public ExportCsvInteractor(TransactionDataAccess transactions, TransactionCsvWriter writer,
                               ExportCsvOutputBoundary presenter) {
        this.transactions = transactions;
        this.writer = writer;
        this.presenter = presenter;
    }

    @Override
    public void execute(YearMonth month, String destination) {
        if (month == null || destination == null || destination.isBlank()) {
            presenter.showExportResult("Choose a month and a destination for the CSV.");
            return;
        }
        try {
            List<Transaction> rows = MonthlyTransactions.select(transactions, month);
            writer.write(destination, rows);
            presenter.showExportResult("Exported " + rows.size() + " transaction(s) for " + month + ".");
        } catch (LedgerAccessException ex) {
            presenter.showExportResult(ex.getMessage());
        }
    }
}
