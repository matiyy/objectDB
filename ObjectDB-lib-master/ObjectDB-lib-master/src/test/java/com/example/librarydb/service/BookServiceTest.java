package com.example.librarydb.service;

import com.example.librarydb.config.JPAConfig;
import com.example.librarydb.entity.Book;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BookServiceTest {

    private BookService service;

    @BeforeAll
    void setup() {
        // Ensure fresh DB
        File db = new File("C:/Users/Mateu/Desktop/Semestr6/NBD/library-db/database/library.odb");
        if (db.exists()) db.delete();

        service = new BookService(JPAConfig.getEntityManagerFactory());
    }

    @AfterAll
    void teardown() {
        JPAConfig.shutdown();
    }

    @Test
    void createAndFind() {
        Book created = service.create("Sapiens", "Yuval Noah Harari");
        assertNotNull(created.getId());

        Optional<Book> found = service.findById(created.getId());
        assertTrue(found.isPresent());
        assertEquals("Sapiens", found.get().getTitle());
    }

    @Test
    void listAllReturnsMultiple() {
        service.create("Book A", "Author A");
        service.create("Book B", "Author B");

        List<Book> all = service.listAll();
        assertTrue(all.size() >= 2);
    }

    @Test
    void updateReflectsChanges() {
        Book b = service.create("Temp", "Auth");
        b.setAuthor("NewAuth");
        Book updated = service.update(b);

        Optional<Book> fetched = service.findById(updated.getId());
        assertTrue(fetched.isPresent());
        assertEquals("NewAuth", fetched.get().getAuthor());
    }

    @Test
    void deleteRemovesBook() {
        Book b = service.create("ToBeDel", "X");
        long id = b.getId();
        service.delete(id);

        Optional<Book> gone = service.findById(id);
        assertFalse(gone.isPresent());
    }
}
