package adapter;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

public final class MonthlySummaryViewModel {
    public record State(String heading, String totals, List<String> categoryLines,
                        List<String> transactionLines, String message) { }

    private final PropertyChangeSupport changes = new PropertyChangeSupport(this);
    private State state = new State("", "", List.of(), List.of(), "");

    public State getState() {
        return state;
    }

    void setState(State newState) {
        State previous = state;
        state = newState;
        changes.firePropertyChange("state", previous, newState);
    }

    void showExportResult(String message) {
        changes.firePropertyChange("exportResult", null, message);
    }

    public void addExportListener(PropertyChangeListener listener) {
        changes.addPropertyChangeListener("exportResult", listener);
    }

    public void addStateListener(PropertyChangeListener listener) {
        changes.addPropertyChangeListener("state", listener);
    }
}
