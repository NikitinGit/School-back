package org.example.schoolback.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.example.schoolback.dto.ProjectCommentDTO;
import org.example.schoolback.dto.ProjectDTO;
import org.example.schoolback.dto.request.CreateCommentRequest;
import org.example.schoolback.dto.request.CreateProjectRequest;
import org.example.schoolback.dto.request.ProjectScope;
import org.example.schoolback.dto.request.ProjectUpdateRequest;
import org.example.schoolback.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    @Autowired
    private ProjectService projectService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Получить проекты",
            description = """
                    Лента проектов.
                    
                    **Отбор:** ALL — все доступные, MY — только свои, GROUP — проекты группы
                    (тогда нужен groupId).
                    
                    **Влияние роли:**
                    - ADMIN и TEACHER: видят проекты всех учеников;
                    - STUDENT: видит проекты, открытые всем, свои и проекты своих групп;
                    """)
    public Page<ProjectDTO> getProjects(
            @Parameter(description = "Отбор") @RequestParam(defaultValue = "ALL") ProjectScope scope,
            @Parameter(description = "Номер группы, нужен при отборе GROUP") @RequestParam(required = false) Long groupId,
            @Parameter(description = "Номер страницы") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        return projectService.getProjects(scope, groupId, pageable(page, size, sortBy, sortDir));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Получить проект по его id")
    public ResponseEntity<ProjectDTO> getProject(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Опубликовать проект",
            description = """
                    Автором становится текущий пользователь.
                    При видимости GROUP_ONLY группа обязательна, и это должна быть
                    группа, в которой пользователь состоит или которую он ведёт.
                    """)
    public ResponseEntity<ProjectDTO> createProject(@RequestBody CreateProjectRequest createProjectRequest) {
        final ProjectDTO resource = projectService.createProject(createProjectRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(resource);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Изменить проект",
            description = "Править проект может его автор или администратор. Пустые поля не меняются.")
    public ResponseEntity<ProjectDTO> updateProject(@PathVariable Long id,
                                                    @RequestBody ProjectUpdateRequest projectUpdateRequest) {
        return ResponseEntity.ok(projectService.updateProject(id, projectUpdateRequest));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Удалить проект",
            description = "Удалить проект может его автор или администратор.")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/like")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Отметить проект",
            description = "Повторная отметка ничего не меняет и ошибкой не считается.")
    public ResponseEntity<ProjectDTO> likeProject(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.likeProject(id));
    }

    @DeleteMapping("/{id}/like")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Снять отметку с проекта")
    public ResponseEntity<ProjectDTO> unlikeProject(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.unlikeProject(id));
    }

    @GetMapping("/{id}/comments")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Получить комментарии к проекту",
            description = "По умолчанию от самых старых к новым — в порядке переписки.")
    public Page<ProjectCommentDTO> getComments(
            @PathVariable Long id,
            @Parameter(description = "Номер страницы") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "20") int size
    ) {
        return projectService.getComments(id, PageRequest.of(page, size));
    }

    @PostMapping("/{id}/comments")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Добавить комментарий к проекту")
    public ResponseEntity<ProjectCommentDTO> addComment(@PathVariable Long id,
                                                        @RequestBody CreateCommentRequest createCommentRequest) {
        final ProjectCommentDTO resource = projectService.addComment(id, createCommentRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(resource);
    }

    @DeleteMapping("/{projectId}/comments/{commentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Удалить комментарий",
            description = "Удалить комментарий может его автор, автор проекта или администратор.")
    public ResponseEntity<Void> deleteComment(@PathVariable Long projectId, @PathVariable Long commentId) {
        projectService.deleteComment(projectId, commentId);
        return ResponseEntity.ok().build();
    }

    private Pageable pageable(int page, int size, String sortBy, String sortDir) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        return PageRequest.of(page, size, sort);
    }
}
