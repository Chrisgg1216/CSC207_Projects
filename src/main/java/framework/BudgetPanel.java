package framework;

import adapter.BudgetController;
import adapter.BudgetViewModel;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * The Budget tab: set a monthly limit per category and see spent / remaining /
 * status. Status is shown as text and a symbol, not only color.
 */
public class BudgetPanel extends JPanel {

    private final BudgetController controller;
    private final BudgetViewModel viewModel;

    private final JComboBox<String> categoryBox;
    private final JTextField limitField = new JTextField(8);
    private final JTextField monthField = new JTextField(YearMonth.now().toString(), 7);
    private final DefaultListModel<String> statusModel = new DefaultListModel<>();
    private final JList<String> statusList = new JList<>(statusModel);
    private final JTextArea messageArea = new JTextArea(2, 40);

    public BudgetPanel(BudgetController controller, BudgetViewModel viewModel, List<String> categoryNames) {
        super(new BorderLayout(8, 8));
        this.controller = controller;
        this.viewModel = viewModel;
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        categoryBox = new JComboBox<>(categoryNames.toArray(new String[0]));
        categoryBox.setSelectedIndex(-1);
        categoryBox.getAccessibleContext().setAccessibleName("Category");

        monthField.getAccessibleContext().setAccessibleName("Month in the form year-month, for example 2026-11");
        limitField.getAccessibleContext().setAccessibleName("Monthly limit in " + controller.getBaseCurrency());

        JLabel monthLabel = new JLabel("Month (yyyy-MM):");
        monthLabel.setLabelFor(monthField);
        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setLabelFor(categoryBox);
        JLabel limitLabel = new JLabel("Monthly limit (" + controller.getBaseCurrency() + "):");
        limitLabel.setLabelFor(limitField);

        JButton setButton = new JButton("Set budget");
        setButton.addActionListener(e -> onSetBudget());
        JButton refreshButton = new JButton("Refresh status");
        refreshButton.addActionListener(e -> onRefresh());

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        form.add(monthLabel);
        form.add(monthField);
        form.add(categoryLabel);
        form.add(categoryBox);
        form.add(limitLabel);
        form.add(limitField);
        form.add(setButton);
        form.add(refreshButton);

        JPanel top = new JPanel(new BorderLayout());
        top.add(new JLabel("Monthly budgets"), BorderLayout.NORTH);
        top.add(form, BorderLayout.CENTER);

        statusList.getAccessibleContext().setAccessibleName("Budget status for each category");
        JScrollPane scroll = new JScrollPane(statusList);

        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setOpaque(false);
        messageArea.getAccessibleContext().setAccessibleName("Message");

        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(messageArea, BorderLayout.SOUTH);

        viewModel.addPropertyChangeListener(evt -> SwingUtilities.invokeLater(this::render));
        render();
    }

    private void onSetBudget() {
        YearMonth month = readMonth();
        if (month == null) {
            return;
        }
        controller.setBudget((String) categoryBox.getSelectedItem(), month, limitField.getText());
        // Show the new status straight away so a warning appears immediately.
        controller.checkBudget(month);
    }

    private void onRefresh() {
        YearMonth month = readMonth();
        if (month != null) {
            controller.checkBudget(month);
        }
    }

    private YearMonth readMonth() {
        try {
            return YearMonth.parse(monthField.getText().trim());
        } catch (DateTimeParseException ex) {
            viewModel.setMessage("Month must look like 2026-11.", true);
            return null;
        }
    }

    private void render() {
        statusModel.clear();
        for (String line : viewModel.getLines()) {
            statusModel.addElement(line);
        }
        String prefix = viewModel.isError() ? "Error: " : "";
        messageArea.setText(viewModel.getMessage().isEmpty() ? "" : prefix + viewModel.getMessage());
    }
}
