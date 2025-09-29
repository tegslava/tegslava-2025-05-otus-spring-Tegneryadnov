package ru.otus.hw.converters;

import org.springframework.stereotype.Component;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.models.Comment;

@Component
public class CommentConverter {
    public String dtoToString(CommentDto comment) {
        return "Id: %d, Text: %s".formatted(comment.id(), comment.text());
    }

    public CommentDto commentToDto(Comment comment) {
        return new CommentDto(comment.getId(), comment.getBook().getId(), comment.getText());
    }
}
