package framework;

import entity.Budget;
import usecase.port.BudgetDataAccess;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Keeps budgets in memory. Used for tests and the demo. Person C's JSON storage
 * will implement the same BudgetDataAccess interface to make budgets persistent.
 */
public class InMemoryBudgetDataAccess implements BudgetDataAccess {

    private final Map<String, Budget> budgets = new HashMap<>();

    private static String key(String categoryName, YearMonth month) {
        return categoryName.trim().toLowerCase() + "|" + month;
    }

    @Override
    public void save(Budget budget) {
        budgets.put(key(budget.getCategory().getName(), budget.getMonth()), budget);
    }

    @Override
    public Optional<Budget> find(String categoryName, YearMonth month) {
        return Optional.ofNullable(budgets.get(key(categoryName, month)));
    }

    @Override
    public List<Budget> findByMonth(YearMonth month) {
        List<Budget> result = new ArrayList<>();
        for (Budget b : budgets.values()) {
            if (b.getMonth().equals(month)) {
                result.add(b);
            }
        }
        return result;
    }
}
