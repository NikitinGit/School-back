package org.example.schoolback.entity;

/**
 * Кому виден проект.
 */
public enum ProjectVisibility {
    /** Проект видят все ученики. */
    ALL_STUDENTS,
    /** Проект видят только ученики группы, к которой он привязан. */
    GROUP_ONLY
}
