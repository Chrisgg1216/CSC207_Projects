package usecase.port;

import entity.Transaction;
import java.util.List;

/** Shared contract: Person C can implement this port using JSON storage. */
public interface TransactionDataAccess {
    void save(Transaction transaction);

    List<Transaction> findAll();
}
