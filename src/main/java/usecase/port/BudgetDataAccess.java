package usecase.port;

import entity.Budget;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

/**
 * Storage port for budgets. Implemented in the framework layer (in-memory now,
 * JSON file later, owned by Person C).
 */
public interface BudgetDataAccess {

    /** Saves the budget, replacing any budget for the same category and month. */
    void save(Budget budget);

    Optional<Budget> find(String categoryName, YearMonth month);

    List<Budget> findByMonth(YearMonth month);
}
