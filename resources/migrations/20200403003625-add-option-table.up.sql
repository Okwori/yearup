create table option
(
    id          serial  not null
        constraint option_pk
            primary key,
    name        varchar not null,
    question_id integer not null,
    sentiment   integer
);

comment on column option.sentiment is 'defines the weighted sentiment attached to the option
i.e FALSE = 0;
TRUE and NOT SURE = 1';

-- alter table option owner to postgres;

INSERT INTO public.option (id, name, question_id, sentiment)
VALUES (1, 'Select the CITY closest to you', 1, null);
INSERT INTO public.option (id, name, question_id, sentiment)
VALUES (2, 'START', 2, null);
INSERT INTO public.option (id, name, question_id, sentiment)
VALUES (32, 'Not sure', 12, 1);
INSERT INTO public.option (id, name, question_id, sentiment)
VALUES (19, 'Nope, not me
', 8, 0);
INSERT INTO public.option (id, name, question_id, sentiment)
VALUES (13, 'NO, not for me', 6, 0);
INSERT INTO public.option (id, name, question_id, sentiment)
VALUES (12, 'YES, I’m ready to learn', 6, 1);
INSERT INTO public.option (id, name, question_id, sentiment)
VALUES (15, 'YES, I’m ready to learn', 7, 1);
INSERT INTO public.option (id, name, question_id, sentiment)
VALUES (14, 'Not sure', 6, 1);
INSERT INTO public.option (id, name, question_id, sentiment)
VALUES (9, 'YES, that sounds fun', 5, 1);
INSERT INTO public.option (id, name, question_id, sentiment)
VALUES (8, 'Not sure', 4, 1);
INSERT INTO public.option (id, name, question_id, sentiment)
VALUES (11, 'Not sure', 5, 1);
INSERT INTO public.option (id, name, question_id, sentiment)
VALUES (16, 'NO, not for me', 7, 0);
INSERT INTO public.option (id, name, question_id, sentiment)
VALUES (5, 'Not sure', 3, 1);
INSERT INTO public.option (id, name, question_id, sentiment)
VALUES (6, 'Yes, I like how that sounds
', 4, 1);
INSERT INTO public.option (id, name, question_id, sentiment)
VALUES (3, 'YES', 3, 1);
INSERT INTO public.option (id, name, question_id, sentiment)
VALUES (29, 'Not sure', 11, 1);
INSERT INTO public.option (id, name, question_id, sentiment)
VALUES (30, 'Yes, that can work for me', 12, 1);
INSERT INTO public.option (id, name, question_id, sentiment)
VALUES (24, 'YES, that sounds helpful', 10, 1);
INSERT INTO public.option (id, name, question_id, sentiment)
VALUES (27, 'YES, I can make that work', 11, 1);
INSERT INTO public.option (id, name, question_id, sentiment)
VALUES (26, 'Not sure', 10, 1);
INSERT INTO public.option (id, name, question_id, sentiment)
VALUES (21, 'Yes, I like how that sounds', 9, 1);
INSERT INTO public.option (id, name, question_id, sentiment)
VALUES (20, 'Not sure', 8, 1);
INSERT INTO public.option (id, name, question_id, sentiment)
VALUES (23, 'Not sure', 9, 1);
INSERT INTO public.option (id, name, question_id, sentiment)
VALUES (17, 'Not sure', 7, 1);
INSERT INTO public.option (id, name, question_id, sentiment)
VALUES (18, 'Yes, I like how that sounds', 8, 1);
INSERT INTO public.option (id, name, question_id, sentiment)
VALUES (10, 'NO thanks', 5, 0);
INSERT INTO public.option (id, name, question_id, sentiment)
VALUES (4, 'NO', 3, 0);
INSERT INTO public.option (id, name, question_id, sentiment)
VALUES (7, 'Nope, not me
', 4, 0);
INSERT INTO public.option (id, name, question_id, sentiment)
VALUES (28, 'NO, not possible', 11, 0);
INSERT INTO public.option (id, name, question_id, sentiment)
VALUES (31, 'Nope, not me
', 12, 0);
INSERT INTO public.option (id, name, question_id, sentiment)
VALUES (25, 'NO, not for me', 10, 0);
INSERT INTO public.option (id, name, question_id, sentiment)
VALUES (22, 'Nope, not me
', 9, 0);