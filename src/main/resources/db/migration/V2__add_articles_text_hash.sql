-- V2__add_articles_text_hash.sql
-- Add SHA-256 hash of text content to detect duplicate articles
-- (different URLs, same body).

CREATE EXTENSION IF NOT EXISTS pgcrypto;

ALTER TABLE articles
    ADD COLUMN text_hash VARCHAR(64);

-- backfill existing rows
UPDATE articles
SET text_hash = encode(digest(coalesce(text, ''), 'sha256'), 'hex');

ALTER TABLE articles
    ALTER COLUMN text_hash SET NOT NULL;

CREATE INDEX idx_articles_text_hash ON articles (text_hash);
