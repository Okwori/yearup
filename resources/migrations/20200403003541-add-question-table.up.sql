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

INSERT INTO public.question (id, question, sub_note, quiz_id)
VALUES (5, 'You’ll learn practical skills, like workplace communication, problem solving, and time management.', null,
        1);
INSERT INTO public.question (id, question, sub_note, quiz_id)
VALUES (6, 'You’ll be in class at Year Up 5 days a week for 6 months.', '30+ hours / week Monday to Friday 8:30am - 3:30pm
', 1);
INSERT INTO public.question (id, question, sub_note, quiz_id)
VALUES (7, 'You’ll be expected to arrive on time and professionally dressed, every weekday.', null, 1);
INSERT INTO public.question (id, question, sub_note, quiz_id)
VALUES (1, 'If you’ve made it here, you’re probably wondering whether Year Up is a good fit for you!', null, 1);
INSERT INTO public.question (id, question, sub_note, quiz_id)
VALUES (2, 'Find out if Year Up might be right for you In 3 minutes by responding to questions about the program.',
        null, 1);
INSERT INTO public.question (id, question, sub_note, quiz_id)
VALUES (3, 'Are you looking to start, advance, or change your career?', null, 1);
INSERT INTO public.question (id, question, sub_note, quiz_id)
VALUES (12, 'You’ll commute to ', 'You can get there on public transit', 1);
INSERT INTO public.question (id, question, sub_note, quiz_id)
VALUES (13, 'Before we get started, what is your e-mail address?',
        'Don’t worry, we won’t send you spam. This is just to make sure you’re a real person!', 1);
INSERT INTO public.question (id, question, sub_note, quiz_id)
VALUES (8, 'After those 6 months of training, you’ll be ready for a corporate internship.',
        'Fields include: Information Technology Financial Operations Business Operations Quality Assurance Software Development',
        1);
INSERT INTO public.question (id, question, sub_note, quiz_id)
VALUES (9, '6 months of successful career training leads to a 6 month corporate internship',
        '40+ hours / week Monday to Friday 9:00am to 5:00pm', 1);
INSERT INTO public.question (id, question, sub_note, quiz_id)
VALUES (10,
        'You’ll be learning with the support of a Year Up community of your peers, professional coaches, mentors, and social workers.',
        null, 1);
INSERT INTO public.question (id, question, sub_note, quiz_id)
VALUES (11, 'You’ll earn a stipend, but you’ll need to rely on savings, family support, and evening/weekend jobs.', 'Training: up to $150/week Internship: up to $250/week
', 1);
INSERT INTO public.question (id, question, sub_note, quiz_id)
VALUES (4, 'Are you willing to step outside of your comfort zone?
', null, 1);
