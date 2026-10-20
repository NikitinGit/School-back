package org.example.schoolback.exception;

import org.example.schoolback.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class RestExceptionHandler {

    private static final String ACCESS_DENIED_MESSAGE = "У вас нет прав на это действие";

    /**
     * Отказ в правах: и проверка роли через {@code @PreAuthorize}, и проверки владения
     * внутри служб. Отдаём 403, чтобы клиент отличал «тебе сюда нельзя» от «неверные данные».
     *
     * <p>Подробности наружу не выносим намеренно: по разным сообщениям посторонний мог бы
     * выяснить, существует ли чужая задача или проект с таким номером.
     *
     * <p>Внимание: это {@code org.springframework.security.access.AccessDeniedException},
     * а не одноимённый класс из {@code java.nio.file}.
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleAccessDenied(AccessDeniedException ex) {
        return new ErrorResponse(
                "FORBIDDEN",
                ACCESS_DENIED_MESSAGE,
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleCustomException(Exception ex) {
        return new ErrorResponse(
                "ERROR",
                ex.getMessage(),
                LocalDateTime.now()
        );
    }
}