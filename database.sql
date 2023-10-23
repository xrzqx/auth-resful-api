CREATE
    DATABASE auth_resful_api;

USE
    auth_resful_api;

CREATE TABLE users
(
    username                 VARCHAR(100) NOT NULL,
    password                 VARCHAR(100) NOT NULL,
    name                     VARCHAR(100) NOT NULL,
    email                    VARCHAR(100) NOT NULL,
    refresh_token            VARCHAR(100),
    refresh_token_expired_at BIGINT,
    access_token             VARCHAR(100),
    access_token_expired_at  BIGINT,
    PRIMARY KEY (username),
    UNIQUE (email,refresh_token,access_token)
) ENGINE InnoDB;

desc users;

DROP TABLE users;
