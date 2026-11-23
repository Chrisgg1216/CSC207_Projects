package adapter;

import usecase.monthly_summary.MonthlySummaryInputBoundary;

import java.time.LocalDate;
import java.time.YearMonth;

public final class MonthlySummaryController {
    private final MonthlySummaryInputBoundary summary;

    public MonthlySummaryController(MonthlySummaryInputBoundary summary) {
        this.summary = summary;
    }

    public void showMonth(LocalDate selectedDate) {
        summary.execute(selectedDate == null ? null : YearMonth.from(selectedDate));
    }
}
