package usecase.port;

import entity.Transaction;

import java.util.List;

public interface TransactionCsvWriter {
    void write(String destination, List<Transaction> transactions);
}
