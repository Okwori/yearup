create table if not exists city
(
    id   serial  not null
        constraint city_pk
            primary key,
    name varchar not null
);

-- alter table city owner to postgres;

create unique index if not exists city_id_uindex
    on city (id);

create unique index if not exists city_name_uindex
    on city (name);

INSERT INTO public.city (id, name)
VALUES (1, 'Los Angeles');
INSERT INTO public.city (id, name)
VALUES (2, 'New York');
INSERT INTO public.city (id, name)
VALUES (3, 'Charlotte');