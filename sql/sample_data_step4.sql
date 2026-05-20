-- =====================================================================
-- ArtConnect - Sample Data (step 4 revision)
-- Group 8 / EFREI Databases 2 / TI603I
--
-- Updated to populate workshop.instructor_artist_id (introduced in
-- schema_step4.sql). Workshop instructors are now Artists, which
-- matches the Java model.
-- Run after schema_step4.sql.
-- =====================================================================

USE artconnect;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE booking;
TRUNCATE TABLE review;
TRUNCATE TABLE member_discipline;
TRUNCATE TABLE artist_discipline;
TRUNCATE TABLE artwork_artwork_tag;
TRUNCATE TABLE artwork_exhibition;
TRUNCATE TABLE exhibition_artist;
TRUNCATE TABLE workshop;
TRUNCATE TABLE artwork;
TRUNCATE TABLE exhibition;
TRUNCATE TABLE artwork_tag;
TRUNCATE TABLE discipline;
TRUNCATE TABLE community_member;
TRUNCATE TABLE gallery;
TRUNCATE TABLE artist;
TRUNCATE TABLE audit_log;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO discipline (name) VALUES
    ('Painting'), ('Sculpture'), ('Photography'), ('Digital Art'),
    ('Music'), ('Ceramics'), ('Printmaking'), ('Street Art');

INSERT INTO artwork_tag (name) VALUES
    ('Renaissance'), ('Impressionist'), ('Modern'), ('Landscape'),
    ('Portrait'), ('Abstract'), ('Religious'), ('Black-and-White');

INSERT INTO artist (name, bio, birth_year, contact_email, city, phone, website, is_active) VALUES
    ('Leonardo Vinci',    'Renaissance master and polymath.',                                            1452, 'leo@vincistudio.it',   'Florence',      '+39 055 00 00 00',  'https://vinci.example',      TRUE),
    ('Claude Monet',      'Founder of French Impressionist painting.',                                   1840, 'claude@monet.fr',      'Giverny',       '+33 1 00 00 00 01', 'https://monet.example',      TRUE),
    ('Ansel Adams',       'American landscape photographer and environmentalist.',                       1902, 'ansel@adams.co',       'San Francisco', '+1 415 000 0000',   'https://adams.example',      TRUE),
    ('Frida Kahlo',       'Mexican painter known for her many portraits and self-portraits.',            1907, 'frida@kahlo.mx',       'Mexico City',   '+52 55 0000 0000',  'https://kahlo.example',      TRUE),
    ('Auguste Rodin',     'French sculptor, founder of modern sculpture.',                               1840, 'auguste@rodin.fr',     'Paris',         '+33 1 00 00 00 02', 'https://rodin.example',      TRUE),
    ('Yayoi Kusama',      'Japanese contemporary artist working in sculpture and installation.',         1929, 'yayoi@kusama.jp',      'Tokyo',         '+81 3 0000 0000',   'https://kusama.example',     TRUE),
    ('Banksy Anon',       'Anonymous England-based street artist.',                                      1974, 'banksy@street.uk',     'Bristol',       '+44 117 000 0000',  'https://banksy.example',     TRUE),
    ('Pablo Picasso',     'Spanish painter and sculptor, co-founder of the Cubist movement.',            1881, 'pablo@picasso.es',     'Malaga',        '+34 952 000 000',   'https://picasso.example',    TRUE),
    ('Sandro Botticelli', 'Italian early Renaissance painter from Florence.',                            1445, 'sandro@botticelli.it', 'Florence',      '+39 055 00 00 01',  'https://botticelli.example', FALSE);

