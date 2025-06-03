package com.example.librarydb.service;

import com.example.librarydb.entity.Book;
import com.example.librarydb.gui.model.BookTableModel;

import java.util.ArrayList;
import java.util.List;

public class IdManager {
    private final BookService bookService;
    private final BookTableModel model;

    public IdManager(BookService bookService, BookTableModel model) {
        this.bookService = bookService;
        this.model = model;
    }

    /**
     * Calculate ID for new book insertion after given index
     */
    public long calculateNewId(int modelIndex) {
        if (modelIndex < 0) {
            throw new IllegalArgumentException("Index cannot be negative: " + modelIndex);
        }

        int rowCount = model.getRowCount();
        if (rowCount == 0) {
            return 1; // First book
        }

        // If inserting at the end, just increment last ID
        if (modelIndex >= rowCount) {
            Object lastIdObj = model.getValueAt(rowCount - 1, 0);
            if (!(lastIdObj instanceof Long)) {
                throw new IllegalStateException("Invalid ID type at last row");
            }
            return ((Long) lastIdObj) + 1;
        }

        // Get target ID (where we want to insert)
        Object targetIdObj = model.getValueAt(modelIndex, 0);
        if (!(targetIdObj instanceof Long)) {
            throw new IllegalStateException("Invalid ID type at index: " + modelIndex);
        }

        long targetId = (Long) targetIdObj;

        // Shift all IDs from this point up
        shiftIdsUp(modelIndex);

        return targetId;
    }

    /**
     * Shift all IDs up by 1 starting from given index
     */
    private void shiftIdsUp(int fromIndex) {
        List<Book> booksToShift = new ArrayList<>();
        for (int i = model.getRowCount() - 1; i >= fromIndex; i--) {
            Book book = model.getBookAt(i);
            if (book != null) {
                booksToShift.add(book);
            }
        }

        for (Book book : booksToShift) {
            try {
                book.setId(book.getId() + 1);
                bookService.update(book);
            } catch (Exception e) {
                System.err.println("Błąd podczas aktualizacji książki ID=" + book.getId() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
