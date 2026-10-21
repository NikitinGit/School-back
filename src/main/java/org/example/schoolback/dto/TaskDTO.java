package org.example.schoolback.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Задача — то, что отдаётся наружу преподавателю и администратору.
 *
 * @param rewardExpSlots скольким первым ученикам достаётся опыт; null — всем, кто сдал
 * @param overdue        срок сдачи уже прошёл
 */
public record TaskDTO(
        Long id,
        String title,
        String description,
        Long rewardCoins,
        Long rewardExp,
        Integer rewardExpSlots,
        String rewardNote,
        LocalDate dateFrom,
        LocalDate dateTo,
        List<Long> groupIds,
        List<String> groupNames,
        Long teacherId,
        String teacherName,
        boolean overdue,
        LocalDateTime createdAt
) {
}
