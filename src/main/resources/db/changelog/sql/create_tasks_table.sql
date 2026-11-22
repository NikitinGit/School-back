CREATE TABLE tasks
(
    id              BIGSERIAL PRIMARY KEY,
    teacher_id      BIGINT       NOT NULL,
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    reward_coins    BIGINT       NOT NULL DEFAULT 0,
    reward_exp      BIGINT       NOT NULL DEFAULT 0,
    reward_exp_slots INTEGER,
    date_from       DATE         NOT NULL,
    date_to         DATE         NOT NULL,
    created_at      TIMESTAMP    NOT NULL,

    CONSTRAINT fk_tasks_teacher FOREIGN KEY (teacher_id)
        REFERENCES users (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_tasks_teacher ON tasks (teacher_id);
