package com.example.librarydb.service;

import com.example.librarydb.entity.Book;
import com.example.librarydb.entity.Category;
import com.example.librarydb.repository.BookRepository;

import javax.persistence.EntityManagerFactory;
import java.util.List;
import java.util.Optional;

public class BookService {
    private final BookRepository repository;

    public BookService(EntityManagerFactory emf) {
        this.repository = new BookRepository(emf);
    }

    /** Create and persist a new Book */
    public Book create(String title, String author, Category category) {
        Book book = new Book(title, author, category);
        return repository.save(book);
    }

    /** Find a book by its database ID */
    public Optional<Book> findById(long id) {
        return repository.findById(id);
    }

    /** Return all books in the database */
    public List<Book> listAll() {
        return repository.findAll();
    }

    /** Update an existing book (must have a valid ID) */
    public Book update(Book book) {
        return repository.update(book);
    }

    /** Delete a book by ID */
    public void delete(long id) {
        repository.delete(id);
    }

    public List<Book> findByCategory(Category category) {
        return repository.findByCategory(category);
    }
}
