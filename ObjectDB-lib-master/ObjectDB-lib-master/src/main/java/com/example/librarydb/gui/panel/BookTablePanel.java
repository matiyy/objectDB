package com.example.librarydb.gui.panel;

import com.example.librarydb.entity.Book;
import com.example.librarydb.entity.Category;
import com.example.librarydb.gui.controller.BookTableController;
import com.example.librarydb.gui.model.BookTableModel;
import com.example.librarydb.service.CategoryService;

import javax.persistence.EntityManagerFactory;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.function.Consumer;

public class BookTablePanel extends JPanel {
    private final BookTableModel tableModel;
    private final JTable table;
    private final BookTableController controller;
    private final CategoryService categoryService;

    // Filter components
    private JTextField idFromField;
    private JTextField idToField;
    private JTextField titleField;
    private JTextField authorField;
    private JComboBox<Category> categoryCombo;

    // Advanced search components
    private JTextField regexTitleField;
    private JTextField regexAuthorField;
    private JCheckBox caseSensitiveBox;
    private JCheckBox useRegexBox;

    // Action buttons
    private JButton undoButton;
    private JButton redoButton;
    private JButton copyButton;
    private JButton duplicateButton;
    private JButton exportButton;
    private JButton statsButton;
    private JButton clearFiltersButton;

    public BookTablePanel(EntityManagerFactory emf, Consumer<Book> onEdit) {
        this.tableModel = new BookTableModel(emf);
        this.table = new JTable();
        this.controller = new BookTableController(tableModel, table, emf, onEdit);
        this.categoryService = new CategoryService(emf);

        initializeComponents();
        layoutComponents();
        setupEventHandlers();

        // Load initial data
        refreshData();
        updateButtonStates();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());

        // Filter fields
        idFromField = new JTextField(8);
        idToField = new JTextField(8);
        titleField = new JTextField(15);
        authorField = new JTextField(15);
        categoryCombo = new JComboBox<>();

        // Advanced search fields
        regexTitleField = new JTextField(15);
        regexAuthorField = new JTextField(15);
        caseSensitiveBox = new JCheckBox("Case Sensitive");
        useRegexBox = new JCheckBox("Use Regex");

        // Action buttons
        undoButton = new JButton("↶ Undo");
        redoButton = new JButton("↷ Redo");
        copyButton = new JButton("📋 Copy");
        duplicateButton = new JButton("📄 Duplicate");
        exportButton = new JButton("💾 Export CSV");
        statsButton = new JButton("📊 Statistics");
        clearFiltersButton = new JButton("🗑️ Clear Filters");

        // Set tooltips
        undoButton.setToolTipText("Undo last action (Ctrl+Z)");
        redoButton.setToolTipText("Redo last action (Ctrl+Y)");
        copyButton.setToolTipText("Copy selected books to clipboard");
        duplicateButton.setToolTipText("Duplicate selected books");
        exportButton.setToolTipText("Export visible books to CSV file");
        statsButton.setToolTipText("Show statistics about visible books");
        clearFiltersButton.setToolTipText("Clear all active filters");

        // Set button colors and styling
        styleButton(undoButton, new Color(52, 152, 219));
        styleButton(redoButton, new Color(52, 152, 219));
        styleButton(copyButton, new Color(46, 204, 113));
        styleButton(duplicateButton, new Color(155, 89, 182));
        styleButton(exportButton, new Color(230, 126, 34));
        styleButton(statsButton, new Color(241, 196, 15));
        styleButton(clearFiltersButton, new Color(231, 76, 60));

