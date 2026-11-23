package entity;

import java.time.YearMonth;

/**
 * A monthly spending limit for one category, in the user's base currency.
 */
public final class Budget {

    private final Category category;
    private final YearMonth month;
    private final Money limit;

    public Budget(Category category, YearMonth month, Money limit) {
        if (category == null) {
            throw new IllegalArgumentException("Category is required.");
        }
        if (month == null) {
            throw new IllegalArgumentException("Month is required.");
        }
        if (limit == null || !limit.isPositive()) {
            throw new IllegalArgumentException("Limit must be greater than zero.");
        }
        this.category = category;
        this.month = month;
        this.limit = limit;
    }

    public Category getCategory() {
        return category;
    }

    public YearMonth getMonth() {
        return month;
    }

    public Money getLimit() {
        return limit;
    }
}
