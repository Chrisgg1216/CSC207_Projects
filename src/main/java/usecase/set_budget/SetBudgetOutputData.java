package usecase.set_budget;

import entity.Money;

import java.time.YearMonth;

public final class SetBudgetOutputData {

    private final String categoryName;
    private final YearMonth month;
    private final Money limit;
    private final boolean replacedExisting;

    public SetBudgetOutputData(String categoryName, YearMonth month, Money limit, boolean replacedExisting) {
        this.categoryName = categoryName;
        this.month = month;
        this.limit = limit;
        this.replacedExisting = replacedExisting;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public YearMonth getMonth() {
        return month;
    }

    public Money getLimit() {
        return limit;
    }

    public boolean isReplacedExisting() {
        return replacedExisting;
    }
}
