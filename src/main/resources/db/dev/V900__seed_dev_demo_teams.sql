-- DEVELOPMENT / DEMO DATA ONLY. Loaded only with the "dev" Spring profile.
-- To remove: delete src/main/resources/db/dev and the flyway.locations line in application-dev.properties.
-- Stream assignments and details are placeholders for demo purposes.
INSERT INTO teams (name, short_name, stadium, city, stream, founded_year) VALUES
    ('Nkana FC',       'NKA', 'Nkana Stadium',        'Kitwe',    'A', 1935),
    ('Power Dynamos',  'PWD', 'Dag Hammarskjold Stadium', 'Kitwe', 'A', 1971),
    ('ZESCO United',   'ZES', 'Levy Mwanawasa Stadium', 'Ndola',  'B', 1994),
    ('Red Arrows',     'RAR', 'Nkoloma Stadium',      'Lusaka',   'B', 1969);
