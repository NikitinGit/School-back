package org.example.schoolback.dto.request;

/**
 * Какие проекты показывать: отбор для вкладок на экране проектов.
 */
public enum ProjectScope {
    /** Все проекты, которые доступны смотрящему. */
    ALL,
    /** Только свои проекты. */
    MY,
    /** Проекты группы. */
    GROUP
}
