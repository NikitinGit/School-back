package org.example.schoolback.dto.request;

import java.time.LocalDate;
import java.util.List;

/**
 * Изменение задачи. Поля, оставленные пустыми, не меняются.
 */
public record TaskUpdateRequest(
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
