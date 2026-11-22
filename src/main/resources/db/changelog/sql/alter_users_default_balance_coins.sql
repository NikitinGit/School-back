UPDATE users
SET balance_coins = 0
WHERE balance_coins IS NULL;

ALTER TABLE users
    ALTER COLUMN balance_coins SET DEFAULT 0;

ALTER TABLE users
    ALTER COLUMN balance_coins SET NOT NULL;
