package org.example.schoolback.entity;

import jakarta.persistence.*;
import org.hibernate.Hibernate;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "task_answers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    /** Номер попытки сдачи, начиная с 1. */
    @Column(name = "attempt", nullable = false)
    private Integer attempt = 1;

    @Column(name = "answer_text", columnDefinition = "TEXT", nullable = false)
    private String answerText;

    @Column(name = "teacher_comment", columnDefinition = "TEXT")
    private String teacherComment;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AnswerStatus status;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    /**
     * Сущности считаются одинаковыми только по идентификатору.
     * Несохранённые объекты (идентификатора ещё нет) равны лишь сами себе.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        return id != null && id.equals(((TaskAnswer) o).id);
    }

    /** Постоянное значение: отпечаток не меняется при изменении полей. */
    @Override
    public int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