INSERT INTO artist_discipline (artist_id, discipline_id)
SELECT a.artist_id, d.discipline_id
FROM artist a JOIN discipline d ON
       (a.name = 'Leonardo Vinci'    AND d.name IN ('Painting','Sculpture'))
    OR (a.name = 'Claude Monet'      AND d.name = 'Painting')
    OR (a.name = 'Ansel Adams'       AND d.name = 'Photography')
    OR (a.name = 'Frida Kahlo'       AND d.name = 'Painting')
    OR (a.name = 'Auguste Rodin'     AND d.name = 'Sculpture')
    OR (a.name = 'Yayoi Kusama'      AND d.name IN ('Sculpture','Painting'))
    OR (a.name = 'Banksy Anon'       AND d.name IN ('Printmaking','Street Art'))
    OR (a.name = 'Pablo Picasso'     AND d.name IN ('Painting','Sculpture'))
    OR (a.name = 'Sandro Botticelli' AND d.name = 'Painting');

INSERT INTO gallery (name, address, owner_name, opening_hours, contact_phone, rating, website) VALUES
    ('Louvre Art House',    'Rue de Rivoli, Paris',            'Henri Loyrette', '09:00 to 18:00', '+33 1 40 20 50 50', 4.9, 'https://louvre.example'),
    ('The British Gallery', 'Great Russell St, London',        'Hartwig Fischer','10:00 to 17:30', '+44 20 7323 8000',  4.7, 'https://british.example'),
    ('Metropolitan Hub',    '1000 5th Ave, New York',          'Max Hollein',    '10:00 to 17:00', '+1 212 535 7710',   4.8, 'https://met.example'),
    ('Tate Modern Space',   'Bankside, London',                'Frances Morris', '10:00 to 18:00', '+44 20 7887 8888',  4.6, 'https://tate.example'),
    ('Uffizi Quarter',      'Piazzale degli Uffizi, Florence', 'Eike Schmidt',   '08:15 to 18:30', '+39 055 294 883',   4.8, 'https://uffizi.example');

INSERT INTO artwork (title, creation_year, type, medium, dimensions, description, price, status, artist_id)
SELECT t.title, t.creation_year, t.type, t.medium, t.dimensions, t.description, t.price, t.status, a.artist_id
FROM artist a JOIN (
    SELECT 'Mona Lisa'                       AS title, 1503 AS creation_year, 'Painting'     AS type, 'Oil on poplar'           AS medium, '77 x 53 cm'         AS dimensions, 'Iconic Renaissance portrait.'             AS description, 85000000.00 AS price, 'EXHIBITED' AS status, 'Leonardo Vinci'   AS artist_name UNION ALL
    SELECT 'The Last Supper',                       1498,                  'Painting',                'Tempera and oil',                    '460 x 880 cm',                       'Late 15th century mural.',                                  45000000.00,             'EXHIBITED',            'Leonardo Vinci' UNION ALL
    SELECT 'Water Lilies',                          1919,                  'Painting',                'Oil on canvas',                      '200 x 425 cm',                       'Late series of water lily ponds.',                          40000000.00,             'FOR_SALE',             'Claude Monet'   UNION ALL
    SELECT 'Impression Sunrise',                    1872,                  'Painting',                'Oil on canvas',                      '48 x 63 cm',                         'Painting that gave Impressionism its name.',                30000000.00,             'FOR_SALE',             'Claude Monet'   UNION ALL
    SELECT 'The Thinker',                           1904,                  'Sculpture',               'Bronze',                             '186 x 98 x 142 cm',                  'Bronze sculpture of a seated man.',                         15000000.00,             'EXHIBITED',            'Auguste Rodin'  UNION ALL
    SELECT 'The Kiss',                              1882,                  'Sculpture',               'Marble',                             '181 x 112 x 117 cm',                 'Marble sculpture of an embracing couple.',                  12000000.00,             'FOR_SALE',             'Auguste Rodin'  UNION ALL
    SELECT 'The Two Fridas',                        1939,                  'Painting',                'Oil on canvas',                      '173 x 173 cm',                       'Double self-portrait.',                                      5000000.00,             'FOR_SALE',             'Frida Kahlo'    UNION ALL
    SELECT 'Monolith, The Face of Half Dome',        1927,                  'Photography',             'Gelatin silver print',               'Varies',                             'Iconic Yosemite photograph.',                                 100000.00,             'FOR_SALE',             'Ansel Adams'    UNION ALL
    SELECT 'Infinity Mirror Room',                  2013,                  'Installation',            'Mirrors, LEDs',                      'Room-scale',                         'Immersive infinity mirror installation.',                    2000000.00,             'EXHIBITED',            'Yayoi Kusama'   UNION ALL
    SELECT 'Girl with Balloon',                     2002,                  'Street',                  'Stencil and spray paint',            '100 x 100 cm',                       'Stencil of a girl reaching for a balloon.',                  1500000.00,             'SOLD',                 'Banksy Anon'    UNION ALL
    SELECT 'Guernica',                              1937,                  'Painting',                'Oil on canvas',                      '349 x 776 cm',                       'Anti-war mural.',                                           20000000.00,             'EXHIBITED',            'Pablo Picasso'  UNION ALL
    SELECT 'Les Demoiselles d''Avignon',            1907,                  'Painting',                'Oil on canvas',                      '244 x 234 cm',                       'Early Cubist masterpiece.',                                 18000000.00,             'EXHIBITED',            'Pablo Picasso'  UNION ALL
    SELECT 'The Birth of Venus',                    1486,                  'Painting',                'Tempera on canvas',                  '172 x 278 cm',                       'Mythological scene of Venus.',                              30000000.00,             'EXHIBITED',            'Sandro Botticelli'
) t ON a.name = t.artist_name;

