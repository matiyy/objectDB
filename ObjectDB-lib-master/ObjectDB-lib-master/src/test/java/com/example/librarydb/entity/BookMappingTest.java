package com.example.librarydb.entity;

import static org.junit.jupiter.api.Assertions.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import com.example.librarydb.config.JPAConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class BookMappingTest {
    private static EntityManagerFactory emf;

    @BeforeAll
    public static void setUp() {
        emf = JPAConfig.getEntityManagerFactory();
    }

    @AfterAll
    public static void tearDown() {
        JPAConfig.shutdown();
    }

    @Test
    public void testPersistAndFindBook() {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Book book = new Book("Test Title", "Test Author");
        em.persist(book);
        em.getTransaction().commit();

        Long id = book.getId();
        assertNotNull(id, "Persisted book should have an ID");

        Book found = em.find(Book.class, id);
        assertEquals("Test Title", found.getTitle());
        assertEquals("Test Author", found.getAuthor());

        em.close();
    }
}