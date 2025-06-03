package com.example.librarydb.config;

import static org.junit.jupiter.api.Assertions.*;

import javax.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;

public class JPAConfigTest {

    @Test
    public void testEntityManagerFactoryInitialization() {
        EntityManagerFactory emf = JPAConfig.getEntityManagerFactory();
        assertNotNull(emf, "EntityManagerFactory should be initialized");
        assertTrue(emf.isOpen(), "EntityManagerFactory should be open");
        JPAConfig.shutdown();
        assertFalse(emf.isOpen(), "EntityManagerFactory should be closed after shutdown");
    }
}