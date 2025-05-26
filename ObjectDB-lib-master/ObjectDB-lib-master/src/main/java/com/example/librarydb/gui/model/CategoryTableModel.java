package com.example.librarydb.gui.model;

import com.example.librarydb.entity.Category;
import com.example.librarydb.service.CategoryService;

import javax.persistence.EntityManagerFactory;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class CategoryTableModel extends AbstractTableModel {
    private final CategoryService service;
    private final List<Category> data = new ArrayList<>();
    private final String[] columns = {"ID", "Name", "Description", "Books Count"};
    
    public CategoryTableModel(EntityManagerFactory emf) {
        this.service = new CategoryService(emf);
        reload();
    }
    
    public void reload() {
        data.clear();
        data.addAll(service.listAll());
        fireTableDataChanged();
    }
    
    public Category getCategoryAt(int row) {
        return data.get(row);
    }

    @Override 
    public int getRowCount() { 
        return data.size(); 
    }

    @Override 
    public int getColumnCount() { 
        return columns.length; 
    }

    @Override 
    public String getColumnName(int col) { 
        return columns[col]; 
    }
    
    @Override
    public Object getValueAt(int row, int col) {
        Category c = data.get(row);
        return switch(col) {
            case 0 -> c.getId();
            case 1 -> c.getName();
            case 2 -> c.getDescription();
            case 3 -> c.getBooks().size();
            default -> null;
        };
    }

    public CategoryService getService() {
        return service;
    }
}