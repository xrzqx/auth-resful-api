CREATE
DATABASE auth_resful_api;

USE
auth_resful_api;

CREATE TABLE users
(
    username VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL,
    name     VARCHAR(100) NOT NULL,
    email    VARCHAR(100) NOT NULL,
    PRIMARY KEY (username),
    UNIQUE (email)
) ENGINE InnoDB;

desc users;

DROP TABLE users;
