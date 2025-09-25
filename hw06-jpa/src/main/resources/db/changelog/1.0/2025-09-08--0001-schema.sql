create table comments
(
    id      bigserial,
    book_id bigint references books (id) on delete cascade,
    text    text,
    primary key (id)
);