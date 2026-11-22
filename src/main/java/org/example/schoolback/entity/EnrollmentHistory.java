package org.example.schoolback.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import org.hibernate.Hibernate;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Entity
@Table(name = "enrollment_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "enrollment_date")
    private LocalDateTime enrollmentDate;

    @Column(name = "enrolled_coins")
    private Long enrolledCoins;

    @Column(name = "enrolled_exp", nullable = false)
    private Long enrolledExp = 0L;

    /** За что начислено: название задачи или произвольная причина. */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Сущности считаются одинаковыми только по идентификатору.
     * Несохранённые объекты (идентификатора ещё нет) равны лишь сами себе.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        return id != null && id.equals(((EnrollmentHistory) o).id);
    }

    /** Постоянное значение: отпечаток не меняется при изменении полей. */
    @Override
    public int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
