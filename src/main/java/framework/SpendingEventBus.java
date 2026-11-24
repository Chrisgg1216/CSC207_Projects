package framework;

import usecase.port.SpendingChangeListener;
import usecase.port.SpendingChangeNotifier;

import java.time.YearMonth;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Keeps the listeners and tells all of them. A listener that fails is logged and
 * skipped, so one broken screen cannot stop the others or undo a saved transaction.
 */
public class SpendingEventBus implements SpendingChangeNotifier {

    private static final Logger LOG = Logger.getLogger(SpendingEventBus.class.getName());

    private final List<SpendingChangeListener> listeners = new CopyOnWriteArrayList<>();

    @Override
    public void addListener(SpendingChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public void spendingChanged(YearMonth month) {
        for (SpendingChangeListener listener : listeners) {
            try {
                listener.onSpendingChanged(month);
            } catch (RuntimeException ex) {
                LOG.log(Level.WARNING, "A spending listener failed", ex);
            }
        }
    }
}
