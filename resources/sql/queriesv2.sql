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