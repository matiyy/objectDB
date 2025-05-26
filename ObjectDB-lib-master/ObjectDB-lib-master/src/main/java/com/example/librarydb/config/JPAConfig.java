package com.example.librarydb.config;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class JPAConfig {
    private static EntityManagerFactory emf;
    private static String dbPath;
    
    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            String dbPath = getOrCreateDatabasePath();
            Map<String, String> properties = new HashMap<>();
            properties.put("javax.persistence.jdbc.url", dbPath);
            emf = Persistence.createEntityManagerFactory("libraryPU", properties);
        }
        return emf;
    }

    private static String getOrCreateDatabasePath() {
        if (dbPath == null) {
            // Get the project directory
            Path projectDir = Paths.get("").toAbsolutePath();
            // Go up one level to parent directory
            Path parentDir = projectDir.getParent();
            // Create database path in parent directory
            Path databasePath = parentDir.resolve("library.odb");
            dbPath = databasePath.toString();
        }
        // Create parent directories if they don't exist
        File dbFile = new File(dbPath);
        if (!dbFile.exists()) {
            dbFile.getParentFile().mkdirs();
        }
        return dbPath;
    }

    public static File getDatabaseFile() {
        return new File(getOrCreateDatabasePath());
    }

    public static void shutdown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}