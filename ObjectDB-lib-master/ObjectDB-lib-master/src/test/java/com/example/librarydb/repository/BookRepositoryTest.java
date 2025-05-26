package com.example.librarydb.repository;

import com.example.librarydb.config.JPAConfig;
import com.example.librarydb.entity.Book;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BookRepositoryTest {

    private BookRepository repo;

    @BeforeAll
    void setupFactory() {
        // Ensure we start with a fresh DB file
        File db = new File("C:/Users/Mateu/Desktop/Semestr6/NBD/library-db/database/library.odb");
        if (db.exists()) db.delete();

        repo = new BookRepository(JPAConfig.getEntityManagerFactory());
    }

    @AfterAll
    void closeFactory() {
        JPAConfig.shutdown();
    }

    @Test
    void testSaveAndFind() {
        Book book = new Book("Dune", "Frank Herbert");
        repo.save(book);

        Optional<Book> found = repo.findById(book.getId());
        assertTrue(found.isPresent());
        assertEquals("Dune", found.get().getTitle());
        assertEquals("Frank Herbert", found.get().getAuthor());
    }

    @Test
    void testFindAll() {
        // clean up and add two books
        // (could also delete all first if needed)
        Book b1 = repo.save(new Book("1984", "Orwell"));
        Book b2 = repo.save(new Book("Brave New World", "Huxley"));

        List<Book> list = repo.findAll();
        assertTrue(list.size() >= 2);
        assertTrue(list.stream().anyMatch(b -> b.getTitle().equals("1984")));
        assertTrue(list.stream().anyMatch(b -> b.getTitle().equals("Brave New World")));
    }

    @Test
    void testUpdate() {
        Book b = repo.save(new Book("Old Title", "Author"));
        b.setTitle("New Title");
        repo.update(b);

        Optional<Book> updated = repo.findById(b.getId());
        assertTrue(updated.isPresent());
        assertEquals("New Title", updated.get().getTitle());
    }

    @Test
    void testDelete() {
        Book b = repo.save(new Book("To Delete", "Author"));
        long id = b.getId();
        repo.delete(id);

        Optional<Book> deleted = repo.findById(id);
        assertFalse(deleted.isPresent());
    }
}
