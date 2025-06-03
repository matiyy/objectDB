package com.example.librarydb.service;

import com.example.librarydb.config.JPAConfig;
import com.example.librarydb.entity.Book;
import com.example.librarydb.service.DataGenerator;
import com.example.librarydb.service.BookService;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DataGeneratorTest {

    private BookService bookService;
    private static final String DB_PATH =
            "C:/Users/Mateu/Desktop/Semestr6/NBD/library-db/database/library.odb";

    @BeforeAll
    void setup() {
        // Ensure a clean database file
        File db = new File(DB_PATH);
        if (db.exists()) db.delete();

        bookService = new BookService(JPAConfig.getEntityManagerFactory());
    }

    @AfterAll
    void teardown() {
        JPAConfig.shutdown();
    }

    @Test
    void generateCreatesExpectedCount() {
        // Generate 10 sample books
        DataGenerator.generateSampleData(10);

        List<Book> all = bookService.listAll();
        assertEquals(10, all.size(),
                "Expected 10 books after generation, but got " + all.size());
    }
}
