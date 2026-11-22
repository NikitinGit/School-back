CREATE TABLE task_groups
(
    task_id  BIGINT NOT NULL,
    group_id BIGINT NOT NULL,
    PRIMARY KEY (task_id, group_id),
    CONSTRAINT fk_task_groups_task FOREIGN KEY (task_id)
        REFERENCES tasks (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_task_groups_group FOREIGN KEY (group_id)
        REFERENCES groups (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_task_groups_group ON task_groups (group_id);
