package framework;

import adapter.CategoryController;
import adapter.CategoryViewModel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

public final class CategoriesPanel extends JPanel {
    private final DefaultListModel<String> model = new DefaultListModel<>();
    private final JList<String> categories = new JList<>(model);
    private final JTextField name = new JTextField(18);
    private final JLabel message = new JLabel(" ");

    public CategoriesPanel(CategoryController controller, CategoryViewModel viewModel) {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        categories.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        categories.getAccessibleContext().setAccessibleName("Categories");
        JLabel nameLabel = new JLabel("Category name:");
        nameLabel.setLabelFor(name);

        JButton add = new JButton("Add category");
        JButton rename = new JButton("Rename selected");
        JButton delete = new JButton("Delete selected");
        add.addActionListener(event -> controller.add(name.getText()));
        rename.addActionListener(event -> controller.rename(categories.getSelectedValue(), name.getText()));
        delete.addActionListener(event -> {
            String selected = categories.getSelectedValue();
            if (selected != null && JOptionPane.showConfirmDialog(this,
                    "Delete category \"" + selected + "\"?", "Delete category",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                controller.delete(selected);
            }
        });
        rename.setEnabled(false);
        delete.setEnabled(false);
        categories.addListSelectionListener(event -> {
            boolean selected = !categories.isSelectionEmpty();
            rename.setEnabled(selected);
            delete.setEnabled(selected);
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.add(nameLabel);
        actions.add(name);
        actions.add(add);
        actions.add(rename);
        actions.add(delete);
        add(actions, BorderLayout.NORTH);
        add(new JScrollPane(categories), BorderLayout.CENTER);
        add(message, BorderLayout.SOUTH);
        viewModel.addStateListener(event -> render(viewModel.getState()));
        controller.refresh();
    }

    private void render(CategoryViewModel.State state) {
        String selected = categories.getSelectedValue();
        model.clear();
        state.categories().forEach(model::addElement);
        categories.setSelectedValue(selected, true);
        String text = state.message();
        message.setText(text.isEmpty() ? " " : (state.error() ? "Error: " : "") + text);
        if (!state.error() && !text.isEmpty()) {
            name.setText("");
        }
    }
}
