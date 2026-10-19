package org.example.schoolback.util.assembler.impl;

import org.example.schoolback.dto.ProjectDTO;
import org.example.schoolback.dto.request.CreateProjectRequest;
import org.example.schoolback.dto.request.ProjectUpdateRequest;
import org.example.schoolback.entity.Group;
import org.example.schoolback.entity.Project;
import org.example.schoolback.entity.ProjectVisibility;
import org.example.schoolback.entity.User;
import org.example.schoolback.util.assembler.ProjectViewContext;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProjectAssembler {

    public ProjectDTO toDto(Project project, ProjectViewContext context) {
        Group group = project.getGroup();
        User author = project.getAuthor();
        Long groupId = group == null ? null : group.getId();

        return new ProjectDTO(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getLink(),
                project.getVisibility(),
                groupId,
                group == null ? null : group.getName(),
                author.getId(),
                author.getLogin(),
                author.getFullName(),
                group == null || group.getTeacher() == null ? null : group.getTeacher().getFullName(),
                context.likesOf(project.getId()),
                context.likedBy(project.getId()),
                context.isMyGroup(groupId),
                context.isMine(author.getId()),
                context.commentsOf(project.getId()),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

    public List<ProjectDTO> toDtoList(List<Project> projects, ProjectViewContext context) {
        return projects.stream()
                .map(project -> toDto(project, context))
                .collect(Collectors.toList());
    }

    /** Новый проект. Группа передаётся уже найденной, здесь к базе не обращаемся. */
    public Project toEntity(CreateProjectRequest request, User author, Group group) {
        Project project = new Project();
        project.setAuthor(author);
        project.setGroup(group);
        project.setTitle(request.title());
        project.setDescription(request.description());
        project.setLink(request.link());
        project.setVisibility(request.visibility() == null
                ? ProjectVisibility.ALL_STUDENTS
                : request.visibility());

        LocalDateTime now = LocalDateTime.now();
        project.setCreatedAt(now);
        project.setUpdatedAt(now);
        return project;
    }

    /**
     * Изменение проекта: поля, оставленные пустыми, не трогаем.
     * Группа передаётся отдельно, потому что её надо сначала найти в базе.
     */
    public void applyUpdate(Project project, ProjectUpdateRequest request, Group group) {
        if (request.title() != null) {
            project.setTitle(request.title());
        }
        if (request.description() != null) {
            project.setDescription(request.description());
        }
        if (request.link() != null) {
            project.setLink(request.link());
        }
        if (request.visibility() != null) {
            project.setVisibility(request.visibility());
        }
        if (request.groupId() != null) {
            project.setGroup(group);
        }
        project.setUpdatedAt(LocalDateTime.now());
    }
}
