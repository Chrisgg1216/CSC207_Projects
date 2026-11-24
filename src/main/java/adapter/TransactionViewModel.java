package adapter;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

/** UI-neutral screen state; Swing is confined to framework. */
public class TransactionViewModel {
    private final PropertyChangeSupport changes = new PropertyChangeSupport(this);
    private List<String> lines = List.of();
    private String message = "";
    private boolean error;

    public List<String> getLines() { return lines; }
    public String getMessage() { return message; }
    public boolean isError() { return error; }

    public synchronized void update(List<String> newLines, String newMessage, boolean isError) {
        lines = List.copyOf(newLines);
        message = newMessage;
        error = isError;
        changes.firePropertyChange("state", null, newMessage);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changes.addPropertyChangeListener(listener);
    }
}
