package usecase.port;

import java.time.YearMonth;

/** Observer: told when the transactions of a month have changed. */
public interface SpendingChangeListener {

    void onSpendingChanged(YearMonth month);
}
