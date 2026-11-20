package usecase.monthly_summary;

import java.time.YearMonth;

public interface MonthlySummaryInputBoundary {
    void execute(YearMonth month);
}
