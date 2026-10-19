package org.example.schoolback.dto.request;

import org.example.schoolback.entity.ProjectVisibility;

/**
 * Изменение проекта. Поля, оставленные пустыми, не меняются.
 */
public record ProjectUpdateRequest(
        String title,
        String description,
        String link,
        ProjectVisibility visibility,
        Long groupId
) {
}
