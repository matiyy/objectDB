package com.example.librarydb.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Book {
    @Id
    @GeneratedValue
    private Long id;

    private String title;
    private String author;

    @ManyToOne
    private Category category;

    // Constructors
    public Book() {}
    public Book(String title, String author) {
        this.title = title;
        this.author = author;
        this.category = null;
    }
    public Book(String title, String author, Category category) {
        this.title = title;
        this.author = author;
        this.category = category;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public void setId(Long id) {
        this.id = id;
    }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
}