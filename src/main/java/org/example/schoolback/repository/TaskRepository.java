package org.example.schoolback.repository;

import org.example.schoolback.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /** Задачи, выданные конкретным преподавателем. */
    Page<Task> findByTeacherId(Long teacherId, Pageable pageable);

    /** Задачи конкретной группы. */
    Page<Task> findByGroupsId(Long groupId, Pageable pageable);

    /** Задачи группы, срок которых ещё не вышел — вкладка «Активные». */
    Page<Task> findByGroupsIdAndDateToGreaterThanEqual(Long groupId, LocalDate today, Pageable pageable);

    /** Задачи группы с истёкшим сроком — вкладка «Просроченные». */
    Page<Task> findByGroupsIdAndDateToLessThan(Long groupId, LocalDate today, Pageable pageable);

    /** Все задачи ученика: по всем группам, в которых он состоит. */
    @Query("""
            SELECT DISTINCT t FROM Task t
            JOIN t.groups g
            JOIN g.participants p
            WHERE p.id = :studentId
            """)
    Page<Task> findAllByStudentId(@Param("studentId") Long studentId, Pageable pageable);

    /** Задачи ученика, срок которых ещё не вышел — вкладка «Активные». */
    @Query("""
            SELECT DISTINCT t FROM Task t
            JOIN t.groups g
            JOIN g.participants p
            WHERE p.id = :studentId AND t.dateTo >= :today
            """)
    Page<Task> findActiveByStudentId(@Param("studentId") Long studentId,
                                    @Param("today") LocalDate today,
                                    Pageable pageable);

    /** Задачи ученика с истёкшим сроком — вкладка «Просроченные». */
    @Query("""
            SELECT DISTINCT t FROM Task t
            JOIN t.groups g
            JOIN g.participants p
            WHERE p.id = :studentId AND t.dateTo < :today
            """)
    Page<Task> findOverdueByStudentId(@Param("studentId") Long studentId,
                                      @Param("today") LocalDate today,
                                      Pageable pageable);

    /**
     * Доступна ли задача ученику: состоит ли он хотя бы в одной из групп,
     * которым она выдана. Нужно, чтобы чужую задачу нельзя было открыть по номеру.
     */
    @Query("""
            SELECT COUNT(t) > 0 FROM Task t
            JOIN t.groups g
            JOIN g.participants p
            WHERE t.id = :taskId AND p.id = :studentId
            """)
    boolean isVisibleToStudent(@Param("taskId") Long taskId, @Param("studentId") Long studentId);
}
