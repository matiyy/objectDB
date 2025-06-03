package com.example.librarydb.gui;

import com.example.librarydb.entity.Book;
import com.example.librarydb.gui.controller.BookTableController;
import com.example.librarydb.gui.dialog.BookFormDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Comparator;

public class BookTablePopupMenu {
    public BookTablePopupMenu(JTable table,
                              BookTableController controller,
                              Frame owner) {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem miNew    = new JMenuItem("New Book");
        JMenuItem miEdit   = new JMenuItem("Edit Book");
        JMenuItem miDelete = new JMenuItem("Delete Book(s)");
        popup.add(miNew);
        popup.add(miEdit);
        popup.add(miDelete);

        // Show popup on right-click, update menu item enablement
        table.addMouseListener(new MouseAdapter() {
            private void showIfPopup(MouseEvent e) {
                if (!e.isPopupTrigger()) return;

                // Get clicked row and update selection
                int clickedRow = table.rowAtPoint(e.getPoint());
                if (clickedRow >= 0 && !table.isRowSelected(clickedRow)) {
                    table.getSelectionModel().setSelectionInterval(clickedRow, clickedRow);
                }

                int selCount = table.getSelectedRowCount();
                miNew.setEnabled(true);
                miEdit.setEnabled(selCount >= 1); // Allow editing multiple rows
                miDelete.setEnabled(selCount >= 1);

                popup.show(table, e.getX(), e.getY());
            }

            @Override public void mousePressed(MouseEvent e)  { showIfPopup(e); }
            @Override public void mouseReleased(MouseEvent e) { showIfPopup(e); }
        });

        // New Book - insert at clicked position
        miNew.addActionListener(ae -> {
            int selectedRow = table.getSelectedRow();
            BookFormDialog dlg = new BookFormDialog(owner, new Book());
            dlg.setVisible(true);
            
            if (dlg.isSaved()) {
                controller.insertAtPosition(
                    selectedRow >= 0 ? selectedRow : table.getRowCount(),
                    dlg.getBook().getTitle(),
                    dlg.getBook().getAuthor(),
                    dlg.getBook().getCategory() // Pass the category
                );
            }
        });

        // Edit Book(s) - handle multiple selection
        miEdit.addActionListener(ae -> {
            int[] selectedRows = table.getSelectedRows();
            if (selectedRows.length == 0) return;

            int firstModelRow = table.convertRowIndexToModel(selectedRows[0]);
            Book firstBook = controller.getModel().getBookAt(firstModelRow);
            
            BookFormDialog dlg = new BookFormDialog(owner, firstBook);
            dlg.setVisible(true);
            
            if (dlg.isSaved()) {
                controller.batchUpdate(
                    selectedRows,
                    dlg.getBook().getTitle(),
                    dlg.getBook().getAuthor(),
                    dlg.getBook().getCategory() // Pass the category
                );
            }
        });

        // Delete Book(s) → delete all selected
        miDelete.addActionListener(ae -> {
            if (!controller.confirmDelete(owner)) return;
            int[] viewRows = table.getSelectedRows();
            // convert to model indices and sort descending
            Integer[] modelRows = Arrays.stream(viewRows)
                    .map(table::convertRowIndexToModel)
                    .boxed()
                    .sorted(Comparator.reverseOrder())
                    .toArray(Integer[]::new);

            for (int mr : modelRows) {
                Long id = (Long) controller.getModel().getValueAt(mr, 0);
                controller.getBookService().delete(id);
            }
            controller.getModel().reload();
        });
    }
}
