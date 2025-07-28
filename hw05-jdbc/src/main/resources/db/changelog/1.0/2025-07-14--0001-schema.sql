--liquibase formatted sql


--changeset tegslava:2025-07-14--0001-authors
create table authors
(
    id        bigserial,
    full_name varchar(255),
    primary key (id)
);

--changeset tegslava:2025-07-14--0001-genres
create table genres
(
    id   bigserial,
    name varchar(255),
    primary key (id)
);

--changeset tegslava:2025-07-14--0001-books
create table books
(
    id        bigserial,
    title     varchar(255),
    author_id bigint references authors (id) on delete cascade,
    primary key (id)
);

--changeset tegslava:2025-07-14--0001-books_genres
create table books_genres
(
    book_id  bigint references books (id) on delete cascade,
    genre_id bigint references genres (id) on delete cascade,
    primary key (book_id, genre_id)
);