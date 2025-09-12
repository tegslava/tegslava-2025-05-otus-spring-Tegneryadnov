package ru.otus.hw.repositories;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе JPA для работы с книгами")
@DataJpaTest
@Import(JpaBookRepository.class)
public class JpaBookRepositoryTest {

    private static final long TRANSIENT_BOOK_ID = 0;
    private static final long FIRST_BOOK_ID = 1L;
    private static final int SECOND_AUTHOR = 1;
    private static final int FIRST_GENRE = 0;
    private static final int THIRD_GENRE = 2;
    private static final int SIXTH_GENRE = 5;

    @Autowired
    private JpaBookRepository bookRepository;

    @Autowired
    private TestEntityManager em;

    private List<Author> dbAuthors;

    private List<Genre> dbGenres;

    private List<Book> dbBooks;

    @BeforeEach
    void setUp() {
        dbAuthors = getDbAuthors();
        dbGenres = getDbGenres();
        dbBooks = getDbBooks(dbAuthors, dbGenres);
    }

    @DisplayName("должен возвращать информацию о книге по id")
    @ParameterizedTest
    @MethodSource("getDbBooks")
    void shouldReturnCorrectBookById(Book expectedBook) {
        val actualBook = bookRepository.findById(expectedBook.getId());

        assertThat(actualBook).isPresent()
                .get()
                .isEqualTo(expectedBook);
    }

    @DisplayName("должен возвращать информацию по всем книгам")
    @Test
    void shouldFindAllBooks() {
        val actualBooks = bookRepository.findAll();
        val expectedBooks = dbBooks;

        assertThat(actualBooks)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrderElementsOf(expectedBooks);
    }

    @DisplayName("должен сохранять новую книгу")
    @Test
    void shouldSaveNewBook() {
        val expectedBook = new Book(TRANSIENT_BOOK_ID, "The Mysterious Island",
                dbAuthors.get(SECOND_AUTHOR),
                List.of(dbGenres.get(FIRST_GENRE), dbGenres.get(THIRD_GENRE)));
        val actualBook = bookRepository.save(expectedBook);

        assertThat(actualBook).isNotNull()
                .matches(book -> book.getId() > 0)
                .usingRecursiveComparison().ignoringExpectedNullFields().isEqualTo(expectedBook);

        assertThat(bookRepository.findById(actualBook.getId()))
                .isPresent()
                .get()
                .isEqualTo(actualBook);
    }

    @DisplayName("должен сохранять измененную книгу")
    @Test
    void shouldSaveUpdatedBook() {
        val expectedBook = new Book(FIRST_BOOK_ID, "The Jungle Book",
                dbAuthors.get(SECOND_AUTHOR),
                List.of(dbGenres.get(THIRD_GENRE), dbGenres.get(SIXTH_GENRE)));
        bookRepository.save(expectedBook);
        val actualBook = em.find(Book.class, expectedBook.getId());

        assertThat(actualBook)
                .isNotNull()
                .matches(book -> book.getId() > 0)
                .usingRecursiveComparison()
                .ignoringExpectedNullFields()
                .isEqualTo(expectedBook);

        assertThat(bookRepository.findById(actualBook.getId()))
                .isPresent()
                .get()
                .isEqualTo(expectedBook);
    }

    @DisplayName("должен удалять книгу по id")
    @Test
    void shouldDeleteBookById() {
        val book = em.find(Book.class, FIRST_BOOK_ID);
        assertThat(book).isNotNull();
        em.detach(book);
        bookRepository.deleteById(FIRST_BOOK_ID);

        assertThat(em.find(Book.class, FIRST_BOOK_ID)).isNull();
    }


    private static List<Author> getDbAuthors() {
        return IntStream.range(1, 4).boxed()
                .map(id -> new Author(id, "Author_" + id))
                .toList();
    }

    private static List<Genre> getDbGenres() {
        return IntStream.range(1, 7).boxed()
                .map(id -> new Genre(id, "Genre_" + id))
                .toList();
    }

    private static List<Book> getDbBooks(List<Author> dbAuthors, List<Genre> dbGenres) {
        return IntStream.range(1, 4).boxed()
                .map(id -> new Book(id,
                        "BookTitle_" + id,
                        dbAuthors.get(id - 1),
                        dbGenres.subList((id - 1) * 2, (id - 1) * 2 + 2)
                ))
                .toList();
    }

    private static List<Book> getDbBooks() {
        val dbAuthors = getDbAuthors();
        val dbGenres = getDbGenres();
        return getDbBooks(dbAuthors, dbGenres);
    }
}
