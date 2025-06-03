package com.example.librarydb.service;

import com.example.librarydb.entity.Category;
import com.example.librarydb.repository.CategoryRepository;

import javax.persistence.EntityManagerFactory;
import java.util.List;
import java.util.Optional;

public class CategoryService {
    private final CategoryRepository repository;

    public CategoryService(EntityManagerFactory emf) {
        this.repository = new CategoryRepository(emf);
    }

    /** Create and persist a new Category */
    public Category create(String name, String description) {
        Category category = new Category(name, description);
        return repository.save(category);
    }

    /** Find a category by its database ID */
    public Optional<Category> findById(long id) {
        return repository.findById(id);
    }

    /** Return all categories in the database */
    public List<Category> findAll() {
        return repository.findAll();
    }

    /** Return all categories in the database (alternative method name) */
    public List<Category> listAll() {
        return repository.findAll();
    }

    /** Update an existing category (must have a valid ID) */
    public Category update(Category category) {
        return repository.update(category);
    }

    /** Delete a category by ID */
    public void delete(long id) {
        repository.delete(id);
    }

    /** Search categories by name (case-insensitive partial match) */
    public List<Category> searchByName(String name) {
        return repository.findByName(name);
    }

    /** Get the repository instance */
    public CategoryRepository getRepository() {
        return repository;
    }
}