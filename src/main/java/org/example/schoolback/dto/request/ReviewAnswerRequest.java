package org.example.schoolback.dto.request;

import org.example.schoolback.entity.AnswerStatus;

/**
 * Проверка ответа преподавателем.
 *
 * @param status допустимы только ACCEPTED и REVISION
 */
public record ReviewAnswerRequest(AnswerStatus status, String comment) {
}
