package ru.otus.hw.repositories;

import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе JPA для работы с комментариями")
@DataJpaTest
@Import(JpaCommentsRepository.class)
public class JpaCommentsRepositoryTest {

    private static final long TRANSIENT_COMMENT_ID = 0;
    private static final long FIRST_COMMENT_ID = 1L;
    private static final long SECOND_COMMENT_ID = 2L;
    private static final long THIRD_COMMENT_ID = 3L;
    private static final long FIRST_BOOK_ID = 1L;
    private static final long SECOND_BOOK_ID = 2L;

    @Autowired
    private JpaCommentsRepository commentsRepository;

    @Autowired
    private TestEntityManager em;

    @DisplayName("должен возвращать информацию о комментарий по id")
    @Test
    void shouldFindCommentById() {
        val optionalActualComment = commentsRepository.findById(FIRST_COMMENT_ID);
        val expectedComment = em.find(Comment.class, FIRST_COMMENT_ID);

        assertThat(optionalActualComment).isPresent()
                .get()
                .isEqualTo(expectedComment);
    }

    @DisplayName("должен возвращать все комментарии по id книги")
    @Test
    void shouldFindAllCommentsByBookId() {
        val book = em.find(Book.class, FIRST_BOOK_ID);
        val actualComments = commentsRepository.findByBookId(FIRST_BOOK_ID);
        val expectedComments = List.of(
                new Comment(FIRST_COMMENT_ID, "Comment 1", book),
                new Comment(SECOND_COMMENT_ID, "Comment 2", book),
                new Comment(THIRD_COMMENT_ID, "Comment 3", book)
        );

        assertThat(actualComments)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrderElementsOf(expectedComments);
    }

    @DisplayName("должен сохранять новый комментарий к книге")
    @Test
    void shouldSaveNewComment() {
        val book = em.find(Book.class, SECOND_BOOK_ID);
        val expectedComment = new Comment(TRANSIENT_COMMENT_ID, "Comment 1 for book 2", book);
        commentsRepository.save(expectedComment);
        var actualComment = em.find(Comment.class, expectedComment.getId());

        assertThat(actualComment).isNotNull();
    }

    @DisplayName("должен сохранять измененный комментарий к книге")
    @Test
    void shouldSaveUpdatedComment() {
        val expectedComment = em.find(Comment.class, FIRST_COMMENT_ID);
        expectedComment.setText(expectedComment.getText() + " updated");
        commentsRepository.save(expectedComment);
        val actualComment = em.find(Comment.class, expectedComment.getId());

        assertThat(actualComment.getText()).isEqualTo("Comment 1 updated");
    }

    @DisplayName("должен удалять комментарий по id")
    @Test
    void shouldDeleteCommentById() {
        var expectedComment = em.find(Comment.class, FIRST_COMMENT_ID);
        assertThat(expectedComment).isNotNull();
        em.detach(expectedComment);
        commentsRepository.deleteById(FIRST_COMMENT_ID);

        assertThat(em.find(Comment.class, FIRST_COMMENT_ID)).isNull();
    }
}
