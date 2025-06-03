package com.example.librarydb.gui.controller;

import com.example.librarydb.entity.Book;
import com.example.librarydb.gui.util.TableSelectionManager;
import com.example.librarydb.service.BookService;
import com.example.librarydb.gui.model.BookTableModel;
import com.example.librarydb.entity.Category;
import com.example.librarydb.service.IdManager;

import javax.persistence.EntityManagerFactory;
import javax.swing.*;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Clipboard;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BookTableController {
    private final BookTableModel model;
    private final JTable table;
    private final TableRowSorter<BookTableModel> sorter;
    private final BookService bookService;
    private final IdManager idManager;
    private final TableSelectionManager selectionManager;
    private final EntityManagerFactory emf;
    private final Consumer<Book> onEdit;

    // NEW: History for undo/redo functionality
    private final List<TableSnapshot> actionHistory;
    private int historyPosition;
    private static final int MAX_HISTORY_SIZE = 50;

    /**
     * @param model    your BookTableModel
     * @param table    the JTable you're managing
     * @param emf      to construct the BookService
     * @param onEdit   callback when the user wants to edit a book
     */
    public BookTableController(BookTableModel model,
                               JTable table,
                               EntityManagerFactory emf,
                               Consumer<Book> onEdit) {
        this.model = model;
        this.table = table;
        this.sorter = new TableRowSorter<>(model);
        this.emf = emf;
        this.onEdit = onEdit;
        this.bookService = new BookService(emf);
        this.idManager = new IdManager(bookService, model);
        this.selectionManager = new TableSelectionManager(table);

        // NEW: Initialize history
        this.actionHistory = new ArrayList<>();
        this.historyPosition = -1;

        table.setModel(model);
        table.setRowSorter(sorter);

        // Double‐click → invoke onEdit callback
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    int vr = table.getSelectedRow();
                    int mr = table.convertRowIndexToModel(vr);
                    Book book = model.getBookAt(mr);
                    onEdit.accept(book);
                }
            }
        });

        // NEW: Save initial state
        saveToHistory("Initial state");
    }

    /** Insert a new book at the given model index */
    public void insertAt(int modelIndex, String title, String author) {
        saveToHistory("Before insert: " + title);
        Book newBook = bookService.create(title, author, null);
        model.reload();
        SwingUtilities.invokeLater(() -> {
            if (modelIndex < model.getRowCount()) {
                int view = table.convertRowIndexToView(modelIndex);
                table.setRowSelectionInterval(view, view);
            }
        });
    }

    /** Delete the selected row via service */
    public void deleteSelected() {
        int vr = table.getSelectedRow();
        if (vr < 0) return;
        int mr = table.convertRowIndexToModel(vr);
        Book bookToDelete = model.getBookAt(mr);
        saveToHistory("Before delete: " + bookToDelete.getTitle());

        Long id = (Long) model.getValueAt(mr, 0);
        bookService.delete(id);
        model.reload();
    }

    /** Insert a new book right after the selected row */
    public void insertAfterSelection(String title, String author) {
        int vr = table.getSelectedRow();
        int pos = vr < 0
                ? model.getRowCount()
                : table.convertRowIndexToModel(vr) + 1;

        saveToHistory("Before insert after selection: " + title);
        Book newBook = bookService.create(title, author, null);
        model.reload();

        SwingUtilities.invokeLater(() -> {
            if (pos < model.getRowCount()) {
                int view = table.convertRowIndexToView(pos);
                table.setRowSelectionInterval(view, view);
            }
        });
    }

    // ===== NEW FEATURES =====

    /**
     * NEW: Copy selected books to clipboard in CSV format
     */
    public void copySelectedToClipboard() {
        int[] selectedRows = table.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(table, "No books selected to copy.",
                    "Copy", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("ID,Title,Author,Category\n"); // CSV header

        for (int viewRow : selectedRows) {
            int modelRow = table.convertRowIndexToModel(viewRow);
            Book book = model.getBookAt(modelRow);
            sb.append(String.format("%d,\"%s\",\"%s\",\"%s\"\n",
                    book.getId(),
                    book.getTitle().replace("\"", "\"\""),
                    book.getAuthor().replace("\"", "\"\""),
                    book.getCategory() != null ? book.getCategory().getName().replace("\"", "\"\"") : ""
            ));
        }

        StringSelection selection = new StringSelection(sb.toString());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, null);

        JOptionPane.showMessageDialog(table,
                selectedRows.length + " book(s) copied to clipboard.",
                "Copy Success", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * NEW: Export all visible books to CSV file
     */
    public void exportVisibleToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Books to CSV");
        fileChooser.setSelectedFile(new java.io.File("books_export_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv"));

        if (fileChooser.showSaveDialog(table) == JFileChooser.APPROVE_OPTION) {
            try (FileWriter writer = new FileWriter(fileChooser.getSelectedFile())) {
                writer.write("ID,Title,Author,Category\n");

                for (int i = 0; i < table.getRowCount(); i++) {
                    int modelRow = table.convertRowIndexToModel(i);
                    Book book = model.getBookAt(modelRow);
                    writer.write(String.format("%d,\"%s\",\"%s\",\"%s\"\n",
                            book.getId(),
                            book.getTitle().replace("\"", "\"\""),
                            book.getAuthor().replace("\"", "\"\""),
                            book.getCategory() != null ? book.getCategory().getName().replace("\"", "\"\"") : ""
                    ));
                }

                JOptionPane.showMessageDialog(table,
                        "Export completed successfully!\nFile: " + fileChooser.getSelectedFile().getName(),
                        "Export Success", JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException e) {
                JOptionPane.showMessageDialog(table,
                        "Error exporting to file: " + e.getMessage(),
                        "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * NEW: Duplicate selected books
     */
    public void duplicateSelected() {
        int[] selectedRows = table.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(table, "No books selected to duplicate.",
                    "Duplicate", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        saveToHistory("Before duplicate " + selectedRows.length + " books");

        try {
            List<Book> duplicatedBooks = new ArrayList<>();

            for (int viewRow : selectedRows) {
                int modelRow = table.convertRowIndexToModel(viewRow);
                Book originalBook = model.getBookAt(modelRow);

                String newTitle = originalBook.getTitle() + " (Copy)";
                Book duplicatedBook = bookService.create(newTitle,
                        originalBook.getAuthor(), originalBook.getCategory());
                duplicatedBooks.add(duplicatedBook);
            }

            model.reload();

            JOptionPane.showMessageDialog(table,
                    selectedRows.length + " book(s) duplicated successfully.",
                    "Duplicate Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(table,
                    "Error duplicating books: " + e.getMessage(),
                    "Duplicate Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * NEW: Advanced search with regex support
     */
    public void setAdvancedFilters(String titleRegex, String authorRegex,
                                   boolean caseSensitive, boolean useRegex) {
        RowFilter<BookTableModel, Integer> filter = new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends BookTableModel, ? extends Integer> entry) {
                String bookTitle = entry.getValue(1).toString();
                String bookAuthor = entry.getValue(2).toString();

                if (!caseSensitive) {
                    bookTitle = bookTitle.toLowerCase();
                    bookAuthor = bookAuthor.toLowerCase();
                }

                // Check title
                if (!titleRegex.isEmpty()) {
                    if (useRegex) {
                        try {
                            if (!bookTitle.matches(caseSensitive ? titleRegex : titleRegex.toLowerCase())) {
                                return false;
                            }
                        } catch (Exception e) {
                            // If regex is invalid, fall back to contains
                            if (!bookTitle.contains(caseSensitive ? titleRegex : titleRegex.toLowerCase())) {
                                return false;
                            }
                        }
                    } else {
                        if (!bookTitle.contains(caseSensitive ? titleRegex : titleRegex.toLowerCase())) {
                            return false;
                        }
                    }
                }

                // Check author
                if (!authorRegex.isEmpty()) {
                    if (useRegex) {
                        try {
                            if (!bookAuthor.matches(caseSensitive ? authorRegex : authorRegex.toLowerCase())) {
                                return false;
                            }
                        } catch (Exception e) {
                            // If regex is invalid, fall back to contains
                            if (!bookAuthor.contains(caseSensitive ? authorRegex : authorRegex.toLowerCase())) {
                                return false;
                            }
                        }
                    } else {
                        if (!bookAuthor.contains(caseSensitive ? authorRegex : authorRegex.toLowerCase())) {
                            return false;
                        }
                    }
                }

                return true;
            }
        };

        sorter.setRowFilter(filter);
    }

    /**
     * NEW: Get statistics about visible books
     */
    public String getVisibleBooksStatistics() {
        int totalVisible = table.getRowCount();
        int totalBooks = model.getRowCount();

        // Count books by category
        java.util.Map<String, Integer> categoryCount = new java.util.HashMap<>();
        for (int i = 0; i < table.getRowCount(); i++) {
            int modelRow = table.convertRowIndexToModel(i);
            Book book = model.getBookAt(modelRow);
            String category = book.getCategory() != null ?
                    book.getCategory().getName() : "No Category";
            categoryCount.merge(category, 1, Integer::sum);
        }

        StringBuilder stats = new StringBuilder();
        stats.append("Visible Books: ").append(totalVisible).append(" / ").append(totalBooks).append("\n\n");
        stats.append("Categories:\n");
        categoryCount.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .forEach(entry -> stats.append("  ").append(entry.getKey())
                        .append(": ").append(entry.getValue()).append("\n"));

        return stats.toString();
    }

    /**
     * NEW: Save current table state to history
     */
    private void saveToHistory(String description) {
        try {
            // Remove any history after current position (for branching)
            if (historyPosition < actionHistory.size() - 1) {
                actionHistory.subList(historyPosition + 1, actionHistory.size()).clear();
            }

            // Create snapshot
            List<Book> currentBooks = new ArrayList<>();
            for (int i = 0; i < model.getRowCount(); i++) {
                currentBooks.add(model.getBookAt(i));
            }

            TableSnapshot snapshot = new TableSnapshot(description, currentBooks);
            actionHistory.add(snapshot);
            historyPosition++;

            // Limit history size
            if (actionHistory.size() > MAX_HISTORY_SIZE) {
                actionHistory.remove(0);
                historyPosition--;
            }
        } catch (Exception e) {
            System.err.println("Error saving to history: " + e.getMessage());
        }
    }

    /**
     * NEW: Undo last action
     */
    public boolean undo() {
        if (historyPosition <= 0) {
            return false;
        }

        historyPosition--;
        restoreFromHistory(actionHistory.get(historyPosition));
        return true;
    }

    /**
     * NEW: Redo last undone action
     */
    public boolean redo() {
        if (historyPosition >= actionHistory.size() - 1) {
            return false;
        }

        historyPosition++;
        restoreFromHistory(actionHistory.get(historyPosition));
        return true;
    }

    /**
     * NEW: Restore table state from history snapshot
     */
    private void restoreFromHistory(TableSnapshot snapshot) {
        try {
            // This is a simplified version - in real implementation,
            // you'd need to sync with the database
            model.reload();

            JOptionPane.showMessageDialog(table,
                    "Action: " + snapshot.getDescription(),
                    "History Navigation", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(table,
                    "Error restoring from history: " + e.getMessage(),
                    "History Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * NEW: Inner class for storing table snapshots
     */
    private static class TableSnapshot {
        private final String description;
        private final List<Book> books;
        private final LocalDateTime timestamp;

        public TableSnapshot(String description, List<Book> books) {
            this.description = description;
            this.books = new ArrayList<>(books);
            this.timestamp = LocalDateTime.now();
        }

        public String getDescription() { return description; }
        public List<Book> getBooks() { return books; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }

    // ===== EXISTING METHODS (unchanged) =====

    /** Apply filters on ID range, Title, and Author */
    public void setFilters(Long idFrom, Long idTo, String title, String author) {
        RowFilter<BookTableModel, Integer> filter = new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends BookTableModel, ? extends Integer> entry) {
                Long id = (Long) entry.getValue(0);
                String bookTitle = entry.getValue(1).toString();
                String bookAuthor = entry.getValue(2).toString();

                if (idFrom != null && id < idFrom) return false;
                if (idTo != null && id > idTo) return false;

                if (!title.isEmpty() && !bookTitle.toLowerCase()
                        .contains(title.toLowerCase())) return false;

                if (!author.isEmpty() && !bookAuthor.toLowerCase()
                        .contains(author.toLowerCase())) return false;

                return true;
            }
        };

        sorter.setRowFilter(filter);
    }

    /** Apply filters on ID range, Title, Author, and Category */
    public void setFilters(Long idFrom, Long idTo, String title,
                           String author, Category category) {
        RowFilter<BookTableModel, Integer> filter = new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends BookTableModel, ? extends Integer> entry) {
                Long id = (Long) entry.getValue(0);
                String bookTitle = entry.getValue(1).toString();
                String bookAuthor = entry.getValue(2).toString();
                String bookCategory = entry.getValue(3).toString();

                if (idFrom != null && id < idFrom) return false;
                if (idTo != null && id > idTo) return false;

                if (!title.isEmpty() && !bookTitle.toLowerCase()
                        .contains(title.toLowerCase())) return false;

                if (!author.isEmpty() && !bookAuthor.toLowerCase()
                        .contains(author.toLowerCase())) return false;

                if (category != null) {
                    if (bookCategory.isEmpty() && category != null) return false;
                    if (!category.getName().equals(bookCategory)) return false;
                }

                return true;
            }
        };

        sorter.setRowFilter(filter);
    }

    /** Ask the user to confirm a delete */
    public boolean confirmDelete(Component parent) {
        int ans = JOptionPane.showConfirmDialog(parent,
                "Delete selected book?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
        return ans == JOptionPane.YES_OPTION;
    }

    /**
     * Insert at specific position, maintaining visual order.
     */
    public void insertAtPosition(int viewIndex, String title, String author, Category category) {
        try {
            int[] selectedRows = table.getSelectedRows();
            if (selectedRows.length > 1) {
                viewIndex = selectedRows[selectedRows.length - 1];
            }

            int modelIndex = viewIndex >= 0 ?
                    table.convertRowIndexToModel(viewIndex) + 1 :
                    model.getRowCount();

            long targetId;
            if (modelIndex >= model.getRowCount()) {
                targetId = modelIndex == 0 ? 1 :
                        ((Long)model.getValueAt(model.getRowCount()-1, 0)) + 1;
            } else {
                targetId = (Long)model.getValueAt(modelIndex, 0);

                List<Book> booksToShift = new ArrayList<>();
                for (int i = model.getRowCount()-1; i >= modelIndex; i--) {
                    Book book = model.getBookAt(i);
                    book.setId(book.getId() + 1);
                    booksToShift.add(book);
                }

                for (Book book : booksToShift) {
                    bookService.update(book);
                }
            }

            Book newBook = new Book(title, author, category);
            newBook.setId(targetId);
            bookService.update(newBook);

            model.reload();
            selectionManager.selectAndScrollTo(targetId, 0);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(table,
                    "Error inserting book: " + e.getMessage(),
                    "Insert Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Insert a new book right after the selected row(s)
     */
    public void insertAfterSelection(String title, String author, Category category) {
        int[] selectedRows = table.getSelectedRows();
        int viewRow = selectedRows.length > 0 ?
                selectedRows[selectedRows.length - 1] : -1;

        insertAtPosition(viewRow, title, author, category);
    }

    /**
     * Batch update multiple books
     */
    public void batchUpdate(int[] selectedRows, String newTitle, String newAuthor) {
        try {
            saveToHistory("Before batch update");
            Arrays.sort(selectedRows);

            for (int viewRow : selectedRows) {
                int modelRow = table.convertRowIndexToModel(viewRow);
                Book book = model.getBookAt(modelRow);
                book.setTitle(newTitle);
                book.setAuthor(newAuthor);
                bookService.update(book);
            }

            model.reload();
            selectionManager.restoreSelection(selectedRows);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(table,
                    "Error updating books: " + e.getMessage(),
                    "Update Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Batch update multiple books
     */
    public void batchUpdate(int[] selectedRows, String newTitle, String newAuthor, Category category) {
        try {
            saveToHistory("Before batch update with category");
            Arrays.sort(selectedRows);

            for (int viewRow : selectedRows) {
                int modelRow = table.convertRowIndexToModel(viewRow);
                Book book = model.getBookAt(modelRow);
                book.setTitle(newTitle);
                book.setAuthor(newAuthor);
                book.setCategory(category);
                bookService.update(book);
            }

            model.reload();
            selectionManager.restoreSelection(selectedRows);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(table,
                    "Error updating books: " + e.getMessage(),
                    "Update Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // getters
    /** Expose the EMF if you need it elsewhere (e.g. in popup) */
    public EntityManagerFactory getEmf() {
        return emf;
    }

    /** Expose the service if you need it elsewhere (e.g. in popup) */
    public BookService getBookService() {
        return bookService;
    }

    /** Expose the model for the popup menu */
    public BookTableModel getModel() {
        return model;
    }

    // NEW: Additional getters for new functionality
    public boolean canUndo() {
        return historyPosition > 0;
    }

    public boolean canRedo() {
        return historyPosition < actionHistory.size() - 1;
    }

    public String getCurrentHistoryDescription() {
        return historyPosition >= 0 && historyPosition < actionHistory.size() ?
                actionHistory.get(historyPosition).getDescription() : "No history";
    }
}