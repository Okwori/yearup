create table quiz
(
    id     serial not null
        constraint quiz_pk
            primary key,
    "desc" varchar,
    status integer default 1
);

comment on column quiz.status is 'status of the quiz: ACTIVE = 1; INACTIVE = 2';

alter table quiz
    owner to postgres;

create unique index quiz_id_uindex
    on quiz (id);

INSERT INTO public.quiz (id, "desc")
VALUES (1, 'YU DS Program ');

