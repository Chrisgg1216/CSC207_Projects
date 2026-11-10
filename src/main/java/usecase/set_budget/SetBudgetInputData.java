package usecase.set_budget;

import java.math.BigDecimal;
import java.time.YearMonth;

/**
 * Raw request from the user. limit may be null when the text typed could not be
 * read as a number; the Interactor rejects it with a clear message.
 */
public final class SetBudgetInputData {

    private final String categoryName;
    private final YearMonth month;
    private final BigDecimal limit;
    private final String currency;

    public SetBudgetInputData(String categoryName, YearMonth month, BigDecimal limit, String currency) {
        this.categoryName = categoryName;
        this.month = month;
        this.limit = limit;
        this.currency = currency;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public YearMonth getMonth() {
        return month;
    }

    public BigDecimal getLimit() {
        return limit;
    }

    public String getCurrency() {
        return currency;
    }
}
