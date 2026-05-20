-- =====================================================================
-- ArtConnect - Schema (step 4 revision)
-- Group 8 / EFREI Databases 2 / TI603I
--
-- This file supersedes step 3's 00_schema.sql for step 4 onwards. The
-- only difference is in the `workshop` table:
--
--   * Step 2/3 modelled the workshop instructor as a many-to-many link
--     to community_member (`workshop_instructor` junction table).
--   * The Java model says `Workshop.instructor` is an `Artist` (see
--     model/Workshop.java line 9). To keep the schema aligned with the
--     application's domain model, we replace the junction table with a
--     direct mandatory FK `workshop.instructor_artist_id -> artist.artist_id`.
--
-- This is a normal LDM refinement: integration with the application
-- revealed that the original cardinality (member instructs workshop)
-- was too permissive; in practice only registered artists may
-- instruct, and exactly one instructor per workshop.
-- =====================================================================

DROP DATABASE IF EXISTS artconnect;
CREATE DATABASE artconnect
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE artconnect;

CREATE TABLE artist (
    artist_id       INT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100) NOT NULL UNIQUE,
    bio             TEXT,
    birth_year      INT,
    contact_email   VARCHAR(150) UNIQUE,
    city            VARCHAR(100),
    phone           VARCHAR(30),
    website         VARCHAR(200),
    social_media    VARCHAR(200),
    is_active       BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE gallery (
    gallery_id      INT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(150) NOT NULL UNIQUE,
    address         VARCHAR(255),
    owner_name      VARCHAR(100),
    opening_hours   VARCHAR(100),
    contact_phone   VARCHAR(30),
    rating          DECIMAL(3,1),
    website         VARCHAR(200)
) ENGINE=InnoDB;

