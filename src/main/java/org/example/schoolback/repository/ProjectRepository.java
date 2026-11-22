package org.example.schoolback.repository;

import org.example.schoolback.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /** Вкладка «Мои проекты». */
    Page<Project> findByAuthorId(Long authorId, Pageable pageable);

    /** Вкладка «Моя группа»: проекты, привязанные к конкретной группе. */
    Page<Project> findByGroupId(Long groupId, Pageable pageable);

    /**
     * Вкладка «Все»: лента с учётом того, кому проект виден.
     * Ученик видит проект, если он открыт всем, либо если это его собственный проект,
     * либо если проект привязан к группе, в которой ученик состоит.
     */
    @Query("""
            SELECT DISTINCT p FROM Project p
            LEFT JOIN p.group g
            LEFT JOIN g.participants gp
            WHERE p.visibility = org.example.schoolback.entity.ProjectVisibility.ALL_STUDENTS
               OR p.author.id = :userId
               OR gp.id = :userId
            """)
    Page<Project> findVisibleTo(@Param("userId") Long userId, Pageable pageable);

    /** Проверка доступа к одному проекту — чтобы чужой нельзя было открыть по номеру. */
    @Query("""
            SELECT COUNT(p) > 0 FROM Project p
            LEFT JOIN p.group g
            LEFT JOIN g.participants gp
            WHERE p.id = :projectId
              AND (p.visibility = org.example.schoolback.entity.ProjectVisibility.ALL_STUDENTS
                   OR p.author.id = :userId
                   OR gp.id = :userId)
            """)
    boolean isVisibleTo(@Param("projectId") Long projectId, @Param("userId") Long userId);
}
