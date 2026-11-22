package org.example.schoolback.dto;

import org.example.schoolback.entity.AnswerStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Задача глазами ученика: те же сведения плюс состояние его собственной сдачи.
 * Отдельный тип, а не наследник TaskDTO: записи наследоваться не могут.
 *
 * @param myStatus состояние сдачи; null — ученик ещё не сдавал
 */
public record StudentTaskDTO(
        Long id,
        String title,
        String description,
        Long rewardCoins,
        Long rewardExp,
        Integer rewardExpSlots,
        String rewardNote,
        LocalDate dateFrom,
        LocalDate dateTo,
        String groupName,
        String teacherName,
        boolean overdue,
        LocalDateTime createdAt,

        AnswerStatus myStatus,
        Integer myAttempt,
        String myAnswer,
        String teacherComment,
        LocalDateTime submittedAt
) {
}
