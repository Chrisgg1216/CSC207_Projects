package adapter;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

public final class CategoryViewModel {
    public record State(List<String> categories, String message, boolean error) {
        public State {
            categories = List.copyOf(categories);
        }
    }

    private final PropertyChangeSupport changes = new PropertyChangeSupport(this);
    private State state = new State(List.of(), "", false);

    public State getState() {
        return state;
    }

    void setState(State state) {
        State previous = this.state;
        this.state = state;
        changes.firePropertyChange("state", previous, state);
    }

    public void addStateListener(PropertyChangeListener listener) {
        changes.addPropertyChangeListener("state", listener);
    }
}
