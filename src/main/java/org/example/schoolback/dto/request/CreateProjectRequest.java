package org.example.schoolback.dto.request;

import org.example.schoolback.entity.ProjectVisibility;

/**
 * Публикация проекта.
 *
 * @param groupId группа, к которой привязан проект; обязательна при visibility = GROUP_ONLY
 */
public record CreateProjectRequest(
        String title,
        String description,
        String link,
        ProjectVisibility visibility,
        Long groupId
) {
}
