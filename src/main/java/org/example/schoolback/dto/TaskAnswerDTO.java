package org.example.schoolback.dto;

import org.example.schoolback.entity.AnswerStatus;

import java.time.LocalDateTime;

/**
 * Сдача ученика — то, что видит преподаватель при проверке.
 */
public record TaskAnswerDTO(
        Long id,
        Long taskId,
        Long studentId,
        String studentName,
        Integer attempt,
        String answerText,
        AnswerStatus status,
        String teacherComment,
        LocalDateTime submittedAt,
        LocalDateTime reviewedAt
) {
}
