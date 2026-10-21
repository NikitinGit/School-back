package org.example.schoolback.util.assembler;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Сведения, которые зависят от того, кто смотрит ленту проектов, и которые
 * собираются одним запросом на всю страницу, а не отдельным запросом на каждый проект.
 *
 * @param viewerId        кто смотрит
 * @param viewerGroupIds  группы, в которых он состоит
 * @param likedProjectIds какие из показываемых проектов он уже отметил
 * @param likeCounts      число отметок по каждому проекту
 * @param commentCounts   число комментариев по каждому проекту
 */
public record ProjectViewContext(
        Long viewerId,
        Set<Long> viewerGroupIds,
        Set<Long> likedProjectIds,
        Map<Long, Long> likeCounts,
        Map<Long, Long> commentCounts
) {

    /** Пустые сведения: проект ещё никто не видел, например сразу после создания. */
    public static ProjectViewContext forNewProject(Long viewerId) {
        return new ProjectViewContext(viewerId, Set.of(), Set.of(), Map.of(), Map.of());
    }

    public ProjectViewContext {
        viewerGroupIds = viewerGroupIds == null ? Collections.emptySet() : viewerGroupIds;
        likedProjectIds = likedProjectIds == null ? Collections.emptySet() : likedProjectIds;
        likeCounts = likeCounts == null ? Collections.emptyMap() : likeCounts;
        commentCounts = commentCounts == null ? Collections.emptyMap() : commentCounts;
    }

    public long likesOf(Long projectId) {
        return likeCounts.getOrDefault(projectId, 0L);
    }

    public int commentsOf(Long projectId) {
        return commentCounts.getOrDefault(projectId, 0L).intValue();
    }

    public boolean likedBy(Long projectId) {
        return likedProjectIds.contains(projectId);
    }

    public boolean isMyGroup(Long groupId) {
        return groupId != null && viewerGroupIds.contains(groupId);
    }

    public boolean isMine(Long authorId) {
        return viewerId != null && viewerId.equals(authorId);
    }
}
