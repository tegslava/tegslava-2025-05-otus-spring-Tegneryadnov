package ru.otus.hw.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Author;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcAuthorRepository implements AuthorRepository {
    private static final String AUTHOR_ID = "id";

    private static final String AUTHOR_FULL_NAME = "full_name";

    private final NamedParameterJdbcOperations jdbc;

    @Override
    public List<Author> findAll() {
        return jdbc.query("select id, full_name from authors",
                new AuthorRowMapper());
    }

    @Override
    public Optional<Author> findById(long id) {
        var params = Collections.singletonMap(AUTHOR_ID, id);
        var sql = "select id, full_name from authors where id = :id";
        return Optional.ofNullable(jdbc.query(sql, params,
                new JdbcAuthorRepository.AuthorResultSetExtractor()));
    }

    private static class AuthorRowMapper implements RowMapper<Author> {

        @Override
        public Author mapRow(ResultSet rs, int i) throws SQLException {
            var id = rs.getLong(AUTHOR_ID);
            var name = rs.getString(AUTHOR_FULL_NAME);
            return new Author(id, name);
        }
    }

    @RequiredArgsConstructor
    private static class AuthorResultSetExtractor implements ResultSetExtractor<Author> {

        @Override
        public Author extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (!rs.isBeforeFirst()) {
                return null;
            }
            long authorId = 0;
            String authorFullName = null;
            while (rs.next()) {
                if (authorId == 0) {
                    authorId = rs.getLong(AUTHOR_ID);
                }
                if (authorFullName == null) {
                    authorFullName = rs.getString(AUTHOR_FULL_NAME);
                }
            }
            return new Author(authorId, authorFullName);
        }
    }
}
