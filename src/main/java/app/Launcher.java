package app;

import javax.swing.SwingUtilities;

/** Starts the app. Run this class from the IDE. */
public class Launcher {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainApp::show);
    }
}
