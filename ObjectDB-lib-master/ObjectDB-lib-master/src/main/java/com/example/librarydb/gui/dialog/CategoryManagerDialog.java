package com.example.librarydb.gui.dialog;

import com.example.librarydb.entity.Category;
import com.example.librarydb.gui.model.CategoryTableModel;
import com.example.librarydb.config.JPAConfig;

import javax.swing.*;
import java.awt.*;

public class CategoryManagerDialog extends JDialog {
    private final CategoryTableModel tableModel;
    private final JTable table;

    public CategoryManagerDialog(Frame owner) {
        super(owner, "Category Manager", true);
        setLayout(new BorderLayout());
        setSize(500, 400);
        setLocationRelativeTo(owner);

        tableModel = new CategoryTableModel(JPAConfig.getEntityManagerFactory());
        table = new JTable(tableModel);

        // Add toolbar
        add(createToolBar(), BorderLayout.NORTH);

        // Add table
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(scrollPane, BorderLayout.CENTER);
    }

    private JToolBar createToolBar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

        JButton btnNew = new JButton("New Category");
        JButton btnEdit = new JButton("Edit");
        JButton btnDelete = new JButton("Delete");

        btnNew.addActionListener(e -> createCategory());
        btnEdit.addActionListener(e -> editSelected());
        btnDelete.addActionListener(e -> deleteSelected());

        toolbar.add(btnNew);
        toolbar.add(btnEdit);
        toolbar.add(btnDelete);

        return toolbar;
    }

    private void createCategory() {
        CategoryFormDialog dialog = new CategoryFormDialog((Frame)getOwner(), new Category());
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            tableModel.getService().create(
                    dialog.getCategory().getName(),
                    dialog.getCategory().getDescription()
            );
            tableModel.reload();
        }
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return;

        Category category = tableModel.getCategoryAt(row);
        CategoryFormDialog dialog = new CategoryFormDialog((Frame)getOwner(), category);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            tableModel.getService().update(category);
            tableModel.reload();
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return;

        Category category = tableModel.getCategoryAt(row);
        if (category.getBooks().size() > 0) {
            JOptionPane.showMessageDialog(this,
                    "Cannot delete category with associated books.",
                    "Delete Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete selected category?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            tableModel.getService().delete(category.getId());
            tableModel.reload();
        }
    }
}