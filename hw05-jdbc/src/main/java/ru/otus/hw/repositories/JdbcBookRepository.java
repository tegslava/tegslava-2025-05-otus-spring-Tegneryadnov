package ru.otus.hw.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.util.Pair;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Collections;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;

import static org.springframework.data.util.Pair.toMap;

@Repository
@RequiredArgsConstructor
public class JdbcBookRepository implements BookRepository {

    private static final String BOOK_ID = "book_id";

    private static final String BOOK_TITLE = "book_title";

    private static final String AUTHOR_ID = "author_id";

    private static final String AUTHOR_FULL_NAME = "author_full_name";

    private static final String GENRE_ID = "genre_id";

    private static final String GENRE_NAME = "genre_name";

    private final GenreRepository genreRepository;

    private final NamedParameterJdbcOperations jdbc;

    @Override
    public Optional<Book> findById(long id) {
        try {
            var params = Collections.singletonMap(BOOK_ID, id);
            var sql = """
                    select b.id as book_id,
                           b.title as book_title,
                           b.author_id,
                           a.full_name as author_full_name,
                           bg.genre_id,
                           g.name as genre_name
                     from books  b
                    left outer join authors a on a.id = b.author_id
                    left outer join books_genres bg on bg.book_id = b.id
                    left outer join genres g on g.id = bg.genre_id
                    where b.id = :book_id
                    """;
            return Optional.ofNullable(jdbc.query(sql, params, new BookResultSetExtractor()));
        } catch (EntityNotFoundException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Book> findAll() {
        var genres = genreRepository.findAll();
        var relations = getAllGenreRelations();
        var books = getAllBooksWithoutGenres();
        mergeBooksInfo(books, genres, relations);
        return books;
    }

    @Override
    public Book save(Book book) {
        if (book.getId() == 0) {
            return insert(book);
        }
        return update(book);
    }

    @Override
    public void deleteById(long id) {
        var params = Collections.singletonMap(BOOK_ID, id);
        jdbc.update("delete from books where id = :book_id", params);
    }

    private List<Book> getAllBooksWithoutGenres() {
        return jdbc.query("""
                        select b.id as book_id,
                               b.title as book_title,
                               b.author_id,
                               a.full_name as author_full_name
                         from books b
                          left outer join authors a on b.author_id = a.id
                        """,
                new JdbcBookRepository.BookRowMapper());
    }

    private List<BookGenreRelation> getAllGenreRelations() {
        return jdbc.query("select book_id, genre_id from books_genres",
                new BookGenreRelationRowMapper());
    }

    private void mergeBooksInfo(List<Book> booksWithoutGenres, List<Genre> genres,
                                List<BookGenreRelation> relations) {

        var genresByGenreId = genres.stream()
                .map(genre -> Pair.of(genre.getId(), genre))
                .collect(toMap());

        var genresByBookId = relations.stream().collect(
                Collectors.groupingBy(
                        relation -> relation.bookId,
                        Collectors.mapping(
                                relation -> genresByGenreId.get(relation.genreId),
                                Collectors.toList()
                        )
                )
        );

        for (var book : booksWithoutGenres) {
            var bookGenres = genresByBookId.getOrDefault(book.getId(), Collections.emptyList());
            book.setGenres(bookGenres);
        }
    }

    private Book insert(Book book) {
        var keyHolder = new GeneratedKeyHolder();
        var params = new MapSqlParameterSource(
                Map.of(AUTHOR_ID, book.getAuthor().getId(), BOOK_TITLE, book.getTitle())
        );
        jdbc.update("insert into books (title, author_id) values (:book_title, :author_id)",
                params, keyHolder);
        Optional.ofNullable(keyHolder.getKeyAs(Long.class)).ifPresent(book::setId);
        batchInsertGenresRelationsFor(book);
        return book;
    }

    private Book update(Book book) {
        var params = new MapSqlParameterSource(Map.of(AUTHOR_ID, book.getAuthor().getId(),
                BOOK_TITLE, book.getTitle(), BOOK_ID, book.getId()));
        var updatedRowsCount = jdbc.update("""
                update books b
                   set b.title = :book_title,
                   b.author_id = :author_id
                  where b.id = :book_id
                """, params);

        if (updatedRowsCount == 0) {
            var message = "Book with id %d is not found for update".formatted(book.getId());
            throw new EntityNotFoundException(message);
        }
        removeGenresRelationsFor(book);
        batchInsertGenresRelationsFor(book);

        return book;
    }

    private void batchInsertGenresRelationsFor(Book book) {
        var params = book.getGenres().stream().map(genre ->
                new MapSqlParameterSource(Map.of(BOOK_ID, book.getId(), GENRE_ID, genre.getId())));
        jdbc.batchUpdate("""
                insert into books_genres(book_id, genre_id)
                 values(:book_id, :genre_id)
                """, params.toArray(MapSqlParameterSource[]::new));
    }

    private void removeGenresRelationsFor(Book book) {
        var params = Collections.singletonMap(BOOK_ID, book.getId());
        jdbc.update("delete from books_genres where book_id = :book_id", params);
    }

    private static class BookRowMapper implements RowMapper<Book> {

        @Override
        public Book mapRow(ResultSet rs, int rowNum) throws SQLException {
            var id = rs.getLong(BOOK_ID);
            var title = rs.getString(BOOK_TITLE);
            var authorId = rs.getLong(AUTHOR_ID);
            var authorFullName = rs.getString(AUTHOR_FULL_NAME);
            var author = new Author(authorId, authorFullName);
            var genres = Collections.<Genre>emptyList();
            return new Book(id, title, author, genres);
        }
    }

    @RequiredArgsConstructor
    private static class BookResultSetExtractor implements ResultSetExtractor<Book> {

        @Override
        public Book extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (!rs.isBeforeFirst()) {
                return null;
            }
            long bookId = 0;
            String bookTitle = null;
            Author author = null;
            List<Genre> genres = new ArrayList<>();
            while (rs.next()) {
                if (bookId == 0) {
                    bookId = rs.getLong(BOOK_ID);
                }
                if (bookTitle == null) {
                    bookTitle = rs.getString(BOOK_TITLE);
                }
                if (author == null) {
                    author = new Author(rs.getLong(AUTHOR_ID), rs.getString(AUTHOR_FULL_NAME));
                }
                genres.add(new Genre(rs.getLong(GENRE_ID), rs.getString(GENRE_NAME)));
            }
            return new Book(bookId, bookTitle, author, genres);
        }
    }

    private record BookGenreRelation(long bookId, long genreId) {
    }

    private static class BookGenreRelationRowMapper implements RowMapper<BookGenreRelation> {

        @Override
        public BookGenreRelation mapRow(ResultSet rs, int rowNum) throws SQLException {
            var bookId = rs.getLong(BOOK_ID);
            var genreId = rs.getLong(GENRE_ID);
            return new BookGenreRelation(bookId, genreId);
        }
    }
}
