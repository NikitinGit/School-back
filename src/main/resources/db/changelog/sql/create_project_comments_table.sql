CREATE TABLE project_comments
(
    id         BIGSERIAL PRIMARY KEY,
    project_id BIGINT    NOT NULL,
    author_id  BIGINT    NOT NULL,
    text       TEXT      NOT NULL,
    created_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_project_comments_project FOREIGN KEY (project_id)
        REFERENCES projects (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_project_comments_author FOREIGN KEY (author_id)
        REFERENCES users (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_project_comments_project ON project_comments (project_id);
