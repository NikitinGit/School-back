package org.example.schoolback.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Составной ключ отметки «нравится»: один пользователь может отметить проект только раз.
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectLikeId implements Serializable {

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "user_id")
    private Long userId;
}
