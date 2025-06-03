package com.example.librarydb.gui.panel;

import com.example.librarydb.entity.Book;
import com.example.librarydb.entity.Category;
import com.example.librarydb.gui.BookTablePopupMenu;
import com.example.librarydb.gui.controller.BookTableController;
import com.example.librarydb.gui.model.BookTableModel;
import com.example.librarydb.service.CategoryService;

import javax.persistence.EntityManagerFactory;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
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
        this.table = new JTable(tableModel);
        this.controller = new BookTableController(tableModel, table, emf, onEdit);
        this.categoryService = new CategoryService(emf);

        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        setupPopupMenu(); // Add this line

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
        // 1) Panel z filtrami (u góry)
        JPanel filtersPanel = createFiltersPanel();

        // 2) ScrollPane otaczający JTables (TYLKO JEDEN RAZ)
        JScrollPane tableScrollPane = new JScrollPane(table);
        tableScrollPane.setPreferredSize(new Dimension(800, 400));

        // 3) Panel z akcjami (na dole)
        JPanel actionsPanel = createActionsPanel();

        // Dodajemy kolejne sekcje w BorderLayout: filtry, tabela, akcje
        add(filtersPanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);
        add(actionsPanel, BorderLayout.SOUTH);
    }

    private JPanel createFiltersPanel() {
        JPanel filtersPanel = new JPanel(new GridBagLayout());
        filtersPanel.setBorder(new TitledBorder("Filters & Search"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // -------------------------------
        // --- Wiersz 1: podstawowe filtry
        // -------------------------------
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

        // ----------------------------------
        // --- Wiersz 2: zaawansowane wyszukiwanie
        // ----------------------------------
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

        // Event handler dla przycisków filtrów
        applyFiltersButton.addActionListener(e -> applyFilters());
        clearFiltersButton.addActionListener(e -> clearFilters());

        return filtersPanel;
    }

    private JPanel createActionsPanel() {
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionsPanel.setBorder(new TitledBorder("Actions"));

        // --- Historia (undo/redo)
        JPanel historyPanel = new JPanel(new FlowLayout());
        historyPanel.setBorder(BorderFactory.createTitledBorder("History"));
        historyPanel.add(undoButton);
        historyPanel.add(redoButton);

        // --- Operacje na danych (copy, duplicate, export)
        JPanel dataPanel = new JPanel(new FlowLayout());
        dataPanel.setBorder(BorderFactory.createTitledBorder("Data Operations"));
        dataPanel.add(copyButton);
        dataPanel.add(duplicateButton);
        dataPanel.add(exportButton);

        // --- Analiza (statistics)
        JPanel analysisPanel = new JPanel(new FlowLayout());
        analysisPanel.setBorder(BorderFactory.createTitledBorder("Analysis"));
        analysisPanel.add(statsButton);

        actionsPanel.add(historyPanel);
        actionsPanel.add(dataPanel);
        actionsPanel.add(analysisPanel);

        return actionsPanel;
    }

    private void setupEventHandlers() {
        // --- Undo / Redo
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

        // --- Copy / Duplicate / Export / Stats
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

        // --- Skróty klawiaturowe
        setupKeyboardShortcuts();
    }

    private void setupPopupMenu() {
        // Get the parent Frame for the popup menu
        Frame parentFrame = getParentFrame();

        // Create and attach the popup menu
        new BookTablePopupMenu(table, controller, parentFrame);
    }

    private Frame getParentFrame() {
        // Find the parent Frame of this component
        Container parent = getParent();
        while (parent != null) {
            if (parent instanceof Frame) {
                return (Frame) parent;
            }
            parent = parent.getParent();
        }

        // If no Frame found, create a dummy one or return null
        // This shouldn't happen in normal circumstances
        return new JFrame(); // or return null if you prefer
    }

    private void setupKeyboardShortcuts() {
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
            // Sprawdź, czy używamy regexów
            if (!regexTitleField.getText().trim().isEmpty() ||
                    !regexAuthorField.getText().trim().isEmpty()) {

                controller.setAdvancedFilters(
                        regexTitleField.getText().trim(),
                        regexAuthorField.getText().trim(),
                        caseSensitiveBox.isSelected(),
                        useRegexBox.isSelected()
                );
            } else {
                // Podstawowe filtry
                Long idFrom = null;
                Long idTo = null;

                if (!idFromField.getText().trim().isEmpty()) {
                    idFrom = Long.parseLong(idFromField.getText().trim());
                }
                if (!idToField.getText().trim().isEmpty()) {
                    idTo = Long.parseLong(idToField.getText().trim());
                }

                // POPRAWKA: Prawidłowe pobieranie wybranej kategorii
                Category selectedCategory = null;
                Object selectedItem = categoryCombo.getSelectedItem();

                // Jeśli wybrana kategoria to nie "All Categories" (null)
                if (selectedItem != null && selectedItem instanceof Category) {
                    selectedCategory = (Category) selectedItem;
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
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error applying filters: " + ex.getMessage(),
                    "Filter Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFilters() {
        // Wyczyść wszystkie pola filtrów
        idFromField.setText("");
        idToField.setText("");
        titleField.setText("");
        authorField.setText("");
        categoryCombo.setSelectedIndex(0); // "All Categories"
        regexTitleField.setText("");
        regexAuthorField.setText("");
        caseSensitiveBox.setSelected(false);
        useRegexBox.setSelected(false);

        // Usuń wszystkie filtry w kontrolerze
        controller.setFilters(null, null, "", "", null);
        controller.setAdvancedFilters("", "", false, false);
        updateButtonStates();
    }

    private void loadCategories() {
        SwingUtilities.invokeLater(() -> {
            try {
                categoryCombo.removeAllItems();

                // Pierwsza opcja - "All Categories" (null)
                categoryCombo.addItem(null);

                // POPRAWKA: Odkomentowane ładowanie kategorii z bazy danych
                List<Category> categories = categoryService.findAll();
                for (Category category : categories) {
                    categoryCombo.addItem(category);
                }

                // Ustawienie renderera do wyświetlania nazw kategorii
                categoryCombo.setRenderer(new DefaultListCellRenderer() {
                    @Override
                    public Component getListCellRendererComponent(JList<?> list, Object value,
                                                                  int index, boolean isSelected, boolean cellHasFocus) {
                        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                        if (value == null) {
                            setText("All Categories");
                            setFont(getFont().deriveFont(Font.ITALIC));
                        } else if (value instanceof Category) {
                            Category category = (Category) value;
                            setText(category.getName());
                            setFont(getFont().deriveFont(Font.PLAIN));
                        }
                        return this;
                    }
                });

                // Domyślnie wybierz "All Categories"
                categoryCombo.setSelectedIndex(0);

            } catch (Exception e) {
                System.err.println("Error loading categories: " + e.getMessage());
                e.printStackTrace();

                // W przypadku błędu, dodaj przynajmniej opcję "All Categories"
                if (categoryCombo.getItemCount() == 0) {
                    categoryCombo.addItem(null);
                    categoryCombo.setRenderer(new DefaultListCellRenderer() {
                        @Override
                        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                                      int index, boolean isSelected, boolean cellHasFocus) {
                            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                            setText("All Categories");
                            setFont(getFont().deriveFont(Font.ITALIC));
                            return this;
                        }
                    });
                }

                // Pokaż błąd użytkownikowi
                JOptionPane.showMessageDialog(this,
                        "Could not load categories from database.\nUsing 'All Categories' only.",
                        "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    private void updateButtonStates() {
        SwingUtilities.invokeLater(() -> {
            // Historia
            undoButton.setEnabled(controller.canUndo());
            redoButton.setEnabled(controller.canRedo());

            // Operacje na danych (w oparciu o zaznaczenie)
            boolean hasSelection = table.getSelectedRowCount() > 0;
            copyButton.setEnabled(hasSelection);
            duplicateButton.setEnabled(hasSelection);

            // Export i statystyki zawsze dostępne, jeśli jest cokolwiek w tabeli
            exportButton.setEnabled(table.getRowCount() > 0);
            statsButton.setEnabled(table.getRowCount() > 0);

            // Zaktualizuj liczbę zaznaczonych
            int selectedCount = table.getSelectedRowCount();
            if (selectedCount > 0) {
                copyButton.setText("📋 Copy (" + selectedCount + ")");
                duplicateButton.setText("📄 Duplicate (" + selectedCount + ")");
            } else {
                copyButton.setText("📋 Copy");
                duplicateButton.setText("📄 Duplicate");
            }

            // Tooltip do undo/redo z opisem bieżącej akcji
            if (controller.canUndo()) {
                undoButton.setToolTipText("Undo: " + controller.getCurrentHistoryDescription());
            } else {
                undoButton.setToolTipText("No actions to undo");
            }
        });
    }

    public void refreshData() {
        tableModel.reload();
        loadCategories();      // odświeża też listę kategorii (np. jeżeli pojawiły się nowe)
        updateButtonStates();
    }

    // Gettery, jeżeli z zewnątrz ktoś potrzebuje sterować kontrolerem/tabelą
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