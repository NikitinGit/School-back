ALTER TABLE enrollment_history
    ADD COLUMN description VARCHAR(500);

ALTER TABLE enrollment_history
    ADD COLUMN enrolled_exp BIGINT NOT NULL DEFAULT 0;
