USE library_management;

SELECT COUNT(*) AS total_person
FROM person;

SELECT COUNT(*) AS total_resource
FROM resource;

SELECT COUNT(*) AS total_book
FROM physical_book;

SELECT COUNT(*) AS total_member
FROM member;

SELECT * FROM person LIMIT 10;