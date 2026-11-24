package app;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 * Entry point: the main window with one tab per feature.
 * Run with: app.Launcher (IDE) or mvn exec:java
 */
public class MainApp {

    /** Creates and shows the window. Must be called on the Swing event thread. */
    public static void show() {
        JFrame frame = new JFrame("Budgeting App");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setContentPane(new ApplicationBuilder().buildTabs());
        frame.setSize(960, 520);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainApp::show);
    }
}
