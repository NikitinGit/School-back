package org.example.schoolback.dto.request;

/**
 * Какие задачи показывать: отбор для вкладок на экране списка задач.
 */
public enum TaskFilter {
    /** Все задачи, без учёта срока. */
    ALL,
    /** Срок ещё не вышел. */
    ACTIVE,
    /** Срок уже прошёл. */
    OVERDUE
}
