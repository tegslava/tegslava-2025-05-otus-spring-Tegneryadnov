package ru.otus.hw.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcAuthorRepository implements AuthorRepository {

    private final NamedParameterJdbcOperations jdbc;

    @Override
    public List<Author> findAll() {
        return jdbc.query("select id, full_name from authors",
                new AuthorRowMapper());
    }

    @Override
    public Optional<Author> findById(long id) {
        try {
            var params = Collections.singletonMap("id", id);
            var author = jdbc.queryForObject(
                    "select id, full_name from authors where id = :id",
                    params, new AuthorRowMapper()
            );
            return Optional.ofNullable(author);
        } catch (EntityNotFoundException e) {
            return Optional.empty();
        }
    }

    private static class AuthorRowMapper implements RowMapper<Author> {

        @Override
        public Author mapRow(ResultSet rs, int i) throws SQLException {
            var id = rs.getLong("id");
            var name = rs.getString("full_name");
            return new Author(id, name);
        }
    }
}
