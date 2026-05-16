-- V1__create_articles_table.sql

CREATE TABLE articles
(
    id         BIGSERIAL PRIMARY KEY,
    lang       VARCHAR(10)  NOT NULL,
    title      TEXT         NOT NULL,
    text       TEXT,
    url        TEXT         NOT NULL UNIQUE,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);