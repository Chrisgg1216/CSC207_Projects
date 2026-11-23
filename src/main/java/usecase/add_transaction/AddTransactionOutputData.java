package usecase.add_transaction;

import entity.Transaction;
import java.util.List;

public record AddTransactionOutputData(List<Transaction> transactions, String message) {
    public AddTransactionOutputData {
        transactions = List.copyOf(transactions);
    }
}
