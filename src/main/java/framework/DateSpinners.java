package framework;

import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

/** Swing has no date picker; a spinner that shows yyyy-MM-dd does the same job. */
final class DateSpinners {

    private DateSpinners() {
    }

    static JSpinner create(String accessibleName) {
        JSpinner spinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
        spinner.setEditor(new JSpinner.DateEditor(spinner, "yyyy-MM-dd"));
        spinner.getAccessibleContext().setAccessibleName(accessibleName);
        spinner.setToolTipText(accessibleName + " (year-month-day)");
        return spinner;
    }

    static LocalDate toLocalDate(JSpinner spinner) {
        return ((Date) spinner.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
