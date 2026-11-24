package usecase.export_csv;

import java.time.YearMonth;

public interface ExportCsvInputBoundary {
    void execute(YearMonth month, String destination);
}
