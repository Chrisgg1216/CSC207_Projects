package usecase.port;

import entity.Transaction;
import java.util.List;

/** Shared storage contract for transactions. */
public interface TransactionDataAccess {
    void save(Transaction transaction);

    List<Transaction> findAll();
}
