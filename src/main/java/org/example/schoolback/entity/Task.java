package org.example.schoolback.entity;

import jakarta.persistence.*;
import org.hibernate.Hibernate;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "reward_coins", nullable = false)
    private Long rewardCoins = 0L;

    @Column(name = "reward_exp", nullable = false)
    private Long rewardExp = 0L;

    /** Скольким первым ученикам достаётся опыт; null — всем, кто сдал. */
    @Column(name = "reward_exp_slots")
    private Integer rewardExpSlots;

    /** Пояснение к награде для показа, например «первым трём». */
    @Column(name = "reward_note")
    private String rewardNote;

    @Column(name = "date_from", nullable = false)
    private LocalDate dateFrom;

    @Column(name = "date_to", nullable = false)
    private LocalDate dateTo;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToMany
    @JoinTable(
            name = "task_groups",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id")
    )
    private Set<Group> groups = new HashSet<>();

    /** Задача просрочена, если дата окончания уже прошла. */
    public boolean isOverdue() {
        return dateTo != null && dateTo.isBefore(LocalDate.now());
    }

    /**
     * Сущности считаются одинаковыми только по идентификатору.
     * Несохранённые объекты (идентификатора ещё нет) равны лишь сами себе.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        return id != null && id.equals(((Task) o).id);
    }

    /** Постоянное значение: отпечаток не меняется при изменении полей. */
    @Override
    public int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
