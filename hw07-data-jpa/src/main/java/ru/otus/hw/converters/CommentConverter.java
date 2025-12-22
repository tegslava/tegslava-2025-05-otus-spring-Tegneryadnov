package ru.otus.hw.converters;

import org.springframework.stereotype.Component;
import ru.otus.hw.dto.CommentDto;

@Component
public class CommentConverter {
    public String dtoToString(CommentDto comment) {
        return "Id: %d, Text: %s".formatted(comment.id(), comment.text());
    }
}
