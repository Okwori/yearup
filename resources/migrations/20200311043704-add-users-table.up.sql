create table users
(
    id         serial  not null
        constraint users_pk
            primary key,
    first_name varchar,
    last_name  varchar,
    email      varchar not null,
    pass       varchar
);

-- alter table users owner to postgres;