INSERT INTO artwork_artwork_tag (artwork_id, tag_id)
SELECT aw.artwork_id, tg.tag_id FROM artwork aw JOIN artwork_tag tg ON
       (aw.title = 'Mona Lisa'                       AND tg.name IN ('Renaissance','Portrait'))
    OR (aw.title = 'The Last Supper'                 AND tg.name IN ('Renaissance','Religious'))
    OR (aw.title = 'The Birth of Venus'              AND tg.name IN ('Renaissance','Portrait'))
    OR (aw.title = 'Water Lilies'                    AND tg.name IN ('Impressionist','Landscape'))
    OR (aw.title = 'Impression Sunrise'              AND tg.name IN ('Impressionist','Landscape'))
    OR (aw.title = 'The Thinker'                     AND tg.name = 'Modern')
    OR (aw.title = 'The Kiss'                        AND tg.name IN ('Modern','Portrait'))
    OR (aw.title = 'The Two Fridas'                  AND tg.name IN ('Modern','Portrait'))
    OR (aw.title = 'Monolith, The Face of Half Dome' AND tg.name IN ('Landscape','Black-and-White'))
    OR (aw.title = 'Infinity Mirror Room'            AND tg.name IN ('Modern','Abstract'))
    OR (aw.title = 'Girl with Balloon'               AND tg.name = 'Modern')
    OR (aw.title = 'Guernica'                        AND tg.name IN ('Modern','Abstract'))
    OR (aw.title = 'Les Demoiselles d''Avignon'      AND tg.name IN ('Modern','Abstract','Portrait'));

INSERT INTO community_member (name, email, birth_year, phone, city, membership_type) VALUES
    ('Alice Wonderland',  'alice@art.com',          1990, '+33 1 00 11 11 11', 'Paris',    'Premium'),
    ('Bob Ross',          'bob@happytrees.com',     1985, '+44 117 111 1111',  'London',   'Free'),
    ('Charlie Brown',     'charlie@peanuts.com',    1992, '+1 212 111 1111',   'New York', 'Premium'),
    ('Diana Prince',      'diana@themyscira.gr',    1988, '+30 21 111 1111',   'Athens',   'Premium'),
    ('Ethan Hunt',        'ethan@imf.de',           1980, '+49 30 111 1111',   'Berlin',   'Free');

INSERT INTO member_discipline (member_id, discipline_id)
SELECT m.member_id, d.discipline_id FROM community_member m JOIN discipline d ON
       (m.email = 'alice@art.com'       AND d.name IN ('Painting','Sculpture'))
    OR (m.email = 'bob@happytrees.com'  AND d.name = 'Painting')
    OR (m.email = 'charlie@peanuts.com' AND d.name IN ('Sculpture','Photography'))
    OR (m.email = 'diana@themyscira.gr' AND d.name = 'Painting')
    OR (m.email = 'ethan@imf.de'        AND d.name IN ('Street Art','Digital Art'));

