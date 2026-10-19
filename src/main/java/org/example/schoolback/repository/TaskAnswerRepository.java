package org.example.schoolback.repository;

import org.example.schoolback.entity.AnswerStatus;
import org.example.schoolback.entity.TaskAnswer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskAnswerRepository extends JpaRepository<TaskAnswer, Long> {

    /** Все сдачи по задаче — преподавателю на проверку. */
    Page<TaskAnswer> findByTaskId(Long taskId, Pageable pageable);

    /** Последняя попытка ученика по задаче: именно её состояние показывается на экране. */
    Optional<TaskAnswer> findFirstByTaskIdAndStudentIdOrderByAttemptDesc(Long taskId, Long studentId);

    /** Все попытки ученика по задаче, свежие первыми. */
    List<TaskAnswer> findByTaskIdAndStudentIdOrderByAttemptDesc(Long taskId, Long studentId);

    /** Номер последней попытки; пусто, если ученик ещё не сдавал. */
    @Query("SELECT MAX(a.attempt) FROM TaskAnswer a WHERE a.task.id = :taskId AND a.student.id = :studentId")
    Optional<Integer> findLastAttempt(@Param("taskId") Long taskId, @Param("studentId") Long studentId);

    /**
     * Сколько учеников уже получили «принято» по задаче.
     * Нужно для правила «опыт первым N сдавшим».
     */
    @Query("SELECT COUNT(DISTINCT a.student.id) FROM TaskAnswer a WHERE a.task.id = :taskId AND a.status = :status")
    long countStudentsWithStatus(@Param("taskId") Long taskId, @Param("status") AnswerStatus status);

    /**
     * Последние попытки учеников сразу по списку задач — чтобы не делать
     * отдельный запрос на каждую задачу в списке.
     */
    @Query("""
            SELECT a FROM TaskAnswer a
            WHERE a.student.id = :studentId
              AND a.task.id IN :taskIds
              AND a.attempt = (SELECT MAX(a2.attempt) FROM TaskAnswer a2
                               WHERE a2.task.id = a.task.id AND a2.student.id = :studentId)
            """)
    List<TaskAnswer> findLastAnswersForTasks(@Param("studentId") Long studentId,
                                             @Param("taskIds") List<Long> taskIds);
}
