package ru.otus.hw.dto.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.models.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(target = "bookId", source = "book.id")
    CommentDto toDto(Comment comment);

    @Mapping(target = "book.id", source = "bookId")
    Comment toEntity(CommentDto commentDto);
}
