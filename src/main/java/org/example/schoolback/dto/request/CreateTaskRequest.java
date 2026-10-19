package org.example.schoolback.dto.request;

import java.time.LocalDate;
import java.util.List;

/**
 * Создание задачи преподавателем.
 *
 * @param rewardExpSlots скольким первым ученикам достаётся опыт; null — всем, кто сдал
 * @param rewardNote     пояснение к награде, например «первым трём»
 * @param groupIds       группы, которым выдаётся задача
 */
public record CreateTaskRequest(
        String title,
        String description,
        Long rewardCoins,
        Long rewardExp,
        Integer rewardExpSlots,
        String rewardNote,
        LocalDate dateFrom,
        LocalDate dateTo,
        List<Long> groupIds
) {
}
