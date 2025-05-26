package com.example.librarydb.gui;

import com.example.librarydb.config.JPAConfig;
import com.example.librarydb.gui.controller.BookTableController;
import com.example.librarydb.gui.dialog.BookFormDialog;
import com.example.librarydb.gui.dialog.CategoryManagerDialog;
import com.example.librarydb.gui.panel.BookTablePanel;
import com.example.librarydb.gui.model.BookTableModel;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {
    private final BookTableModel model;
    private final BookTableController controller;

    public MainWindow() {
        super("Library Book Manager");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(700, 500);
        setLayout(new BorderLayout());

        // 1) Table model & view
        model = new BookTableModel(JPAConfig.getEntityManagerFactory());
        JTable table = new JTable();

        // 2) Controller with an onEdit callback
        controller = new BookTableController(
                model,
                table,
                JPAConfig.getEntityManagerFactory(),
                book -> {
                    // edit callback
                    BookFormDialog dlg = new BookFormDialog(this, book);
                    dlg.setVisible(true);
                    if (dlg.isSaved()) {
                        new com.example.librarydb.service.BookService(
                                JPAConfig.getEntityManagerFactory())
                                .update(dlg.getBook());
                        model.reload();
                    }
                }
        );
        // hook up the popup menu
        new BookTablePopupMenu(table, controller, this);

        // 3) Top panel with just the filter
        add(new BookTablePanel(JPAConfig.getEntityManagerFactory(), book -> {
            // edit callback - taki sam jak w kontrollerze
            BookFormDialog dlg = new BookFormDialog(this, book);
            dlg.setVisible(true);
            if (dlg.isSaved()) {
                new com.example.librarydb.service.BookService(
                        JPAConfig.getEntityManagerFactory())
                        .update(dlg.getBook());
                model.reload();
            }
        }), BorderLayout.NORTH);

        // 4) Main table area
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Add menu bar with category management
        setupMenuBar();
    }

    // Add menu bar with category management
    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu adminMenu = new JMenu("Admin");
        JMenuItem categoryManager = new JMenuItem("Manage Categories");
        categoryManager.addActionListener(e -> {
            CategoryManagerDialog dialog = new CategoryManagerDialog(this);
            dialog.setVisible(true);
        });
        adminMenu.add(categoryManager);
        menuBar.add(adminMenu);
        setJMenuBar(menuBar);
    }
}