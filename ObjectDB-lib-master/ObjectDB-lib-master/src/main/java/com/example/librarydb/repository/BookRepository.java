package com.example.librarydb.repository;

import com.example.librarydb.entity.Book;
import com.example.librarydb.entity.Category;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.List;
import java.util.Optional;

public class BookRepository {

    private final EntityManagerFactory emf;

    public BookRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public Book save(Book book) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(book);
        em.getTransaction().commit();
        em.close();
        return book;
    }

    public Optional<Book> findById(long id) {
        EntityManager em = emf.createEntityManager();
        Book book = em.find(Book.class, id);
        em.close();
        return Optional.ofNullable(book);
    }

    public List<Book> findAll() {
        EntityManager em = emf.createEntityManager();
        List<Book> books = em.createQuery("SELECT b FROM Book b", Book.class)
                .getResultList();
        em.close();
        return books;
    }

    public Book update(Book book) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Book merged = em.merge(book);
        em.getTransaction().commit();
        em.close();
        return merged;
    }

    public void delete(long id) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Book book = em.find(Book.class, id);
        if (book != null) {
            em.remove(book);
        }
        em.getTransaction().commit();
        em.close();
    }

    public List<Book> findByCategory(Category category) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                "SELECT b FROM Book b WHERE b.category = :category", 
                Book.class)
                .setParameter("category", category)
                .getResultList();
        } finally {
            em.close();
        }
    }
}
