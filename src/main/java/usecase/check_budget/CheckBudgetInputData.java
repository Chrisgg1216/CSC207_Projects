package usecase.check_budget;

import java.time.YearMonth;

public final class CheckBudgetInputData {

    private final YearMonth month;
    private final String baseCurrency;

    public CheckBudgetInputData(YearMonth month, String baseCurrency) {
        this.month = month;
        this.baseCurrency = baseCurrency;
    }

    public YearMonth getMonth() {
        return month;
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }
}
