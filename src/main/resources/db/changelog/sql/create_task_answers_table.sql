CREATE TABLE task_answers
(
    id              BIGSERIAL PRIMARY KEY,
    task_id         BIGINT      NOT NULL,
    student_id      BIGINT      NOT NULL,
    attempt         INTEGER     NOT NULL DEFAULT 1,
    answer_text     TEXT        NOT NULL,
    teacher_comment TEXT,
    status          VARCHAR(50) NOT NULL,
    submitted_at    TIMESTAMP   NOT NULL,
    reviewed_at     TIMESTAMP,

    CONSTRAINT uq_task_answers_attempt UNIQUE (task_id, student_id, attempt),
    CONSTRAINT fk_task_answers_task FOREIGN KEY (task_id)
        REFERENCES tasks (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_task_answers_student FOREIGN KEY (student_id)
        REFERENCES users (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_task_answers_student ON task_answers (student_id);
