package framework;

import adapter.MonthlySummaryController;
import adapter.MonthlySummaryViewModel;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;

/** The Summary tab: income, spending, net, category totals and the month's transactions. */
public final class MonthlySummaryPanel extends JPanel {
    private final JSpinner date = DateSpinners.create("Any date in the month to show");
    private final JLabel heading = new JLabel(" ");
    private final JLabel totals = new JLabel(" ");
    private final JTextArea message = new JTextArea(2, 40);
    private final DefaultListModel<String> categoryModel = new DefaultListModel<>();
    private final DefaultListModel<String> transactionModel = new DefaultListModel<>();
    private final JList<String> categories = new JList<>(categoryModel);
    private final JList<String> transactions = new JList<>(transactionModel);
    private final MonthlySummaryController controller;

    public MonthlySummaryPanel(MonthlySummaryController controller, MonthlySummaryViewModel viewModel) {
        super(new BorderLayout(8, 8));
        this.controller = controller;
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel dateLabel = new JLabel("Choose any date in the month:");
        dateLabel.setLabelFor(date);
        JButton refreshButton = new JButton("Refresh summary");
        date.addChangeListener(event -> refresh());
        refreshButton.addActionListener(event -> refresh());

        JPanel picker = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        picker.add(dateLabel);
        picker.add(date);
        picker.add(refreshButton);

        message.setEditable(false);
        message.setLineWrap(true);
        message.setWrapStyleWord(true);
        message.setOpaque(false);
        message.getAccessibleContext().setAccessibleName("Message");

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        for (JComponent part : new JComponent[] {picker, heading, totals, message}) {
            part.setAlignmentX(Component.LEFT_ALIGNMENT);
            top.add(part);
        }

        categories.getAccessibleContext().setAccessibleName("Spending by category");
        transactions.getAccessibleContext().setAccessibleName("Transactions in this month");
        JScrollPane categoryScroll = new JScrollPane(categories);
        categoryScroll.setBorder(BorderFactory.createTitledBorder("Spending by category"));
        JScrollPane transactionScroll = new JScrollPane(transactions);
        transactionScroll.setBorder(BorderFactory.createTitledBorder("Transactions in this month"));
        JSplitPane groups = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, categoryScroll, transactionScroll);
        groups.setResizeWeight(0.32);

        add(top, BorderLayout.NORTH);
        add(groups, BorderLayout.CENTER);

        viewModel.addStateListener(event ->
                SwingUtilities.invokeLater(() -> render(viewModel.getState())));
        refresh();
    }

    public void refresh() {
        controller.showMonth(DateSpinners.toLocalDate(date));
    }

    private void render(MonthlySummaryViewModel.State state) {
        heading.setText(state.heading().isEmpty() ? " " : state.heading());
        totals.setText(state.totals().isEmpty() ? " " : state.totals());
        message.setText(state.message());
        message.setVisible(!state.message().isEmpty());
        categoryModel.clear();
        state.categoryLines().forEach(categoryModel::addElement);
        if (categoryModel.isEmpty()) {
            categoryModel.addElement("No converted spending");
        }
        transactionModel.clear();
        state.transactionLines().forEach(transactionModel::addElement);
        if (transactionModel.isEmpty()) {
            transactionModel.addElement("No transactions to show");
        }
    }
}
