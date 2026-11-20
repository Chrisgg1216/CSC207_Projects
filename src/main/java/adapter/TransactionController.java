package adapter;

import entity.TransactionType;
import usecase.add_transaction.AddTransactionInputBoundary;
import usecase.add_transaction.AddTransactionInputData;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionController {
    private final AddTransactionInputBoundary addTransaction;

    public TransactionController(AddTransactionInputBoundary addTransaction) {
        this.addTransaction = addTransaction;
    }

    public void add(LocalDate date, TransactionType type, String category, String amount,
                    String currency, String note) {
        BigDecimal parsed;
        try {
            parsed = new BigDecimal(amount.trim());
        } catch (NumberFormatException | NullPointerException ex) {
            parsed = null;
        }
        addTransaction.execute(new AddTransactionInputData(date, type, category, parsed, currency, note));
    }

    public void refresh() {
        addTransaction.refresh();
    }
}
