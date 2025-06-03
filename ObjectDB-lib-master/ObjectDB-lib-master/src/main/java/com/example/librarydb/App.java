package com.example.librarydb;

import com.example.librarydb.config.JPAConfig;
import com.example.librarydb.service.DataGenerator;
import com.example.librarydb.gui.MainWindow;

import javax.swing.*;
import java.io.File;

public class App {

    public static void main(String[] args) {
        try {
            // Check if database exists
            File dbFile = JPAConfig.getDatabaseFile();
            boolean needsInitialData = !dbFile.exists();

            // Initialize EMF (this will create db file if needed)
            var emf = JPAConfig.getEntityManagerFactory();
            
            // Generate sample data only for new database
            if (needsInitialData) {
                DataGenerator generator = new DataGenerator(emf);
                generator.generate(100);
            }

            // Launch GUI
            SwingUtilities.invokeLater(() -> {
                MainWindow mainWindow = new MainWindow();
                mainWindow.setVisible(true);
            });

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, 
                "Error starting application: " + e.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }

        // JPA shutdown will be handled on app close
        Runtime.getRuntime().addShutdownHook(new Thread(JPAConfig::shutdown));
    }
}