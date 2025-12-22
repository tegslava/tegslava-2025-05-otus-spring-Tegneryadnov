package ru.otus.hw.dto.mappers;

import org.mapstruct.Mapper;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.models.Book;

@Mapper(componentModel = "spring")
public interface BookMapper {
    BookDto toDto(Book book);

    Book toEntity(BookDto bookDto);
}
