package org.example.schoolback.repository;

import org.example.schoolback.entity.ProjectComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectCommentRepository extends JpaRepository<ProjectComment, Long> {

    Page<ProjectComment> findByProjectIdOrderByCreatedAtAsc(Long projectId, Pageable pageable);

    long countByProjectId(Long projectId);

    /** Число комментариев сразу по списку проектов: пары «номер проекта, количество». */
    @Query("SELECT c.project.id, COUNT(c) FROM ProjectComment c WHERE c.project.id IN :projectIds GROUP BY c.project.id")
    List<Object[]> countCommentsForProjects(@Param("projectIds") List<Long> projectIds);
}
