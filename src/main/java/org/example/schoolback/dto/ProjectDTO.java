package org.example.schoolback.dto;

import org.example.schoolback.entity.ProjectVisibility;

import java.time.LocalDateTime;

/**
 * Проект. Четыре признака вычисляются на сервере относительно того, кто спрашивает.
 *
 * @param teacherName преподаватель группы, к которой привязан проект
 * @param likedByMe   запрашивающий уже отметил проект
 * @param inMyGroup   проект принадлежит группе, в которой состоит запрашивающий
 * @param mine        запрашивающий — автор проекта
 */
public record ProjectDTO(
        Long id,
        String title,
        String description,
        String link,
        ProjectVisibility visibility,
        Long groupId,
        String groupName,
        Long authorId,
        String authorLogin,
        String authorName,
        String teacherName,
        long likes,
        boolean likedByMe,
        boolean inMyGroup,
        boolean mine,
        int commentsCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
