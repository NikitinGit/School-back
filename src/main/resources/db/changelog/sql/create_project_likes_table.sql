CREATE TABLE project_likes
(
    project_id BIGINT    NOT NULL,
    user_id    BIGINT    NOT NULL,
    liked_at   TIMESTAMP NOT NULL,
    PRIMARY KEY (project_id, user_id),
    CONSTRAINT fk_project_likes_project FOREIGN KEY (project_id)
        REFERENCES projects (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_project_likes_user FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE
);
