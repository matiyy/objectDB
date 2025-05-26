package com.example.librarydb.service;

import com.example.librarydb.config.JPAConfig;
import com.example.librarydb.entity.Book;
import com.example.librarydb.entity.Category;

import javax.persistence.EntityManagerFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Generates a number of sample Book records in the database.
 */
public class DataGenerator {
    private final BookService bookService;
    private final CategoryService categoryService;
    private final Random random = new Random();
    private List<String> titles;
    private List<String> authors;

    public DataGenerator(EntityManagerFactory emf) {
        this.bookService = new BookService(emf);
        this.categoryService = new CategoryService(emf);
        loadData();
    }

    private void loadData() {
        try {
            // Get path to the data file in the same directory
            Path dataPath = Paths.get("book_data.txt");
            List<String> lines = Files.readAllLines(dataPath);

            // Rest of loading code remains the same...
            titles = lines.stream()
                    .skip(1)
                    .takeWhile(line -> !line.startsWith("AUTHORS:"))
                    .collect(Collectors.toList());

            authors = lines.stream()
                    .dropWhile(line -> !line.startsWith("AUTHORS:"))
                    .skip(1)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load book data from: " +
                    Paths.get("").toAbsolutePath() + "/book_data.txt", e);
        }
    }

    public void generateCategories() {
        categoryService.create("Fiction", "Fiction books");
        categoryService.create("Non-fiction", "Non-fiction books");
        categoryService.create("Science", "Scientific literature");
        categoryService.create("History", "Historical books");
    }

    public void generate(int count) {
    // Generate categories first
    generateCategories();
    List<Category> categories = categoryService.listAll();

    // Then generate books with random categories
    for (int i = 0; i < count; i++) {
        String title = titles.get(random.nextInt(titles.size()));
        String author = authors.get(random.nextInt(authors.size()));
        Category category = categories.get(random.nextInt(categories.size()));
        // Create book with category directly
        Book book = bookService.create(title, author, category);
    }
}
}
