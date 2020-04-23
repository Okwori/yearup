create table question
(
    id       serial not null
        constraint question_pk
            primary key,
    question varchar,
    sub_note varchar
);

-- alter table question owner to postgres;

create unique index question_id_uindex
    on question (id);