        // Configure table
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
            }
        });

        // Load categories
        loadCategories();
    }

    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void layoutComponents() {
        // Main content panel
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Create filters panel
        JPanel filtersPanel = createFiltersPanel();

        // Create action buttons panel
        JPanel actionsPanel = createActionsPanel();

        // Create table panel
        JScrollPane tableScrollPane = new JScrollPane(table);
        tableScrollPane.setPreferredSize(new Dimension(800, 400));

        // Layout
        mainPanel.add(filtersPanel, BorderLayout.NORTH);
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);
        mainPanel.add(actionsPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createFiltersPanel() {
        JPanel filtersPanel = new JPanel(new GridBagLayout());
        filtersPanel.setBorder(new TitledBorder("Filters & Search"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Basic filters row
        gbc.gridy = 0;
        gbc.gridx = 0; gbc.anchor = GridBagConstraints.EAST;
        filtersPanel.add(new JLabel("ID From:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        filtersPanel.add(idFromField, gbc);

        gbc.gridx = 2; gbc.anchor = GridBagConstraints.EAST;
        filtersPanel.add(new JLabel("ID To:"), gbc);
        gbc.gridx = 3; gbc.anchor = GridBagConstraints.WEST;
        filtersPanel.add(idToField, gbc);

        gbc.gridx = 4; gbc.anchor = GridBagConstraints.EAST;
        filtersPanel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 5; gbc.anchor = GridBagConstraints.WEST;
        filtersPanel.add(titleField, gbc);

        gbc.gridx = 6; gbc.anchor = GridBagConstraints.EAST;
        filtersPanel.add(new JLabel("Author:"), gbc);
        gbc.gridx = 7; gbc.anchor = GridBagConstraints.WEST;
        filtersPanel.add(authorField, gbc);

        gbc.gridx = 8; gbc.anchor = GridBagConstraints.EAST;
        filtersPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 9; gbc.anchor = GridBagConstraints.WEST;
        filtersPanel.add(categoryCombo, gbc);

        // Advanced search row
        gbc.gridy = 1;
        gbc.gridx = 0; gbc.anchor = GridBagConstraints.EAST;
        filtersPanel.add(new JLabel("Regex Title:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        filtersPanel.add(regexTitleField, gbc);

        gbc.gridx = 2; gbc.anchor = GridBagConstraints.EAST;
        filtersPanel.add(new JLabel("Regex Author:"), gbc);
        gbc.gridx = 3; gbc.anchor = GridBagConstraints.WEST;
        filtersPanel.add(regexAuthorField, gbc);

        gbc.gridx = 4; gbc.anchor = GridBagConstraints.WEST;
        filtersPanel.add(caseSensitiveBox, gbc);

        gbc.gridx = 5; gbc.anchor = GridBagConstraints.WEST;
        filtersPanel.add(useRegexBox, gbc);

        gbc.gridx = 6; gbc.anchor = GridBagConstraints.WEST;
        JButton applyFiltersButton = new JButton("🔍 Apply Filters");
        styleButton(applyFiltersButton, new Color(52, 73, 94));
        filtersPanel.add(applyFiltersButton, gbc);

        gbc.gridx = 7; gbc.anchor = GridBagConstraints.WEST;
        filtersPanel.add(clearFiltersButton, gbc);

        // Event handlers for filter buttons
        applyFiltersButton.addActionListener(e -> applyFilters());
        clearFiltersButton.addActionListener(e -> clearFilters());

        return filtersPanel;
    }

    private JPanel createActionsPanel() {
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionsPanel.setBorder(new TitledBorder("Actions"));

        // History buttons
        JPanel historyPanel = new JPanel(new FlowLayout());
        historyPanel.setBorder(BorderFactory.createTitledBorder("History"));
        historyPanel.add(undoButton);
        historyPanel.add(redoButton);

        // Data manipulation buttons
        JPanel dataPanel = new JPanel(new FlowLayout());
        dataPanel.setBorder(BorderFactory.createTitledBorder("Data Operations"));
        dataPanel.add(copyButton);
        dataPanel.add(duplicateButton);
        dataPanel.add(exportButton);

        // Analysis buttons
        JPanel analysisPanel = new JPanel(new FlowLayout());
        analysisPanel.setBorder(BorderFactory.createTitledBorder("Analysis"));
        analysisPanel.add(statsButton);

        actionsPanel.add(historyPanel);
        actionsPanel.add(dataPanel);
        actionsPanel.add(analysisPanel);

        return actionsPanel;
    }

    private void setupEventHandlers() {
        // History buttons
        undoButton.addActionListener(e -> {
            if (controller.undo()) {
                updateButtonStates();
                JOptionPane.showMessageDialog(this, "Action undone successfully!",
                        "Undo", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        redoButton.addActionListener(e -> {
            if (controller.redo()) {
                updateButtonStates();
                JOptionPane.showMessageDialog(this, "Action redone successfully!",
                        "Redo", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // Data operation buttons
        copyButton.addActionListener(e -> controller.copySelectedToClipboard());

        duplicateButton.addActionListener(e -> {
            controller.duplicateSelected();
            updateButtonStates();
        });

        exportButton.addActionListener(e -> controller.exportVisibleToCSV());

        statsButton.addActionListener(e -> {
            String stats = controller.getVisibleBooksStatistics();
            JTextArea textArea = new JTextArea(stats);
            textArea.setEditable(false);
            textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(400, 300));

            JOptionPane.showMessageDialog(this, scrollPane,
                    "Books Statistics", JOptionPane.INFORMATION_MESSAGE);
        });

        // Keyboard shortcuts
        setupKeyboardShortcuts();
    }

    private void setupKeyboardShortcuts() {
        // Get the input map for the panel
        InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();

        // Undo (Ctrl+Z)
        inputMap.put(KeyStroke.getKeyStroke("control Z"), "undo");
        actionMap.put("undo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (controller.canUndo()) {
                    undoButton.doClick();
                }
            }
        });

        // Redo (Ctrl+Y)
        inputMap.put(KeyStroke.getKeyStroke("control Y"), "redo");
        actionMap.put("redo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (controller.canRedo()) {
                    redoButton.doClick();
                }
            }
        });

        // Copy (Ctrl+C)
        inputMap.put(KeyStroke.getKeyStroke("control C"), "copy");
        actionMap.put("copy", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (table.getSelectedRowCount() > 0) {
                    copyButton.doClick();
                }
            }
        });

        // Duplicate (Ctrl+D)
        inputMap.put(KeyStroke.getKeyStroke("control D"), "duplicate");
        actionMap.put("duplicate", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (table.getSelectedRowCount() > 0) {
                    duplicateButton.doClick();
                }
            }
        });
    }

    private void applyFilters() {
        try {
            // Check if advanced search is being used
            if (!regexTitleField.getText().trim().isEmpty() ||
                    !regexAuthorField.getText().trim().isEmpty()) {

                controller.setAdvancedFilters(
                        regexTitleField.getText().trim(),
                        regexAuthorField.getText().trim(),
                        caseSensitiveBox.isSelected(),
                        useRegexBox.isSelected()
                );
            } else {
                // Use basic filters
                Long idFrom = null;
                Long idTo = null;

                if (!idFromField.getText().trim().isEmpty()) {
                    idFrom = Long.parseLong(idFromField.getText().trim());
                }
                if (!idToField.getText().trim().isEmpty()) {
                    idTo = Long.parseLong(idToField.getText().trim());
                }

                Category selectedCategory = (Category) categoryCombo.getSelectedItem();
                if (categoryCombo.getSelectedIndex() == 0) {
                    selectedCategory = null; // "All Categories" selected
                }

                controller.setFilters(idFrom, idTo,
                        titleField.getText().trim(),
                        authorField.getText().trim(),
                        selectedCategory);
            }

            updateButtonStates();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Invalid number format in ID fields!",
                    "Filter Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFilters() {
        // Clear all filter fields
        idFromField.setText("");
        idToField.setText("");
        titleField.setText("");
        authorField.setText("");
        categoryCombo.setSelectedIndex(0);
        regexTitleField.setText("");
        regexAuthorField.setText("");
        caseSensitiveBox.setSelected(false);
        useRegexBox.setSelected(false);

        // Remove all filters
        controller.setFilters(null, null, "", "", null);
        updateButtonStates();
    }

    private void loadCategories() {
        categoryCombo.removeAllItems();
        categoryCombo.addItem(null); // "All Categories" option

        /*try {
            List<Category> categories = categoryService.findAll();
            for (Category category : categories) {
                categoryCombo.addItem(category);
            }
        } catch (Exception e) {
            System.err.println("Error loading categories: " + e.getMessage());
        }*/

        // Custom renderer to show "All Categories" for null
        categoryCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    setText("All Categories");
                } else {
                    setText(((Category) value).getName());
                }
                return this;
            }
        });
    }

    private void updateButtonStates() {
        SwingUtilities.invokeLater(() -> {
            // Update history buttons
            undoButton.setEnabled(controller.canUndo());
            redoButton.setEnabled(controller.canRedo());

            // Update data operation buttons based on selection
            boolean hasSelection = table.getSelectedRowCount() > 0;
            copyButton.setEnabled(hasSelection);
            duplicateButton.setEnabled(hasSelection);

            // Export and stats always available
            exportButton.setEnabled(table.getRowCount() > 0);
            statsButton.setEnabled(table.getRowCount() > 0);

            // Update button text with additional info
            int selectedCount = table.getSelectedRowCount();
            if (selectedCount > 0) {
                copyButton.setText("📋 Copy (" + selectedCount + ")");
                duplicateButton.setText("📄 Duplicate (" + selectedCount + ")");
            } else {
                copyButton.setText("📋 Copy");
                duplicateButton.setText("📄 Duplicate");
            }

            // Update undo/redo button tooltips
            if (controller.canUndo()) {
                undoButton.setToolTipText("Undo: " + controller.getCurrentHistoryDescription());
            } else {
                undoButton.setToolTipText("No actions to undo");
            }
        });
    }

    public void refreshData() {
        tableModel.reload();
        loadCategories();
        updateButtonStates();
    }

    // Getters for external access
    public BookTableController getController() {
        return controller;
    }

    public JTable getTable() {
        return table;
    }

    public BookTableModel getTableModel() {
        return tableModel;
    }
}