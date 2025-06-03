package com.example.librarydb.gui;

import com.example.librarydb.config.JPAConfig;
import com.example.librarydb.gui.dialog.BookFormDialog;
import com.example.librarydb.gui.dialog.CategoryManagerDialog;
import com.example.librarydb.gui.panel.BookTablePanel;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicReference;

public class MainWindow extends JFrame {

    public MainWindow() {
        super("Library Book Manager");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Używamy jednoelementowej tablicy, żeby móc odwołać się do bookPanel wewnątrz lambdy:
        final BookTablePanel[] bookPanelHolder = new BookTablePanel[1];

        // Tworzymy BookTablePanel i wypełniamy holder:
        bookPanelHolder[0] = new BookTablePanel(
                JPAConfig.getEntityManagerFactory(),
                book -> {
                    // callback wywoływany przy edycji książki
                    BookFormDialog dlg = new BookFormDialog(this, book);
                    dlg.setVisible(true);
                    if (dlg.isSaved()) {
                        new com.example.librarydb.service.BookService(
                                JPAConfig.getEntityManagerFactory()
                        ).update(dlg.getBook());
                        // dzięki holderowi możemy tu odwołać się do właściwego bookPanel
                        bookPanelHolder[0].getTableModel().reload();
                    }
                }
        );

        // Dodajemy panel z tabelą do CENTER (ponieważ BookTablePanel wewnętrznie sam tworzy JTable)
        add(bookPanelHolder[0], BorderLayout.CENTER);

        // Dodajemy pasek menu
        setupMenuBar();

        setVisible(true);
    }

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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainWindow());
    }
}
