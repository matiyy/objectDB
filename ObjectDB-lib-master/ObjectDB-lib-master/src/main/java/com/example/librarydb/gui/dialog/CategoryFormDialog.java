package com.example.librarydb.gui.dialog;

import com.example.librarydb.entity.Category;

import javax.swing.*;
import java.awt.*;

public class CategoryFormDialog extends JDialog {
    private final Category category;
    private final JTextField txtName = new JTextField(20);
    private final JTextField txtDescription = new JTextField(30);
    private boolean saved = false;

    // Change Window to Frame to match the calling code
    public CategoryFormDialog(Frame owner, Category category) {
        super(owner, true); // Call JDialog constructor with modal=true
        this.category = category;

        boolean isNew = (category.getId() == null);
        setTitle(isNew ? "New Category" : "Edit Category");
        setLayout(new BorderLayout());
        setSize(400, 150);
        setLocationRelativeTo(owner);

        // Form fields
        JPanel form = new JPanel(new GridLayout(2, 2, 5, 5));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        form.add(new JLabel("Name:"));
        form.add(txtName);
        form.add(new JLabel("Description:"));
        form.add(txtDescription);

        if (!isNew) {
            txtName.setText(category.getName());
            txtDescription.setText(category.getDescription());
        }
        
        add(form, BorderLayout.CENTER);

        // Buttons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("Save");
        JButton btnCancel = new JButton("Cancel");
        buttons.add(btnSave);
        buttons.add(btnCancel);
        add(buttons, BorderLayout.SOUTH);

        btnSave.addActionListener(e -> {
            String name = txtName.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Category name is required.",
                    "Validation", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            category.setName(name);
            category.setDescription(txtDescription.getText().trim());
            saved = true;
            dispose();
        });

        btnCancel.addActionListener(e -> dispose());
    }

    public boolean isSaved() {
        return saved;
    }

    public Category getCategory() {
        return category;
    }
}