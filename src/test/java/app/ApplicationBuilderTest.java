package app;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Exercises the actual Swing wiring without opening a window. */
class ApplicationBuilderTest {
    @TempDir
    Path temp;

    @BeforeAll
    static void useHeadlessSwing() {
        System.setProperty("java.awt.headless", "true");
    }

    @Test
    void buildsFourTabsInOrder() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JTabbedPane tabs = new ApplicationBuilder(temp.resolve("ledger.json")).buildTabs();
            assertEquals(4, tabs.getTabCount());
            assertEquals("Transactions", tabs.getTitleAt(0));
            assertEquals("Budget", tabs.getTitleAt(1));
            assertEquals("Summary", tabs.getTitleAt(2));
            assertEquals("Categories", tabs.getTitleAt(3));
        });
    }

    @Test
    void renamedCategoryPersistsInTransactionsBudgetAndSummary() throws Exception {
        Path file = temp.resolve("ledger.json");
        var transactionSaved = new CountDownLatch(1);
        var screens = new JTabbedPane[1];

        SwingUtilities.invokeAndWait(() -> {
            JTabbedPane tabs = new ApplicationBuilder(file).buildTabs();
            screens[0] = tabs;
            tabs.setSelectedIndex(3);
            Component categories = tabs.getSelectedComponent();
            first(categories, JTextField.class).setText("Coffee");
            click(categories, "Add category");

            tabs.setSelectedIndex(0);
            Component transactions = tabs.getSelectedComponent();
            categoryBox(transactions, "Transaction category").setSelectedItem("Coffee");
            field(transactions, "Original amount").setText("20");
            JButton add = button(transactions, "Add transaction");
            add.addPropertyChangeListener("enabled", event -> {
                if (Boolean.TRUE.equals(event.getNewValue())) {
                    transactionSaved.countDown();
                }
            });
            add.doClick();
        });
        assertTrue(transactionSaved.await(5, TimeUnit.SECONDS), "CAD transaction save did not finish");

        SwingUtilities.invokeAndWait(() -> {
            JTabbedPane tabs = screens[0];
            tabs.setSelectedIndex(1);
            Component budget = tabs.getSelectedComponent();
            categoryBox(budget, "Category").setSelectedItem("Coffee");
            field(budget, "Monthly limit in CAD").setText("30");
            click(budget, "Set budget");
            tabs.setSelectedIndex(3);
            Component categories = tabs.getSelectedComponent();
            first(categories, JList.class).setSelectedValue("Coffee", true);
            first(categories, JTextField.class).setText("Cafés");
            click(categories, "Rename selected");
        });

        SwingUtilities.invokeAndWait(() -> {
            screens[0] = new ApplicationBuilder(file).buildTabs();
            screens[0].setSelectedIndex(1);
            screens[0].setSelectedIndex(2);
        });
        SwingUtilities.invokeAndWait(() -> {
            var tabs = screens[0];
            Component budget = tabs.getComponentAt(1);
            assertTrue(listContains(budget, "Cafés: 20.00 of 30.00 CAD"));
            Component summary = tabs.getComponentAt(2);
            assertTrue(listContains(summary, "Cafés  |  20.00 CAD"));
            assertTrue(components(summary).filter(JLabel.class::isInstance).map(JLabel.class::cast)
                    .anyMatch(label -> label.getText().contains("Spending: 20.00 CAD")));
        });
    }

    private static boolean listContains(Component panel, String text) {
        return components(panel).filter(JList.class::isInstance).map(JList.class::cast).anyMatch(list -> {
            for (int index = 0; index < list.getModel().getSize(); index++) {
                if (list.getModel().getElementAt(index).toString().contains(text)) {
                    return true;
                }
            }
            return false;
        });
    }

    private static void click(Component panel, String text) {
        button(panel, text).doClick();
    }

    private static JButton button(Component panel, String text) {
        return components(panel).filter(JButton.class::isInstance).map(JButton.class::cast)
                .filter(button -> text.equals(button.getText())).findFirst().orElseThrow();
    }

    private static JTextField field(Component panel, String label) {
        return byAccessibleName(panel, JTextField.class, label);
    }

    private static JComboBox<?> categoryBox(Component panel, String label) {
        return byAccessibleName(panel, JComboBox.class, label);
    }

    private static <T extends Component> T byAccessibleName(Component panel, Class<T> type, String name) {
        return components(panel).filter(type::isInstance).map(type::cast)
                .filter(component -> name.equals(component.getAccessibleContext().getAccessibleName()))
                .findFirst().orElseThrow();
    }

    private static <T extends Component> T first(Component panel, Class<T> type) {
        return components(panel).filter(type::isInstance).map(type::cast).findFirst().orElseThrow();
    }

    private static Stream<Component> components(Component root) {
        if (root instanceof Container container) {
            return Stream.concat(Stream.of(root), Arrays.stream(container.getComponents())
                    .flatMap(ApplicationBuilderTest::components));
        }
        return Stream.of(root);
    }
}