INSERT INTO exhibition (title, start_date, end_date, description, curator_name, theme, gallery_id)
SELECT t.title, t.start_date, t.end_date, t.description, t.curator_name, t.theme, g.gallery_id
FROM gallery g JOIN (
    SELECT 'Renaissance Revival'      AS title, DATE '2026-04-01' AS start_date, DATE '2026-08-15' AS end_date, 'Selected works from the Italian Renaissance.'         AS description, 'Dr. Elena Rossi' AS curator_name, 'Classic Renaissance'           AS theme, 'Louvre Art House'    AS gallery_name UNION ALL
    SELECT 'Sculpting the Soul',          DATE '2026-05-01',              DATE '2026-07-30',              'Modern and classical sculpture side by side.',                       'Marcus Thorne',                   'Modern and Classical Sculpture',          'The British Gallery'  UNION ALL
    SELECT 'Impressionist Dreams',        DATE '2026-03-15',              DATE '2026-09-15',              'Light, water, and color in the late 19th century.',                  'Sarah Jenkins',                   'Light and Color',                          'Metropolitan Hub'      UNION ALL
    SELECT 'Frida and Friends',           DATE '2026-06-01',              DATE '2026-10-01',              'Frida Kahlo in dialogue with her contemporaries.',                   'Sofia Alvarez',                   'Self and Symbol',                          'Metropolitan Hub'      UNION ALL
    SELECT 'Street Echoes',               DATE '2026-05-20',              DATE '2026-07-10',              'Street art retrospective.',                                          'Jonas Wright',                    'Anonymous Voices',                         'Tate Modern Space'     UNION ALL
    SELECT 'Picasso Retrospective',       DATE '2026-07-01',              DATE '2026-11-30',              'Career-spanning Picasso exhibition.',                                'Maria Bianchi',                   'Cubism and Beyond',                        'Uffizi Quarter'
) t ON g.name = t.gallery_name;

INSERT INTO exhibition_artist (exhibition_id, artist_id)
SELECT e.exhibition_id, a.artist_id FROM exhibition e JOIN artist a ON
       (e.title = 'Renaissance Revival'   AND a.name IN ('Leonardo Vinci','Sandro Botticelli'))
    OR (e.title = 'Sculpting the Soul'    AND a.name IN ('Auguste Rodin','Yayoi Kusama'))
    OR (e.title = 'Impressionist Dreams'  AND a.name = 'Claude Monet')
    OR (e.title = 'Frida and Friends'     AND a.name IN ('Frida Kahlo','Yayoi Kusama'))
    OR (e.title = 'Street Echoes'         AND a.name = 'Banksy Anon')
    OR (e.title = 'Picasso Retrospective' AND a.name = 'Pablo Picasso');

INSERT INTO artwork_exhibition (artwork_id, exhibition_id)
SELECT aw.artwork_id, e.exhibition_id FROM artwork aw JOIN exhibition e ON
       (e.title = 'Renaissance Revival'   AND aw.title IN ('Mona Lisa','The Last Supper','The Birth of Venus'))
    OR (e.title = 'Sculpting the Soul'    AND aw.title IN ('The Thinker','The Kiss','Infinity Mirror Room'))
    OR (e.title = 'Impressionist Dreams'  AND aw.title IN ('Water Lilies','Impression Sunrise'))
    OR (e.title = 'Frida and Friends'     AND aw.title IN ('The Two Fridas','Infinity Mirror Room'))
    OR (e.title = 'Street Echoes'         AND aw.title = 'Girl with Balloon')
    OR (e.title = 'Picasso Retrospective' AND aw.title IN ('Guernica','Les Demoiselles d''Avignon'));

