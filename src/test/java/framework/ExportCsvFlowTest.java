package framework;

import adapter.ExportCsvController;
import adapter.MonthlySummaryPresenter;
import adapter.MonthlySummaryViewModel;
import entity.ExchangeRate;
import entity.Money;
import entity.Transaction;
import entity.TransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import usecase.export_csv.ExportCsvInteractor;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExportCsvFlowTest {
    @TempDir
    Path temp;

    @Test
    void exportsOnlySelectedMonthWithExactFieldsAndPendingQuotes() throws Exception {
        JsonLedgerStore store = new JsonLedgerStore(temp.resolve("ledger.json"));
        LocalDate november = LocalDate.of(2026, 11, 10);
        ExchangeRate quote = new ExchangeRate("USD", new BigDecimal("1.38"), november.minusDays(1), "Test");
        store.save(transaction("pending", november.plusDays(1), Optional.empty(), "=1+1"));
        store.save(transaction("converted", november, Optional.of(quote), "咖啡, \"朋友\"\nLunch"));
        store.save(transaction("october", november.minusMonths(1), Optional.empty(), "October only"));

        var viewModel = new MonthlySummaryViewModel();
        var messages = new AtomicReference<String>();
        viewModel.addExportListener(event -> messages.set((String) event.getNewValue()));
        var controller = new ExportCsvController(new ExportCsvInteractor(
                store, new CsvTransactionWriter(), new MonthlySummaryPresenter(viewModel)));
        Path csv = temp.resolve("november.csv");

        controller.exportMonth(november, csv.toString());
        assertEquals("Exported 2 transaction(s) for 2026-11.", messages.get());
        String exported = Files.readString(csv);
        assertTrue(exported.contains("2026-11-11,SPENDING,\"Food\",10.00,\"USD\",,PENDING,,\"'=1+1\""));
        assertTrue(exported.contains("2026-11-10,SPENDING,\"Food\",10.00,\"USD\",13.80,CONVERTED,2026-11-09,"
                + "\"咖啡, \"\"朋友\"\"\nLunch\""));
        assertFalse(exported.contains("October only"));

        Path missingDirectory = temp.resolve("missing/november.csv");
        controller.exportMonth(november, missingDirectory.toString());
        assertTrue(messages.get().startsWith("Could not write the CSV"));
        assertFalse(Files.exists(missingDirectory));
    }

    private static Transaction transaction(String id, LocalDate date, Optional<ExchangeRate> quote, String note) {
        return new Transaction(id, date, TransactionType.SPENDING, "Food",
                new Money(new BigDecimal("10"), "USD"), quote, note);
    }
}
