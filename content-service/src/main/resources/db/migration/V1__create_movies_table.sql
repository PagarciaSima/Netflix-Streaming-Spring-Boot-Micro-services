-- ============================================================
-- content-service : schema for the movie catalog (content_db)
-- V1 - creates the "movies" table mapped by the Movie entity
-- ============================================================

CREATE TABLE IF NOT EXISTS movies (
    id               VARCHAR(36)   NOT NULL,
    title            VARCHAR(255)  NOT NULL,
    description      VARCHAR(1000),
    genre            VARCHAR(50),
    director         VARCHAR(255),
    `cast`           VARCHAR(255),           -- reserved word in MySQL -> quoted
    release_year     INT           NOT NULL DEFAULT 0,
    rating           DOUBLE        NOT NULL DEFAULT 0,
    thumbnail_url    VARCHAR(255),
    duration_minutes INT           NOT NULL DEFAULT 0,
    video_key        VARCHAR(255),
    hls_url          VARCHAR(255),
    video_status     VARCHAR(50),
    created_at       DATETIME(6),
    updated_at       DATETIME(6),
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Indexes backing MovieRepository queries
CREATE INDEX idx_movies_genre ON movies (genre);
CREATE INDEX idx_movies_title ON movies (title);
