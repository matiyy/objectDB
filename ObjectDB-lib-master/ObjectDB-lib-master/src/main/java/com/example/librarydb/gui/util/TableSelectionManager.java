package com.example.librarydb.gui.util;

import javax.swing.*;
import java.awt.*;

public class TableSelectionManager {
    private final JTable table;

    public TableSelectionManager(JTable table) {
        this.table = table;
    }

    public void selectAndScrollTo(long id, int column) {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < table.getModel().getRowCount(); i++) {
                if (((Long)table.getModel().getValueAt(i, 0)).equals(id)) {
                    int viewRow = table.convertRowIndexToModel(i);
                    table.setRowSelectionInterval(viewRow, viewRow);
                    Rectangle rect = table.getCellRect(viewRow, column, true);
                    table.scrollRectToVisible(rect);
                    break;
                }
            }
        });
    }

    public void restoreSelection(int[] selectedRows) {
        table.clearSelection();
        for (int viewRow : selectedRows) {
            table.addRowSelectionInterval(viewRow, viewRow);
        }
    }
}