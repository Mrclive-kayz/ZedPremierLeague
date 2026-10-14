-- zedPremierLeague initial schema.
-- NOTE: there is intentionally NO standings table; standings are calculated
-- from matches with status = 'FULL_TIME'.
-- Enum columns are stored as VARCHAR (EnumType.STRING). CHECK constraints are only
-- used for small closed sets (stream, match status); event types/categories/roles
-- can grow without a migration.

CREATE TABLE teams (
    id            BIGSERIAL    PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    short_name    VARCHAR(20)  NOT NULL,
    logo_url      VARCHAR(500),
    stadium       VARCHAR(150),
    city          VARCHAR(100),
    stream        VARCHAR(1)   NOT NULL,
    founded_year  INTEGER,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_teams_name UNIQUE (name),
    CONSTRAINT ck_teams_stream CHECK (stream IN ('A', 'B')),
    CONSTRAINT ck_teams_founded_year CHECK (founded_year IS NULL OR founded_year BETWEEN 1800 AND 2100)
);
CREATE INDEX idx_teams_stream ON teams (stream);

CREATE TABLE players (
    id            BIGSERIAL    PRIMARY KEY,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    shirt_number  INTEGER,
    position      VARCHAR(20)  NOT NULL,
    nationality   VARCHAR(100),
    team_id       BIGINT       NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_players_team FOREIGN KEY (team_id) REFERENCES teams (id),
    CONSTRAINT ck_players_shirt_number CHECK (shirt_number IS NULL OR shirt_number BETWEEN 1 AND 99),
    -- NULL shirt numbers are allowed and do not collide in PostgreSQL
    CONSTRAINT uq_players_team_shirt UNIQUE (team_id, shirt_number)
);
CREATE INDEX idx_players_team_id ON players (team_id);

CREATE TABLE matches (
    id            BIGSERIAL    PRIMARY KEY,
    home_team_id  BIGINT       NOT NULL,
    away_team_id  BIGINT       NOT NULL,
    match_date    TIMESTAMP    NOT NULL,
    venue         VARCHAR(150),
    stream        VARCHAR(1)   NOT NULL,
    match_week    INTEGER      NOT NULL,
    home_score    INTEGER      NOT NULL DEFAULT 0,
    away_score    INTEGER      NOT NULL DEFAULT 0,
    status        VARCHAR(20)  NOT NULL DEFAULT 'SCHEDULED',
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_matches_home_team FOREIGN KEY (home_team_id) REFERENCES teams (id),
    CONSTRAINT fk_matches_away_team FOREIGN KEY (away_team_id) REFERENCES teams (id),
    CONSTRAINT ck_matches_different_teams CHECK (home_team_id <> away_team_id),
    CONSTRAINT ck_matches_stream CHECK (stream IN ('A', 'B')),
    CONSTRAINT ck_matches_match_week CHECK (match_week >= 1),
    CONSTRAINT ck_matches_scores CHECK (home_score >= 0 AND away_score >= 0),
    CONSTRAINT ck_matches_status CHECK (status IN
        ('SCHEDULED', 'LIVE', 'HALF_TIME', 'FULL_TIME', 'POSTPONED', 'CANCELLED')),
    -- one fixture per ordered pairing per match week
    CONSTRAINT uq_matches_fixture UNIQUE (home_team_id, away_team_id, match_week)
);
CREATE INDEX idx_matches_match_date ON matches (match_date);
CREATE INDEX idx_matches_status ON matches (status);
CREATE INDEX idx_matches_stream ON matches (stream);
-- FK lookups for "all matches of a team"
CREATE INDEX idx_matches_home_team_id ON matches (home_team_id);
CREATE INDEX idx_matches_away_team_id ON matches (away_team_id);

CREATE TABLE match_events (
    id               BIGSERIAL   PRIMARY KEY,
    match_id         BIGINT      NOT NULL,
    team_id          BIGINT      NOT NULL,
    player_id        BIGINT,
    event_type       VARCHAR(30) NOT NULL,
    minute           INTEGER     NOT NULL,
    additional_time  INTEGER,
    created_at       TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_match_events_match  FOREIGN KEY (match_id)  REFERENCES matches (id) ON DELETE CASCADE,
    CONSTRAINT fk_match_events_team   FOREIGN KEY (team_id)   REFERENCES teams (id),
    CONSTRAINT fk_match_events_player FOREIGN KEY (player_id) REFERENCES players (id),
    CONSTRAINT ck_match_events_minute CHECK (minute BETWEEN 0 AND 130),
    CONSTRAINT ck_match_events_additional_time CHECK (additional_time IS NULL OR additional_time >= 0)
);
CREATE INDEX idx_match_events_match_id ON match_events (match_id, minute);
CREATE INDEX idx_match_events_player_id ON match_events (player_id);

CREATE TABLE articles (
    id            BIGSERIAL    PRIMARY KEY,
    title         VARCHAR(255) NOT NULL,
    slug          VARCHAR(255) NOT NULL,
    summary       VARCHAR(500),
    content       TEXT         NOT NULL,
    image_url     VARCHAR(500),
    category      VARCHAR(30)  NOT NULL DEFAULT 'NEWS',
    published     BOOLEAN      NOT NULL DEFAULT FALSE,
    published_at  TIMESTAMP,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_articles_slug UNIQUE (slug)  -- also provides the slug index
);
CREATE INDEX idx_articles_published ON articles (published, published_at DESC);

-- "users" because "user" is a reserved word in PostgreSQL.
-- password holds a hash only (BCrypt etc. once Spring Security is added).
CREATE TABLE users (
    id          BIGSERIAL    PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL,
    email       VARCHAR(255) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(20)  NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email UNIQUE (email)
);
