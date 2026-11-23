package framework;

import entity.Transaction;
import usecase.port.TransactionDataAccess;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Milestone prototype only: replace through the port when Person C adds JSON. */
public class InMemoryTransactionDataAccess implements TransactionDataAccess {
    private final Map<String, Transaction> transactions = new LinkedHashMap<>();

    @Override
    public synchronized void save(Transaction transaction) {
        transactions.put(transaction.id(), transaction);
    }

    @Override
    public synchronized List<Transaction> findAll() {
        return List.copyOf(transactions.values());
    }
}
