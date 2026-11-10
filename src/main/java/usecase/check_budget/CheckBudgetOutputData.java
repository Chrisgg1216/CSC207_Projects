package usecase.check_budget;

import java.time.YearMonth;
import java.util.List;

public final class CheckBudgetOutputData {

    private final YearMonth month;
    private final List<BudgetLine> lines;
    private final int pendingConversionCount;

    public CheckBudgetOutputData(YearMonth month, List<BudgetLine> lines, int pendingConversionCount) {
        this.month = month;
        this.lines = List.copyOf(lines);
        this.pendingConversionCount = pendingConversionCount;
    }

    public YearMonth getMonth() {
        return month;
    }

    public List<BudgetLine> getLines() {
        return lines;
    }

    /** Transactions waiting for an exchange rate; they are NOT counted in any spent total. */
    public int getPendingConversionCount() {
        return pendingConversionCount;
    }
}
