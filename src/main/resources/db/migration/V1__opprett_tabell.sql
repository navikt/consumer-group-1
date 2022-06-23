DROP TABLE IF EXISTS flag_tabell;
CREATE TABLE flag_tabell
(
    id      SERIAL PRIMARY KEY,
    flag    VARCHAR(32) NOT NULL
);