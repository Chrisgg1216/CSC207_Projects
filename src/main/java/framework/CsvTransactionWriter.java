package framework;

import entity.Money;
import entity.Transaction;
import usecase.port.LedgerAccessException;
import usecase.port.TransactionCsvWriter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public final class CsvTransactionWriter implements TransactionCsvWriter {
    private static final String HEADER =
            "date,type,category,original_amount,original_currency,cad_amount,conversion_status,rate_date,note";

    @Override
    public void write(String destination, List<Transaction> transactions) {
        try {
            Path path = Path.of(destination).toAbsolutePath().normalize();
            StringBuilder csv = new StringBuilder(HEADER).append("\r\n");
            for (Transaction transaction : transactions) {
                Optional<Money> converted = transaction.convertedMoney();
                csv.append(String.join(",", transaction.date().toString(), transaction.type().name(),
                        textCell(transaction.categoryName()), transaction.originalMoney().getAmount().toPlainString(),
                        textCell(transaction.originalMoney().getCurrency()),
                        converted.map(money -> money.getAmount().toPlainString()).orElse(""),
                        converted.isPresent() ? "CONVERTED" : "PENDING",
                        transaction.exchangeRate().map(rate -> rate.observedDate().toString()).orElse(""),
                        textCell(transaction.note()))).append("\r\n");
            }
            AtomicTextFile.write(path, csv.toString());
        } catch (IOException | IllegalArgumentException ex) {
            throw new LedgerAccessException("Could not write the CSV to " + destination + ".", ex);
        }
    }

    private static String textCell(String text) {
        String leading = text.stripLeading();
        // Keep leading spreadsheet formula characters in the text value.
        String safeText = !leading.isEmpty() && "=+-@".indexOf(leading.charAt(0)) >= 0 ? "'" + text : text;
        return "\"" + safeText.replace("\"", "\"\"") + "\"";
    }
}
