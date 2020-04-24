-- :name create-user! :! :n
-- :doc creates a new user record
INSERT INTO users
    (first_name, last_name, email, pass)
VALUES (:first_name, :last_name, :email, :pass);

-- :name create-user-return-id! :? :1
-- :doc creates a new user record and returns the ID of the user created
INSERT INTO users
    (first_name, last_name, email, pass)
VALUES (:first_name, :last_name, :email, :pass)
RETURNING id;

-- :name update-user! :! :n
-- :doc updates an existing user record
UPDATE users
SET first_name = :first_name,
    last_name  = :last_name,
    email      = :email
WHERE id = :id;

-- :name get-user :? :1
-- :doc retrieves a user record given the id
SELECT first_name, last_name, email
FROM users
WHERE id = :id;

-- :name get-user-by-email :? :1
-- :doc retrieve record of all users
SELECT first_name, last_name, email
FROM users
WHERE email = :email;

-- :name get-users :? :*
-- :doc retrieve record of all users
SELECT first_name, last_name, email
FROM users;

-- :name delete-user! :! :n
-- :doc deletes a user record given the id
DELETE
FROM users
WHERE id = :id;


-- :name create-question! :! :n
-- :doc creates a new question record
INSERT INTO question
    (question, sub_note, quiz_id)
VALUES (:name, :sub_note, :quiz_id);

-- :name update-question! :! :n
-- :doc updates an existing question record
UPDATE question
SET question = :question,
    sub_note = :sub_note,
    quiz_id  = :quiz_id
WHERE id = :id;

-- :name get-question :? :1
-- :doc retrieves a question record given the id
SELECT *
FROM question
WHERE id = :id
  AND quiz_id = :quiz-id;

-- :name get-questions :? :*
-- :doc retrieves all questions
SELECT *
FROM question
WHERE quiz_id = :quiz-id;

-- :name delete-question! :! :n
-- :doc deletes a question record given the id
DELETE
FROM question
WHERE id = :id
  AND quiz_id = :quiz-id;


-- :name create-setting! :! :n
-- :doc creates a new setting record
INSERT INTO setting
    (name, value)
VALUES (:name, :value);

-- :name update-setting! :! :n
-- :doc updates an existing setting record
UPDATE setting
SET value = :value
WHERE name = :name;

-- :name get-setting :? :1
-- :doc retrieves a setting record given the name
SELECT value
FROM setting
WHERE name = :name;

-- :name get-settings :? :*
-- :doc retrieves all city records
SELECT *
FROM setting;

-- :name delete-setting! :! :n
-- :doc deletes a setting record given the name
DELETE
FROM setting
WHERE name = :name;


-- :name create-option! :! :n
-- :doc creates a new option record for buttons
INSERT INTO option
    (name, question_id, sentiment)
VALUES (:name, :question_id, :sentiment);

-- :name update-option! :! :n
-- :doc updates an existing option record
UPDATE option
SET name        = :name,
    question_id = :question_id,
    sentiment= :sentiment
WHERE id = :id;

-- :name get-option :? :1
-- :doc retrieves an option record given the id
SELECT *
FROM option
WHERE id = :id;

-- :name get-option-by-sentiment-not-null :? :1
-- :doc retrieves an option record given the id
SELECT *
FROM option
WHERE id = :id
  AND sentiment IS NOT NULL;

-- :name get-options :? :*
-- :doc retrieves all option records order by id
SELECT *
FROM option
ORDER BY id;

-- :name get-option-by-question-id :? :*
-- :doc retrieves all option records order by question id
SELECT o.id, o.name
FROM option o
WHERE question_id = :question-id;

-- :name delete-option! :! :n
-- :doc deletes a option record given the id
DELETE
FROM option
WHERE id = :id;


-- :name create-answer! :! :n
-- :doc creates a new answer record
INSERT INTO answer
    (option_id, user_id)
VALUES (:option-id, :user-id);

-- :name update-answer! :! :n
-- :doc updates an existing answer record
UPDATE answer
SET option_id = :option-id
WHERE id = :id;

-- :name get-answer :? :1
-- :doc retrieves a answer record given the id
SELECT *
FROM answer
WHERE id = :id;

-- :name get-answers :? :*
-- :doc retrieves all answer records
SELECT *
FROM answer;

-- :name delete-answer! :! :n
-- :doc deletes a answer record given the id
DELETE
FROM answer
WHERE id = :id;


-- :name create-city! :! :n
-- :doc creates a new city record
INSERT INTO city
    (name)
VALUES (:name);

-- :name update-city! :! :n
-- :doc updates an existing city record
UPDATE city
SET name = :name
WHERE id = :id;

-- :name get-city :? :1
-- :doc retrieves a city record given the id
SELECT *
FROM city
WHERE id = :id;

-- :name get-cities :? :*
-- :doc retrieves all city records ordered by name
SELECT *
FROM city
ORDER BY name;

-- :name delete-city! :! :n
-- :doc deletes a city record given the id
DELETE
FROM city
WHERE id = :id;


-- :name create-quiz! :! :n
-- :doc creates a new quiz record
INSERT INTO quiz
    ("desc")
VALUES (:desc);

-- :name update-quiz! :! :n
-- :doc updates an existing quiz record
UPDATE quiz
SET "desc" = :desc
WHERE id = :id;

-- :name get-quiz :? :1
-- :doc retrieves a quiz record given the id
SELECT *
FROM quiz
WHERE id = :id;

-- :name get-quizzes :? :*
-- :doc retrieves all quiz records ordered by name
SELECT *
FROM quiz;

-- :name delete-quiz! :! :n
-- :doc deletes a quiz record given the id
DELETE
FROM quiz
WHERE id = :id;


-- :name get-question-options :? :*
-- :doc retrieves all questions and corresponding options
SELECT q.*, o.*
FROM question q
         inner join option o on q.id = o.question_id
WHERE q.id = :id;


-- :name get-question-by-quiz :? :*
-- :doc retrieves all questions and corresponding options
SELECT quiz.*, q.id as question_id, q.question, q.sub_note, o.id as option_id, o.name
FROM quiz
         inner join (question q left outer join option o on q.id = o.question_id) on quiz.id = q.quiz_id
WHERE quiz.id = :id;