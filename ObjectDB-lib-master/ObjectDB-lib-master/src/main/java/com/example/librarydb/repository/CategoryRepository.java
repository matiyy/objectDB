package com.example.librarydb.repository;

import com.example.librarydb.entity.Category;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.List;
import java.util.Optional;

public class CategoryRepository {
    private final EntityManagerFactory emf;

    public CategoryRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public Category save(Category category) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(category);
            em.getTransaction().commit();
            return category;
        } finally {
            em.close();
        }
    }

    public Optional<Category> findById(long id) {
        EntityManager em = emf.createEntityManager();
        try {
            Category category = em.find(Category.class, id);
            return Optional.ofNullable(category);
        } finally {
            em.close();
        }
    }

    public List<Category> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT c FROM Category c", Category.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Category update(Category category) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Category merged = em.merge(category);
            em.getTransaction().commit();
            return merged;
        } finally {
            em.close();
        }
    }

    public void delete(long id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Category category = em.find(Category.class, id);
            if (category != null) {
                // Remove category reference from associated books
                category.getBooks().forEach(book -> book.setCategory(null));
                em.remove(category);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public List<Category> findByName(String name) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(:name)",
                    Category.class)
                    .setParameter("name", "%" + name + "%")
                    .getResultList();
        } finally {
            em.close();
        }
    }
}