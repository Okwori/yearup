create table answer
(
    id        serial  not null
        constraint answers_pk
            primary key,
    user_id   integer,
    option_id integer not null
);

-- alter table answer owner to postgres;

create unique index answers_id_uindex
    on answer (id);

