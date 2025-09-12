package ru.otus.hw.repositories;

import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DisplayName("Репозиторий на основе JPA для работы с жанрами книг ")
@DataJpaTest
@Import(JpaGenreRepository.class)
public class JpaGenreRepositoryTest {

    private static final long FIRST_GENRE_ID = 1L;
    private static final long THIRD_GENRE_ID = 3L;
    private static final long SIXTH_GENRE_ID = 6L;

    @Autowired
    private JpaGenreRepository genreRepository;

    @Autowired
    private TestEntityManager em;

    @DisplayName("должен выгружать информацию о жанрах по списку id")
    @Test
    void shouldFindExpectedGenresByIds() {
        val genresIds = Set.of(FIRST_GENRE_ID, THIRD_GENRE_ID, SIXTH_GENRE_ID);
        val optionalActualGenre = genreRepository.findAllByIds(genresIds);
        val expectedGenres = genresIds
                .stream()
                .map(id -> em.find(Genre.class, id))
                .toList();

        assertThat(optionalActualGenre)
                .containsExactlyInAnyOrderElementsOf(expectedGenres);
    }

    @DisplayName("должен выгружать все жанры книг")
    @Test
    void shouldReturnGenresList() {
        val expectedGenres = getDbGenres();
        val actualGenres = genreRepository.findAll();

        assertThat(actualGenres).containsExactlyElementsOf(expectedGenres);
    }

    private static List<Genre> getDbGenres() {
        return IntStream.range(1, 7).boxed()
                .map(id -> new Genre(id, "Genre_" + id))
                .toList();
    }
}
