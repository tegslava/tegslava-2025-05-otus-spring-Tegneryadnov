package ru.otus.hw.services;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.exceptions.EntityNotFoundException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@DisplayName("Сервис для работы с книгами")
@SpringBootTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class BookServiceImplTest {

    private static final long FIRST_BOOK_ID = 1L;
    private static final long FOURTH_BOOK_ID = 4L;
    private static final long INCORRECT_BOOK_ID = 777L;
    private static final int SECOND_AUTHOR = 1;
    private static final int FIRST_GENRE = 0;
    private static final int THIRD_GENRE = 2;
    private static final int SIXTH_GENRE = 5;

    @Autowired
    private BookService bookService;

    private List<AuthorDto> dbAuthors;

    private List<GenreDto> dbGenres;

    private List<BookDto> dbBooks;

    @BeforeEach
    void setUp() {
        dbAuthors = getDbAuthors();
        dbGenres = getDbGenres();
        dbBooks = getDbBooks(dbAuthors, dbGenres);
    }

    @DisplayName("должен возвращать информацию о книге по id")
    @ParameterizedTest
    @MethodSource("getDbBooks")
    void shouldFindBookById(BookDto expectedBook) {
        val actualBook = bookService.findById(expectedBook.id());

        assertThat(actualBook)
                .isPresent()
                .get()
                .usingRecursiveComparison()
                .isEqualTo(expectedBook);
    }

    @DisplayName("должен возвращать ошибку при поиске несуществующей книги по id")
    @Test
    void shouldFailFindBookById() {
        val actualBook = bookService.findById(INCORRECT_BOOK_ID);

        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> actualBook
                        .orElseThrow(() ->
                                new EntityNotFoundException("Book with id %d not found".formatted(INCORRECT_BOOK_ID))));
    }

    @DisplayName("должен возвращать информацию о всех книгах")
    @Test
    void shouldFindAllBooks() {
        val actualBooks = bookService.findAll();
        val expectedBooks = dbBooks;

        assertThat(actualBooks)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrderElementsOf(expectedBooks);
    }

    @DisplayName("должен сохранять новую книгу")
    @Test
    @DirtiesContext
    void shouldSaveNewBook() {
        val expectedBook = new BookDto(
                FOURTH_BOOK_ID,
                "The Mysterious Island",
                dbAuthors.get(SECOND_AUTHOR),
                List.of(dbGenres.get(FIRST_GENRE), dbGenres.get(THIRD_GENRE))
        );
        val genresIds = expectedBook.genres()
                .stream().map(GenreDto::id).collect(Collectors.toSet());
        val actualBook = bookService.insert(expectedBook.title(),
                expectedBook.author().id(), genresIds);

        assertThat(actualBook)
                .isNotNull()
                .matches(book -> book.id() > 0)
                .usingRecursiveComparison()
                .isEqualTo(expectedBook);

        assertThat(bookService.findById(actualBook.id()))
                .isPresent()
                .get()
                .isEqualTo(actualBook);
    }


    @DisplayName("должен сохранять книгу после внесения изменений")
    @Test
    @DirtiesContext
    void shouldSaveUpdatedBook() {
        val expectedBook = new BookDto(
                FIRST_BOOK_ID,
                "The Jungle Book",
                dbAuthors.get(SECOND_AUTHOR),
                List.of(dbGenres.get(THIRD_GENRE), dbGenres.get(SIXTH_GENRE))
        );

        assertThat(bookService.findById(expectedBook.id()))
                .isPresent()
                .get()
                .usingRecursiveComparison()
                .isNotEqualTo(expectedBook);

        val genresIds = expectedBook.genres()
                .stream().map(GenreDto::id).collect(Collectors.toSet());
        val actualBook = bookService.update(expectedBook.id(), expectedBook.title(),
                expectedBook.author().id(), genresIds);

        assertThat(actualBook)
                .isNotNull()
                .matches(book -> book.id() > 0)
                .usingRecursiveComparison()
                .isEqualTo(expectedBook);

        assertThat(bookService.findById(actualBook.id()))
                .isPresent()
                .get()
                .isEqualTo(actualBook);
    }

    @DisplayName("должен удалять книгу по id")
    @Test
    @DirtiesContext
    void shouldDeleteBookById() {
        val actualBook = bookService.findById(FIRST_BOOK_ID);
        assertThat(actualBook).isPresent();
        bookService.deleteById(FIRST_BOOK_ID);
        assertThat(bookService.findById(FIRST_BOOK_ID)).isNotPresent();
    }

    private static List<AuthorDto> getDbAuthors() {
        return IntStream.range(1, 4).boxed()
                .map(id -> new AuthorDto(id, "Author_" + id))
                .toList();
    }

    private static List<GenreDto> getDbGenres() {
        return IntStream.range(1, 7).boxed()
                .map(id -> new GenreDto(id, "Genre_" + id))
                .toList();
    }

    private static List<BookDto> getDbBooks(List<AuthorDto> dbAuthors, List<GenreDto> dbGenres) {
        return IntStream.range(1, 4).boxed()
                .map(id -> new BookDto(id,
                        "BookTitle_" + id,
                        dbAuthors.get(id - 1),
                        dbGenres.subList((id - 1) * 2, (id - 1) * 2 + 2)
                ))
                .toList();
    }

    private static List<BookDto> getDbBooks() {
        var dbAuthors = getDbAuthors();
        var dbGenres = getDbGenres();
        return getDbBooks(dbAuthors, dbGenres);
    }
}
