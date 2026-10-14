-- Hibernate maps EnumType.STRING with length = 1 to CHAR(1).
-- Stream contains only A and B; align both columns without changing their data,
-- NOT NULL constraints, stream CHECK constraints, or indexes.
ALTER TABLE teams
    ALTER COLUMN stream TYPE CHAR(1);

ALTER TABLE matches
    ALTER COLUMN stream TYPE CHAR(1);
