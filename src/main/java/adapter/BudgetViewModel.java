package adapter;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

/**
 * State shown by the budget screen. No JavaFX here, so it can be tested directly.
 * The view listens for the "lines" and "message" property changes.
 */
public class BudgetViewModel {

    public static final String LINES = "lines";
    public static final String MESSAGE = "message";

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    private List<String> lines = List.of();
    private String message = "";
    private boolean error;

    public List<String> getLines() {
        return lines;
    }

    public String getMessage() {
        return message;
    }

    public boolean isError() {
        return error;
    }

    public void setLines(List<String> newLines) {
        List<String> old = this.lines;
        this.lines = List.copyOf(newLines);
        support.firePropertyChange(LINES, old, this.lines);
    }

    /** Always notifies listeners, even if the same text is shown twice in a row. */
    public void setMessage(String newMessage, boolean isError) {
        this.error = isError;
        this.message = newMessage;
        support.firePropertyChange(MESSAGE, null, newMessage);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }
}
