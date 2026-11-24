package framework;

import adapter.TransactionController;
import adapter.TransactionViewModel;
import entity.TransactionType;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.util.List;

/** Add transactions without blocking the Swing event thread during HTTP. */
public class TransactionsPanel extends JPanel {
    private final TransactionController controller;
    private final TransactionViewModel viewModel;
    private final JComboBox<String> category;
    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JList<String> list = new JList<>(listModel);
    private final JTextArea message = new JTextArea(2, 40);
    private final JButton add = new JButton("Add transaction");
    private final JButton refresh = new JButton("Refresh list");

    public TransactionsPanel(TransactionController controller, TransactionViewModel viewModel,
                             List<String> categoryNames) {
        super(new BorderLayout(8, 8));
        this.controller = controller;
        this.viewModel = viewModel;
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JSpinner date = DateSpinners.create("Transaction date");
        JComboBox<TransactionType> type = new JComboBox<>(TransactionType.values());
        type.setSelectedItem(TransactionType.SPENDING);
        type.getAccessibleContext().setAccessibleName("Income or spending");
        category = new JComboBox<>(categoryNames.toArray(new String[0]));
        category.setSelectedIndex(-1);
        category.getAccessibleContext().setAccessibleName("Transaction category");
        JTextField amount = new JTextField(8);
        amount.getAccessibleContext().setAccessibleName("Original amount");
        JComboBox<String> currency = new JComboBox<>(new String[] {"CAD", "USD", "CNY"});
        currency.setSelectedItem("CAD");
        currency.getAccessibleContext().setAccessibleName("Original currency");
        JTextField note = new JTextField(20);
        note.getAccessibleContext().setAccessibleName("Transaction note, optional");

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row1.add(label("Date:", date));
        row1.add(date);
        row1.add(label("Type:", type));
        row1.add(type);
        row1.add(label("Category:", category));
        row1.add(category);
        row1.add(label("Amount:", amount));
        row1.add(amount);
        row1.add(currency);
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row2.add(label("Note (optional):", note));
        row2.add(note);
        row2.add(add);
        row2.add(refresh);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Transactions — base currency CAD");
        for (JComponent part : new JComponent[] {title, row1, row2}) {
            part.setAlignmentX(Component.LEFT_ALIGNMENT);
            form.add(part);
        }

        refresh.addActionListener(event -> controller.refresh());
        add.addActionListener(event -> {
            LocalDate selectedDate = DateSpinners.toLocalDate(date);
            TransactionType selectedType = (TransactionType) type.getSelectedItem();
            String selectedCategory = (String) category.getSelectedItem();
            String selectedAmount = amount.getText();
            String selectedCurrency = (String) currency.getSelectedItem();
            String selectedNote = note.getText();
            setBusy(true);
            message.setText("Saving transaction; checking historical exchange rate if needed…");
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    controller.add(selectedDate, selectedType, selectedCategory, selectedAmount,
                            selectedCurrency, selectedNote);
                    return null;
                }

                @Override
                protected void done() {
                    setBusy(false);
                    try {
                        get();
                    } catch (Exception ex) {
                        viewModel.update(viewModel.getLines(),
                                "Could not complete the operation. Refresh the list before retrying.", true);
                    }
                    render();
                    if (!viewModel.isError()) {
                        amount.setText("");
                        note.setText("");
                    }
                }
            }.execute();
        });

        list.getAccessibleContext().setAccessibleName(
                "Transactions with original amount, reference CAD amount and conversion status");
        message.setEditable(false);
        message.setLineWrap(true);
        message.setWrapStyleWord(true);
        message.setOpaque(false);
        message.getAccessibleContext().setAccessibleName("Message");

        add(form, BorderLayout.NORTH);
        add(new JScrollPane(list), BorderLayout.CENTER);
        add(message, BorderLayout.SOUTH);

        viewModel.addPropertyChangeListener(event -> SwingUtilities.invokeLater(this::render));
        controller.refresh();
    }

    public void refresh(List<String> categories) {
        Object selected = category.getSelectedItem();
        category.setModel(new DefaultComboBoxModel<>(categories.toArray(String[]::new)));
        category.setSelectedItem(selected != null && categories.contains(selected) ? selected : null);
        if (add.isEnabled()) {
            controller.refresh();
        }
    }

    private static JLabel label(String text, Component target) {
        JLabel label = new JLabel(text);
        label.setLabelFor(target);
        return label;
    }

    private void setBusy(boolean busy) {
        add.setEnabled(!busy);
        refresh.setEnabled(!busy);
    }

    private void render() {
        listModel.clear();
        for (String line : viewModel.getLines()) {
            listModel.addElement(line);
        }
        if (listModel.isEmpty()) {
            listModel.addElement("No transactions yet. Add your first income or spending record.");
        }
        message.setText((viewModel.isError() ? "Error: " : "") + viewModel.getMessage());
    }
}
