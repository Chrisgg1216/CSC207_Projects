package usecase.check_budget;

import entity.BudgetStatus;
import entity.Money;

import java.math.BigDecimal;

/**
 * The state of one category's budget for a month.
 * remaining is negative when the category is over budget.
 * percentUsed has one decimal place (for example 85.0).
 */
public final class BudgetLine {

    private final String categoryName;
    private final Money limit;
    private final Money spent;
    private final Money remaining;
    private final BigDecimal percentUsed;
    private final BudgetStatus status;

    public BudgetLine(String categoryName, Money limit, Money spent, Money remaining,
                      BigDecimal percentUsed, BudgetStatus status) {
        this.categoryName = categoryName;
        this.limit = limit;
        this.spent = spent;
        this.remaining = remaining;
        this.percentUsed = percentUsed;
        this.status = status;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public Money getLimit() {
        return limit;
    }

    public Money getSpent() {
        return spent;
    }

    public Money getRemaining() {
        return remaining;
    }

    public BigDecimal getPercentUsed() {
        return percentUsed;
    }

    public BudgetStatus getStatus() {
        return status;
    }
}
