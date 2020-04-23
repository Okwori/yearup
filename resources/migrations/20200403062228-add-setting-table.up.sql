create table setting
(
    name        varchar not null,
    value       varchar not null,
    description varchar
);

-- alter table setting owner to postgres;

create unique index settings_name_uindex
    on setting (name);

INSERT INTO public.setting (name, value, description)
VALUES ('RATIO', '60', '%age cutoff of answer sentiments to be deems successful');
INSERT INTO public.setting (name, value, description)
VALUES ('VIDEO', 'https://www.youtube.com/watch?v=D43z7kYi55I', 'URL to video to display on the last slide');