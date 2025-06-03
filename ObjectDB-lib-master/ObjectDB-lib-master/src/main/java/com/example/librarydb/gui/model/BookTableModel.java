package com.example.librarydb.gui.model;

import com.example.librarydb.entity.Book;
import com.example.librarydb.service.BookService;

import javax.persistence.EntityManagerFactory;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class BookTableModel extends AbstractTableModel {
    private final BookService service;
    private final List<Book> data = new ArrayList<>();
    private final String[] columns = {"ID", "Title", "Author", "Category"};

    public BookTableModel(EntityManagerFactory emf) {
        this.service = new BookService(emf);
        reload();
    }

    public void reload() {
        data.clear();
        data.addAll(service.listAll());
        fireTableDataChanged();
    }

    public Book getBookAt(int row) {
        return data.get(row);
    }

    public void insertAt(int index, Book b) {
        service.create(b.getTitle(), b.getAuthor(), b.getCategory());
        reload();
    }

    @Override public int getRowCount()   { return data.size(); }
    @Override public int getColumnCount(){ return columns.length; }
    @Override public String getColumnName(int col){ return columns[col]; }
    @Override
    public Object getValueAt(int row, int col) {
        Book b = data.get(row);
        return switch (col) {
            case 0 -> b.getId();
            case 1 -> b.getTitle();
            case 2 -> b.getAuthor();
            case 3 -> b.getCategory() != null ? b.getCategory().getName() : "";
            default -> null;
        };
    }
}
