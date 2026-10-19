package org.example.schoolback.entity;

import jakarta.persistence.*;
import org.hibernate.Hibernate;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    /** Группа, к которой привязан проект; нужна для отбора «проекты моей группы». */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "link", length = 512)
    private String link;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false)
    private ProjectVisibility visibility = ProjectVisibility.ALL_STUDENTS;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProjectLike> likes = new HashSet<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<ProjectComment> comments = new ArrayList<>();

    /**
     * Сущности считаются одинаковыми только по идентификатору.
     * Несохранённые объекты (идентификатора ещё нет) равны лишь сами себе.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        return id != null && id.equals(((Project) o).id);
    }

    /** Постоянное значение: отпечаток не меняется при изменении полей. */
    @Override
    public int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
