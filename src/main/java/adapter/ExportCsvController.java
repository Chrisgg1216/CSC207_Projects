package adapter;

import usecase.export_csv.ExportCsvInputBoundary;

import java.time.LocalDate;
import java.time.YearMonth;

public final class ExportCsvController {
    private final ExportCsvInputBoundary useCase;

    public ExportCsvController(ExportCsvInputBoundary useCase) {
        this.useCase = useCase;
    }

    public void exportMonth(LocalDate date, String destination) {
        useCase.execute(date == null ? null : YearMonth.from(date), destination);
    }
}
