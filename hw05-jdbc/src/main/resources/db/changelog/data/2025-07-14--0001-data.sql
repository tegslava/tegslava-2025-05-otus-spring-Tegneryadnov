--liquibase formatted sql

--changeset tegslava:2025-07-14--0001-authors
insert into authors(full_name)
values ('Author_1'), ('Author_2'), ('Author_3');

--changeset tegslava:2025-07-14--0001-genres
insert into genres(name)
values ('Genre_1'), ('Genre_2'), ('Genre_3'),
       ('Genre_4'), ('Genre_5'), ('Genre_6');

--changeset tegslava:2025-07-14--0001-books
insert into books(title, author_id)
values ('BookTitle_1', 1), ('BookTitle_2', 2), ('BookTitle_3', 3);

--changeset tegslava:2025-07-14--0001-books_genres
insert into books_genres(book_id, genre_id)
values (1, 1),   (1, 2),
       (2, 3),   (2, 4),
       (3, 5),   (3, 6);
