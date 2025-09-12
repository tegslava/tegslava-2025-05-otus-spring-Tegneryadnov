package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.dto.CommentDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Сервис для работы с комментариями к книгам")
@SpringBootTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class CommentsServiceTest {

    private static final long FIRST_BOOK_ID = 1L;
    private static final long FIRST_COMMENT_ID = 1L;
    private static final long SECOND_COMMENT_ID = 2L;
    private static final long THIRD_COMMENT_ID = 3L;
    private static final long FOURTH_COMMENT_ID = 4L;
    private static final String NEW_COMMENT_TEXT = "Sharman!";
    private static final String FIRST_COMMENT_TEXT = "Comment 1";
    private static final String SECOND_COMMENT_TEXT = "Comment 2";
    private static final String THIRD_COMMENT_TEXT = "Comment 3";

    @Autowired
    private CommentService commentService;

    @DisplayName("должен возвращать комментарий по id")
    @Test
    void shouldFindCommentById() {
        var expectedComment = new CommentDto(FIRST_COMMENT_ID, FIRST_BOOK_ID, FIRST_COMMENT_TEXT);
        var comment = commentService.findById(FIRST_COMMENT_ID);

        assertThat(comment)
                .isPresent()
                .get()
                .isEqualTo(expectedComment);
    }

    @DisplayName("должен возвращать все комментарии книги")
    @Test
    void shouldFindAllBookComments() {
        var expectedComments = List.of(
                new CommentDto(FIRST_COMMENT_ID, FIRST_BOOK_ID, FIRST_COMMENT_TEXT),
                new CommentDto(SECOND_COMMENT_ID, FIRST_BOOK_ID, SECOND_COMMENT_TEXT),
                new CommentDto(THIRD_COMMENT_ID, FIRST_BOOK_ID, THIRD_COMMENT_TEXT)
        );
        var actualComments = commentService.findByBookId(FIRST_BOOK_ID);

        assertThat(actualComments).containsExactlyInAnyOrderElementsOf(expectedComments);
    }

    @DisplayName("должен сохранять новый комментарий")
    @DirtiesContext
    @Test
    void shouldSaveNewComment() {
        var expectedComment = new CommentDto(FOURTH_COMMENT_ID, FIRST_BOOK_ID, NEW_COMMENT_TEXT);
        var actualComment = commentService.insert(FIRST_BOOK_ID, NEW_COMMENT_TEXT);

        assertThat(actualComment).isEqualTo(expectedComment);
    }

    @DisplayName("должен обновлять текст комментария")
    @DirtiesContext
    @Test
    void shouldUpdateCommentText() {
        var expectedComment = new CommentDto(FIRST_COMMENT_ID, FIRST_BOOK_ID, NEW_COMMENT_TEXT);
        var actualComment = commentService.update(FIRST_COMMENT_ID, FIRST_BOOK_ID, NEW_COMMENT_TEXT);

        assertThat(actualComment).isEqualTo(expectedComment);
    }

    @DisplayName("должен удалять комментарий по id")
    @DirtiesContext
    @Test
    void deleteCommentById() {
        commentService.deleteById(FIRST_COMMENT_ID);
        var actualComment = commentService.findById(FIRST_COMMENT_ID);
        assertThat(actualComment).isEmpty();
    }
}
