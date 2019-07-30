package com.example.bookshelfdemo.book;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@ActiveProfiles("test")
class BookRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookRepository bookRepository;

    @Test
    void save_EntityPersisted() {
        // given
        var book = new Book(1L, "9781617290459", "A", "X", 12, 3);

        // when
        bookRepository.save(book);

        // then
        var persistedBook = entityManager.find(Book.class, 1L);
        assertThat(persistedBook).isNotNull();
        assertThat(persistedBook.getIsbn()).isEqualTo("9781617290459");
        assertThat(persistedBook.getTitle()).isEqualTo("A");
        assertThat(persistedBook.getAuthor()).isEqualTo("X");
        assertThat(persistedBook.getNumberOfPages()).isEqualTo(12);
        assertThat(persistedBook.getRating()).isEqualTo(3);
    }

    @Test
    void findAll_NoEntities_EmptyListReturned() {
        //when
        var result = bookRepository.findAll();

        //then
        assertThat(result).isEmpty();
    }

    @Test
    void findAll_OneEntity_EntityReturned() {
        // given
        var book = new Book("9781617290459", "A", "X", 12, 3);
        entityManager.persist(book);

        // when
        var result = bookRepository.findAll();

        // then
        assertThat(result).isNotEmpty().hasSize(1);
        var firstBook = result.get(0);
        assertThat(firstBook.getIsbn()).isEqualTo("9781617290459");
        assertThat(firstBook.getTitle()).isEqualTo("A");
        assertThat(firstBook.getAuthor()).isEqualTo("X");
        assertThat(firstBook.getNumberOfPages()).isEqualTo(12);
        assertThat(firstBook.getRating()).isEqualTo(3);
    }

    @Test
    void findAll_EntitiesReturned() {
        // given
        var book = new Book("9781617290459", "A", "X", 12, 3);
        book = entityManager.persist(book);
        var book2 = new Book("9783161484100", "B", "R", 111, 3);
        book2 = entityManager.persist(book2);

        // when
        var result = bookRepository.findAll();

        // then
        assertThat(result).isNotEmpty().hasSize(2);
        assertThat(result).containsExactly(book, book2);
    }

    @Test
    void findById_NoEntity_EmptyOptionalReturned() {
        // when
        var result = bookRepository.findById(5L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void findById_EntityReturned() {
        // given
        var book = new Book("9781617290459", "A", "X", 12, 3);
        book = entityManager.persist(book);

        // when
        var result = bookRepository.findById(book.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(book);
    }

    @Test
    void delete_EntityDeleted() {
        // given
        var book = new Book("9781617290459", "A", "X", 12, 3);
        book = entityManager.persist(book);

        // when
        bookRepository.delete(book);

        // then
        var result = entityManager.find(Book.class, book.getId());
        assertThat(result).isNull();
    }

}
