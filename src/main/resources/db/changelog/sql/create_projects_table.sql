CREATE TABLE projects
(
    id          BIGSERIAL PRIMARY KEY,
    author_id   BIGINT       NOT NULL,
    group_id    BIGINT,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    link        VARCHAR(512),
    visibility  VARCHAR(50)  NOT NULL DEFAULT 'ALL_STUDENTS',
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP    NOT NULL,

    CONSTRAINT fk_projects_author FOREIGN KEY (author_id)
        REFERENCES users (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_projects_group FOREIGN KEY (group_id)
        REFERENCES groups (id)
        ON DELETE SET NULL
);

CREATE INDEX idx_projects_author ON projects (author_id);
CREATE INDEX idx_projects_group ON projects (group_id);
