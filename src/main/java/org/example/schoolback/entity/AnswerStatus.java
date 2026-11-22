package org.example.schoolback.entity;

/**
 * Состояние ответа ученика на задачу.
 */
public enum AnswerStatus {
    /** Ответ отправлен, преподаватель ещё не проверил. */
    SUBMITTED,
    /** Ответ принят. */
    ACCEPTED,
    /** Ответ отправлен на доработку, ученик может сдать заново. */
    REVISION
}
