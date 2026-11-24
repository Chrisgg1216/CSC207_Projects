package usecase.port;

import java.time.YearMonth;

/**
 * Subject of the Observer pattern. Transaction use cases call spendingChanged
 * after they save, and anything that shows spending (for example the Budget
 * screen) registers a listener. The use case never knows who is listening.
 *
 * Implementations may call listeners on the thread that called spendingChanged,
 * so a listener that updates a screen must hand the work to the UI thread itself.
 */
public interface SpendingChangeNotifier {

    /** Does nothing; used when nobody needs to be told. */
    SpendingChangeNotifier NONE = new SpendingChangeNotifier() {
        @Override
        public void addListener(SpendingChangeListener listener) {
        }

        @Override
        public void spendingChanged(YearMonth month) {
        }
    };

    void addListener(SpendingChangeListener listener);

    void spendingChanged(YearMonth month);
}