CREATE TABLE exhibition (
    exhibition_id   INT AUTO_INCREMENT PRIMARY KEY,
    title           VARCHAR(200) NOT NULL,
    start_date      DATE NOT NULL,
    end_date        DATE NOT NULL,
    description     TEXT,
    curator_name    VARCHAR(100),
    theme           VARCHAR(100),
    gallery_id      INT NOT NULL,
    CONSTRAINT fk_exhibition_gallery
        FOREIGN KEY (gallery_id) REFERENCES gallery(gallery_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_exhibition_dates
        CHECK (end_date >= start_date)
) ENGINE=InnoDB;

CREATE TABLE artwork (
    artwork_id      INT AUTO_INCREMENT PRIMARY KEY,
    title           VARCHAR(200) NOT NULL,
    creation_year   INT,
    type            VARCHAR(50),
    medium          VARCHAR(100),
    dimensions      VARCHAR(100),
    description     TEXT,
    price           DECIMAL(10,2),
    status          ENUM('FOR_SALE','SOLD','EXHIBITED') NOT NULL DEFAULT 'FOR_SALE',
    artist_id       INT NOT NULL,
    CONSTRAINT fk_artwork_artist
        FOREIGN KEY (artist_id) REFERENCES artist(artist_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- workshop now has a direct instructor FK to artist (replaces the
-- workshop_instructor junction from step 2).
CREATE TABLE workshop (
    workshop_id          INT AUTO_INCREMENT PRIMARY KEY,
    title                VARCHAR(200) NOT NULL,
    date                 DATETIME NOT NULL,
    duration_minutes     INT,
    max_participants     INT,
    price                DECIMAL(8,2),
    location             VARCHAR(200),
    description          TEXT,
    level                VARCHAR(50),
    instructor_artist_id INT NOT NULL,
    CONSTRAINT fk_workshop_instructor
        FOREIGN KEY (instructor_artist_id) REFERENCES artist(artist_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_workshop_capacity
        CHECK (max_participants IS NULL OR max_participants > 0)
) ENGINE=InnoDB;

CREATE TABLE community_member (
    member_id        INT AUTO_INCREMENT PRIMARY KEY,
    name             VARCHAR(100) NOT NULL,
    email            VARCHAR(150) NOT NULL UNIQUE,
    birth_year       INT,
    phone            VARCHAR(30),
    city             VARCHAR(100),
    membership_type  VARCHAR(50)
) ENGINE=InnoDB;

CREATE TABLE discipline (
    discipline_id    INT AUTO_INCREMENT PRIMARY KEY,
    name             VARCHAR(100) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE artwork_tag (
    tag_id           INT AUTO_INCREMENT PRIMARY KEY,
    name             VARCHAR(80) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE review (
    review_id        INT AUTO_INCREMENT PRIMARY KEY,
    rating           INT NOT NULL,
    comment          TEXT,
    review_date      DATE,
    member_id        INT NOT NULL,
    artwork_id       INT NOT NULL,
    CONSTRAINT fk_review_member
        FOREIGN KEY (member_id) REFERENCES community_member(member_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_review_artwork
        FOREIGN KEY (artwork_id) REFERENCES artwork(artwork_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_review_rating
        CHECK (rating BETWEEN 1 AND 5)
) ENGINE=InnoDB;

CREATE TABLE booking (
    booking_id       INT AUTO_INCREMENT PRIMARY KEY,
    booking_date     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    payment_status   ENUM('PENDING','PAID','CANCELLED') NOT NULL DEFAULT 'PENDING',
    member_id        INT NOT NULL,
    workshop_id      INT NOT NULL,
    CONSTRAINT fk_booking_member
        FOREIGN KEY (member_id) REFERENCES community_member(member_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_booking_workshop
        FOREIGN KEY (workshop_id) REFERENCES workshop(workshop_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT uq_booking_unique UNIQUE (member_id, workshop_id)
) ENGINE=InnoDB;

CREATE TABLE exhibition_artist (
    exhibition_id    INT NOT NULL,
    artist_id        INT NOT NULL,
    PRIMARY KEY (exhibition_id, artist_id),
    CONSTRAINT fk_ea_exhibition
        FOREIGN KEY (exhibition_id) REFERENCES exhibition(exhibition_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_ea_artist
        FOREIGN KEY (artist_id) REFERENCES artist(artist_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE artwork_exhibition (
    artwork_id       INT NOT NULL,
    exhibition_id    INT NOT NULL,
    PRIMARY KEY (artwork_id, exhibition_id),
    CONSTRAINT fk_ae_artwork
        FOREIGN KEY (artwork_id) REFERENCES artwork(artwork_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_ae_exhibition
        FOREIGN KEY (exhibition_id) REFERENCES exhibition(exhibition_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE artwork_artwork_tag (
    artwork_id       INT NOT NULL,
    tag_id           INT NOT NULL,
    PRIMARY KEY (artwork_id, tag_id),
    CONSTRAINT fk_aat_artwork
        FOREIGN KEY (artwork_id) REFERENCES artwork(artwork_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_aat_tag
        FOREIGN KEY (tag_id) REFERENCES artwork_tag(tag_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE artist_discipline (
    artist_id        INT NOT NULL,
    discipline_id    INT NOT NULL,
    PRIMARY KEY (artist_id, discipline_id),
    CONSTRAINT fk_ad_artist
        FOREIGN KEY (artist_id) REFERENCES artist(artist_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_ad_discipline
        FOREIGN KEY (discipline_id) REFERENCES discipline(discipline_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE member_discipline (
    member_id        INT NOT NULL,
    discipline_id    INT NOT NULL,
    PRIMARY KEY (member_id, discipline_id),
    CONSTRAINT fk_md_member
        FOREIGN KEY (member_id) REFERENCES community_member(member_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_md_discipline
        FOREIGN KEY (discipline_id) REFERENCES discipline(discipline_id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE audit_log (
    audit_id         INT AUTO_INCREMENT PRIMARY KEY,
    entity_type      VARCHAR(50)  NOT NULL,
    entity_id        INT          NOT NULL,
    action           VARCHAR(20)  NOT NULL,
    action_user      VARCHAR(100) DEFAULT (CURRENT_USER()),
    action_time      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    details          TEXT
) ENGINE=InnoDB;

-- Indexes (kept from step 3)
CREATE INDEX idx_artist_city            ON artist            (city);
CREATE INDEX idx_artwork_status         ON artwork           (status);
CREATE INDEX idx_workshop_date          ON workshop          (date);
CREATE INDEX idx_workshop_instructor    ON workshop          (instructor_artist_id);
CREATE INDEX idx_exhibition_dates       ON exhibition        (start_date, end_date);
CREATE INDEX idx_booking_workshop_status ON booking          (workshop_id, payment_status);
