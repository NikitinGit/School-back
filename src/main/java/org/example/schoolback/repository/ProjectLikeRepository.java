package org.example.schoolback.repository;

import org.example.schoolback.entity.ProjectLike;
import org.example.schoolback.entity.ProjectLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ProjectLikeRepository extends JpaRepository<ProjectLike, ProjectLikeId> {

    boolean existsByIdProjectIdAndIdUserId(Long projectId, Long userId);

    void deleteByIdProjectIdAndIdUserId(Long projectId, Long userId);

    long countByIdProjectId(Long projectId);

    /**
     * Какие из перечисленных проектов отметил пользователь — одним запросом на всю страницу,
     * а не по запросу на каждый проект.
     */
    @Query("SELECT l.id.projectId FROM ProjectLike l WHERE l.id.userId = :userId AND l.id.projectId IN :projectIds")
    Set<Long> findLikedProjectIds(@Param("userId") Long userId, @Param("projectIds") List<Long> projectIds);

    /** Число отметок сразу по списку проектов: пары «номер проекта, количество». */
    @Query("SELECT l.id.projectId, COUNT(l) FROM ProjectLike l WHERE l.id.projectId IN :projectIds GROUP BY l.id.projectId")
    List<Object[]> countLikesForProjects(@Param("projectIds") List<Long> projectIds);
}