-- Workshops: instructor is now an Artist (direct FK).
INSERT INTO workshop (title, date, duration_minutes, max_participants, price, location, description, level, instructor_artist_id)
SELECT t.title, t.date, t.duration_minutes, t.max_participants, t.price, t.location, t.description, t.level, a.artist_id
FROM artist a JOIN (
    SELECT 'Mastering Oil Painting'      AS title, TIMESTAMP '2026-06-05 14:00:00' AS date, 180 AS duration_minutes,  8 AS max_participants, 150.00 AS price, 'Florence Studio'  AS location, 'Classical oil painting techniques.'    AS description, 'Intermediate' AS level, 'Leonardo Vinci' AS instructor_name UNION ALL
    SELECT 'Modern Sculpture Techniques',          TIMESTAMP '2026-06-12 10:00:00',          240,                    10,                       200.00,             'Paris Workshop',                'Hands-on modern sculpture techniques.',                 'Advanced',                  'Auguste Rodin'  UNION ALL
    SELECT 'Japanese Brush Art',                    TIMESTAMP '2026-06-20 09:30:00',          120,                    12,                        90.00,             'Tokyo Atelier',                 'Introduction to traditional sumi-e.',                   'Beginner',                  'Yayoi Kusama'   UNION ALL
    SELECT 'Impressionist Landscapes',              TIMESTAMP '2026-07-04 13:00:00',          180,                     6,                       120.00,             'Giverny Gardens',               'Plein-air impressionist painting.',                     'Beginner',                  'Claude Monet'   UNION ALL
    SELECT 'Street Art Bootcamp',                   TIMESTAMP '2026-07-15 16:00:00',          200,                    15,                       110.00,             'Berlin Yard',                   'Stencils, spray paint, and composition.',               'Beginner',                  'Banksy Anon'    UNION ALL
    SELECT 'Cubism Reimagined',                     TIMESTAMP '2026-08-10 11:00:00',          150,                     2,                       175.00,             'Malaga Studio',                 'Very small group cubism workshop.',                     'Advanced',                  'Pablo Picasso'
) t ON a.name = t.instructor_name;

INSERT INTO review (rating, comment, review_date, member_id, artwork_id)
SELECT r.rating, r.comment, r.review_date, m.member_id, aw.artwork_id
FROM community_member m JOIN artwork aw JOIN (
    SELECT 5 AS rating, 'Unbelievable detail!'           AS comment, DATE '2026-04-20' AS review_date, 'alice@art.com'        AS email, 'Mona Lisa'                 AS title UNION ALL
    SELECT 4,           'The colors are stunning.',                  DATE '2026-04-22',                'bob@happytrees.com',                 'Water Lilies'              UNION ALL
    SELECT 5,           'Deeply moving.',                            DATE '2026-04-25',                'charlie@peanuts.com',                'The Thinker'               UNION ALL
    SELECT 4,           'Powerful anti-war statement.',              DATE '2026-05-01',                'diana@themyscira.gr',                'Guernica'                  UNION ALL
    SELECT 5,           'Iconic. Made me think.',                    DATE '2026-05-03',                'ethan@imf.de',                       'Girl with Balloon'         UNION ALL
    SELECT 5,           'Heartbreaking and beautiful.',              DATE '2026-05-05',                'alice@art.com',                      'The Two Fridas'
) r ON m.email = r.email AND aw.title = r.title;

INSERT INTO booking (payment_status, member_id, workshop_id)
SELECT b.payment_status, m.member_id, w.workshop_id
FROM community_member m JOIN workshop w JOIN (
    SELECT 'alice@art.com'      AS email, 'Mastering Oil Painting'      AS title, 'PAID'      AS payment_status UNION ALL
    SELECT 'bob@happytrees.com',           'Impressionist Landscapes',                  'PAID'                            UNION ALL
    SELECT 'charlie@peanuts.com',          'Modern Sculpture Techniques',              'PENDING'                          UNION ALL
    SELECT 'diana@themyscira.gr',          'Mastering Oil Painting',                   'PAID'                             UNION ALL
    SELECT 'ethan@imf.de',                 'Street Art Bootcamp',                      'PENDING'                          UNION ALL
    SELECT 'alice@art.com',                'Modern Sculpture Techniques',              'PAID'
) b ON m.email = b.email AND w.title = b.title;
