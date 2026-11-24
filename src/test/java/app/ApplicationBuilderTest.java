package app;

import org.junit.jupiter.api.Test;

import javax.swing.JTabbedPane;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Smoke test: the whole app wires up and has the three tabs (no window is opened). */
class ApplicationBuilderTest {

    @Test
    void buildsThreeTabsInOrder() {
        System.setProperty("java.awt.headless", "true");
        JTabbedPane tabs = new ApplicationBuilder().buildTabs();

        assertEquals(3, tabs.getTabCount());
        assertEquals("Transactions", tabs.getTitleAt(0));
        assertEquals("Budget", tabs.getTitleAt(1));
        assertEquals("Summary", tabs.getTitleAt(2));
    }
}
