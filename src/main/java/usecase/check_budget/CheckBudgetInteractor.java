package usecase.check_budget;

import entity.Budget;
import entity.BudgetStatus;
import entity.Money;
import usecase.port.BudgetDataAccess;
import usecase.port.SpendingDataAccess;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Use case: for a month, report how much of each category's budget is used.
 *
 * Status rules (all comparisons use exact BigDecimal values, never doubles):
 *   spent below limit x warningThreshold        -> OK
 *   spent at/above that, up to and including limit -> WARNING
 *   spent strictly above limit                  -> OVER
 */
public class CheckBudgetInteractor implements CheckBudgetInputBoundary {

    /** Warn when 80% of the limit is used. */
    public static final BigDecimal DEFAULT_WARNING_THRESHOLD = new BigDecimal("0.80");

    private final BudgetDataAccess budgets;
    private final SpendingDataAccess spending;
    private final CheckBudgetOutputBoundary presenter;
    private final BigDecimal warningThreshold;

    public CheckBudgetInteractor(BudgetDataAccess budgets,
                                 SpendingDataAccess spending,
                                 CheckBudgetOutputBoundary presenter) {
        this(budgets, spending, presenter, DEFAULT_WARNING_THRESHOLD);
    }

    public CheckBudgetInteractor(BudgetDataAccess budgets,
                                 SpendingDataAccess spending,
                                 CheckBudgetOutputBoundary presenter,
                                 BigDecimal warningThreshold) {
        if (warningThreshold == null
                || warningThreshold.signum() <= 0
                || warningThreshold.compareTo(BigDecimal.ONE) >= 0) {
            throw new IllegalArgumentException("Warning threshold must be between 0 and 1 (exclusive).");
        }
        this.budgets = budgets;
        this.spending = spending;
        this.presenter = presenter;
        this.warningThreshold = warningThreshold;
    }

    @Override
    public void execute(CheckBudgetInputData input) {
        if (input.getMonth() == null) {
            presenter.prepareFailView("Please choose a month.");
            return;
        }
        if (input.getBaseCurrency() == null || input.getBaseCurrency().isBlank()) {
            presenter.prepareFailView("No base currency is set.");
            return;
        }

        List<BudgetLine> lines = new ArrayList<>();
        for (Budget budget : budgets.findByMonth(input.getMonth())) {
            if (!budget.getLimit().getCurrency().equalsIgnoreCase(input.getBaseCurrency().trim())) {
                // Budget was set in another currency; skip rather than mix currencies.
                continue;
            }
            String name = budget.getCategory().getName();
            Money spent = spending.totalSpent(name, input.getMonth(), input.getBaseCurrency());
            lines.add(buildLine(name, budget.getLimit(), spent));
        }
        lines.sort(Comparator.comparing(BudgetLine::getCategoryName, String.CASE_INSENSITIVE_ORDER));

        presenter.prepareSuccessView(new CheckBudgetOutputData(
                input.getMonth(), lines, spending.countPendingConversion(input.getMonth())));
    }

    private BudgetLine buildLine(String name, Money limit, Money spent) {
        BigDecimal limitAmount = limit.getAmount();
        BigDecimal spentAmount = spent.getAmount();

        BudgetStatus status;
        if (spentAmount.compareTo(limitAmount) > 0) {
            status = BudgetStatus.OVER;
        } else if (spentAmount.compareTo(limitAmount.multiply(warningThreshold)) >= 0) {
            status = BudgetStatus.WARNING;
        } else {
            status = BudgetStatus.OK;
        }

        // limit is always > 0 (enforced by Budget), so division is safe.
        BigDecimal percent = spentAmount
                .multiply(BigDecimal.valueOf(100))
                .divide(limitAmount, 1, RoundingMode.HALF_UP);

        return new BudgetLine(name, limit, spent, limit.subtract(spent), percent, status);
    }
}
