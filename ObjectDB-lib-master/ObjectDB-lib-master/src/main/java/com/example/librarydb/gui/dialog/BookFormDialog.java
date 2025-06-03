package com.example.librarydb.gui.dialog;

import com.example.librarydb.config.JPAConfig;
import com.example.librarydb.entity.Book;
import com.example.librarydb.entity.Category;
import com.example.librarydb.service.CategoryService;


import javax.swing.*;
import java.awt.*;
import java.util.List;

public class BookFormDialog extends JDialog {
    private final Book book;
    private final JTextField txtTitle  = new JTextField(20);
    private final JTextField txtAuthor = new JTextField(20);
    private final JComboBox<Category> cmbCategory;
    private boolean saved = false;

    private static class CategoryComboBoxRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                    int index, boolean isSelected,
                                                    boolean cellHasFocus) {
            if (value instanceof Category) {
                value = ((Category) value).getName();
            }
            return super.getListCellRendererComponent(list, value, index,
                                                    isSelected, cellHasFocus);
        }
    }

    public BookFormDialog(Frame owner, Book book) {
        super(owner, true);
        this.book = book;  // keep reference

        boolean isNew = (book.getId() == null);
        setTitle(isNew ? "New Book" : "Edit Book");
        setLayout(new BorderLayout());
        setSize(350, 180);
        setLocationRelativeTo(owner);

        // Form fields
        JPanel form = new JPanel(new GridLayout(3, 2, 5, 5));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        form.add(new JLabel("Title:"));
        form.add(txtTitle);
        form.add(new JLabel("Author:"));
        form.add(txtAuthor);

        // Add category dropdown with custom renderer
        cmbCategory = new JComboBox<>();
        cmbCategory.setRenderer(new CategoryComboBoxRenderer());
        // Add "No Category" option
        cmbCategory.addItem(null);
        loadCategories();
        form.add(new JLabel("Category:"));
        form.add(cmbCategory);

        // Pre-fill when editing
        if (!isNew) {
            txtTitle.setText(book.getTitle());
            txtAuthor.setText(book.getAuthor());
            cmbCategory.setSelectedItem(book.getCategory());
        }
        add(form, BorderLayout.CENTER);

        // Save and Cancel buttons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave   = new JButton("Save");
        JButton btnCancel = new JButton("Cancel");
        buttons.add(btnSave);
        buttons.add(btnCancel);
        add(buttons, BorderLayout.SOUTH);

        // Save action
        btnSave.addActionListener(e -> {
            String title  = txtTitle.getText().trim();
            String author = txtAuthor.getText().trim();
            if (title.isEmpty() || author.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Both Title and Author are required.",
                        "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // write back into the entity
            book.setTitle(title);
            book.setAuthor(author);
            book.setCategory((Category) cmbCategory.getSelectedItem());
            saved = true;
            dispose();
        });

        // Cancel just closes
        btnCancel.addActionListener(e -> dispose());
    }

    private void loadCategories() {
        CategoryService service = new CategoryService(JPAConfig.getEntityManagerFactory());
        List<Category> categories = service.listAll();
        for (Category c : categories) {
            cmbCategory.addItem(c);
        }
    }

    /** True if user clicked “Save” */
    public boolean isSaved() {
        return saved;
    }

    /** Returns the Book instance (new or edited) */
    public Book getBook() {
        return book;
    }
}
