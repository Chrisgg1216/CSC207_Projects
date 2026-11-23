package adapter;

import usecase.check_budget.CheckBudgetInputBoundary;
import usecase.check_budget.CheckBudgetInputData;
import usecase.set_budget.SetBudgetInputBoundary;
import usecase.set_budget.SetBudgetInputData;

import java.math.BigDecimal;
import java.time.YearMonth;

/**
 * Receives actions from the budget screen and calls the use cases.
 */
public class BudgetController {

    private final SetBudgetInputBoundary setBudget;
    private final CheckBudgetInputBoundary checkBudget;
    // TODO: read the base currency from Person A's SetBaseCurrency setting.
    private final String baseCurrency;

    public BudgetController(SetBudgetInputBoundary setBudget,
                            CheckBudgetInputBoundary checkBudget,
                            String baseCurrency) {
        this.setBudget = setBudget;
        this.checkBudget = checkBudget;
        this.baseCurrency = baseCurrency;
    }

    /**
     * limitText is what the user typed. Text that is not a number becomes a null
     * limit, which the use case rejects with a clear message.
     */
    public void setBudget(String categoryName, YearMonth month, String limitText) {
        setBudget.execute(new SetBudgetInputData(categoryName, month, parse(limitText), baseCurrency));
    }

    public void checkBudget(YearMonth month) {
        checkBudget.execute(new CheckBudgetInputData(month, baseCurrency));
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    private static BigDecimal parse(String text) {
        if (text == null) {
            return null;
        }
        try {
            return new BigDecimal(text.trim().replace(",", ""));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
