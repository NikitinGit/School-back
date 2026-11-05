package org.example.schoolback.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Запрос на начисление коинов ученику. */
public record CreateCoinTransactionRequest(
        @NotNull(message = "Student id is required")
        @Positive(message = "Student id must be positive")
        Long studentId,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        Long amount,

        @NotBlank(message = "Description is required")
        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description
) {
}
