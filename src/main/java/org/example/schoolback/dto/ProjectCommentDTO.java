package org.example.schoolback.dto;

import java.time.LocalDateTime;

public record ProjectCommentDTO(
        Long id,
        Long projectId,
        Long authorId,
        String authorName,
        String text,
        LocalDateTime createdAt
) {
}
