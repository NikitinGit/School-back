package org.example.schoolback.service;

import org.apache.commons.lang3.StringUtils;
import org.example.schoolback.dto.ProjectCommentDTO;
import org.example.schoolback.dto.ProjectDTO;
import org.example.schoolback.dto.request.CreateCommentRequest;
import org.example.schoolback.dto.request.CreateProjectRequest;
import org.example.schoolback.dto.request.ProjectScope;
import org.example.schoolback.dto.request.ProjectUpdateRequest;
import org.example.schoolback.entity.Group;
import org.example.schoolback.entity.Project;
import org.example.schoolback.entity.ProjectComment;
import org.example.schoolback.entity.ProjectLike;
import org.example.schoolback.entity.ProjectVisibility;
import org.example.schoolback.entity.Role;
import org.example.schoolback.entity.User;
import org.example.schoolback.repository.GroupRepository;
import org.example.schoolback.repository.ProjectCommentRepository;
import org.example.schoolback.repository.ProjectLikeRepository;
import org.example.schoolback.repository.ProjectRepository;
import org.example.schoolback.util.assembler.ProjectViewContext;
import org.example.schoolback.util.assembler.impl.ProjectAssembler;
import org.example.schoolback.util.assembler.impl.ProjectCommentAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Проекты учеников: лента с учётом того, кому проект виден, отметки «нравится»
 * и комментарии.
 *
 * <p>Служба отдаёт наружу готовые объекты передачи данных, а не сущности: в ответе
 * есть признаки, зависящие от смотрящего («я отметил», «проект моей группы»), и число
 * отметок с числом комментариев. Всё это собирается одним запросом на страницу,
 * а такое знание о выборках из базы — дело службы, а не обработчика запросов.
 */
@Service
@Transactional
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private ProjectLikeRepository projectLikeRepository;
    @Autowired
    private ProjectCommentRepository projectCommentRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private ProjectAssembler projectAssembler;
    @Autowired
    private ProjectCommentAssembler projectCommentAssembler;

    /**
     * Лента проектов.
     *
     * @param scope   вкладка: все доступные, только свои или проекты группы
     * @param groupId номер группы, обязателен для вкладки GROUP
     */
    @Transactional(readOnly = true)
    public Page<ProjectDTO> getProjects(ProjectScope scope, Long groupId, Pageable pageable) {
        final User viewer = currentUser();
        final Page<Project> projects = switch (scope == null ? ProjectScope.ALL : scope) {
            case MY -> projectRepository.findByAuthorId(viewer.getId(), pageable);
            case GROUP -> {
                if (groupId == null) {
                    throw new IllegalArgumentException("Group id is required to show projects of a group");
                }
                final Group group = requireGroup(groupId);
                requireGroupAccess(group, viewer);
                yield projectRepository.findByGroupId(groupId, pageable);
            }
            //преподавателю и администратору видны все проекты, ученику — только разрешённые ему
            case ALL -> canSeeEverything(viewer)
                    ? projectRepository.findAll(pageable)
                    : projectRepository.findVisibleTo(viewer.getId(), pageable);
        };

        final ProjectViewContext context = contextFor(projects.getContent(), viewer);
        return projects.map(project -> projectAssembler.toDto(project, context));
    }

    @Transactional(readOnly = true)
    public ProjectDTO getProjectById(Long projectId) {
        final User viewer = currentUser();
        final Project project = requireProject(projectId);
        requireVisible(project, viewer);

        return projectAssembler.toDto(project, contextFor(List.of(project), viewer));
    }

    public ProjectDTO createProject(CreateProjectRequest request) {
        final User author = currentUser();
        validateFields(request.title(), request.visibility(), request.groupId());

        final Group group = request.groupId() == null ? null : requireGroup(request.groupId());
        if (group != null) {
            requireGroupAccess(group, author);
        }

        final Project project = projectAssembler.toEntity(request, author, group);
        final Project saved = projectRepository.save(project);

        //проект только что создан: отметок и комментариев у него ещё нет
        return projectAssembler.toDto(saved, contextFor(List.of(saved), author));
    }

    public ProjectDTO updateProject(Long projectId, ProjectUpdateRequest request) {
        final User user = currentUser();
        final Project project = requireProject(projectId);
        requireProjectOwner(project, user);

        //видимость и группу проверяем в том виде, в каком они будут после изменения:
        //поля, не пришедшие в запросе, остаются прежними
        final ProjectVisibility visibility = request.visibility() == null
                ? project.getVisibility()
                : request.visibility();
        final Long groupId = request.groupId() == null
                ? (project.getGroup() == null ? null : project.getGroup().getId())
                : request.groupId();
        validateFields(request.title() == null ? project.getTitle() : request.title(), visibility, groupId);

        final Group group = request.groupId() == null ? null : requireGroup(request.groupId());
        if (group != null) {
            requireGroupAccess(group, user);
        }

        projectAssembler.applyUpdate(project, request, group);
        final Project saved = projectRepository.save(project);

        return projectAssembler.toDto(saved, contextFor(List.of(saved), user));
    }

    public void deleteProject(Long projectId) {
        final Project project = requireProject(projectId);
        requireProjectOwner(project, currentUser());

        projectRepository.delete(project);
    }

    /**
     * Отметить проект. Повторная отметка ничего не меняет и ошибкой не считается:
     * на экране это одна кнопка-переключатель, и повтор возможен от двойного нажатия.
     */
    public ProjectDTO likeProject(Long projectId) {
        final User user = currentUser();
        final Project project = requireProject(projectId);
        requireVisible(project, user);

        if (!projectLikeRepository.existsByIdProjectIdAndIdUserId(projectId, user.getId())) {
            final ProjectLike like = new ProjectLike();
            like.setProject(project);
            like.setUser(user);
            like.setLikedAt(LocalDateTime.now());
            projectLikeRepository.save(like);
        }

        return projectAssembler.toDto(project, contextFor(List.of(project), user));
    }

    /** Снять отметку. Если её не было, ответ тот же — состояние уже такое, как просят. */
    public ProjectDTO unlikeProject(Long projectId) {
        final User user = currentUser();
        final Project project = requireProject(projectId);
        requireVisible(project, user);

        projectLikeRepository.deleteByIdProjectIdAndIdUserId(projectId, user.getId());
        //удаление уже выполнено, но для подсчёта отметок надо, чтобы его увидел следующий запрос
        projectLikeRepository.flush();

        return projectAssembler.toDto(project, contextFor(List.of(project), user));
    }

    @Transactional(readOnly = true)
    public Page<ProjectCommentDTO> getComments(Long projectId, Pageable pageable) {
        final User viewer = currentUser();
        final Project project = requireProject(projectId);
        requireVisible(project, viewer);

        return projectCommentRepository.findByProjectIdOrderByCreatedAtAsc(projectId, pageable)
                .map(projectCommentAssembler::toDto);
    }

    public ProjectCommentDTO addComment(Long projectId, CreateCommentRequest request) {
        final User author = currentUser();
        final Project project = requireProject(projectId);
        requireVisible(project, author);

        if (request == null || StringUtils.isBlank(request.text())) {
            throw new IllegalArgumentException("Comment cannot be empty");
        }

        final ProjectComment comment = projectCommentAssembler.toEntity(request, project, author);
        return projectCommentAssembler.toDto(projectCommentRepository.save(comment));
    }

    /**
     * Удалить комментарий может его автор, автор проекта или администратор:
     * автор проекта отвечает за то, что под ним написано.
     */
    public void deleteComment(Long projectId, Long commentId) {
        final User user = currentUser();
        final Project project = requireProject(projectId);
        final ProjectComment comment = projectCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Comment with id: %d not found", commentId)));

        if (!comment.getProject().getId().equals(project.getId())) {
            throw new IllegalArgumentException(
                    String.format("Comment with id %d does not belong to project with id %d", commentId, projectId));
        }

        final boolean allowed = user.getRole().equals(Role.ADMIN)
                || comment.getAuthor().getId().equals(user.getId())
                || project.getAuthor().getId().equals(user.getId());
        if (!allowed) {
            throw new IllegalArgumentException(
                    String.format("Comment with id %d was written by another user", commentId));
        }

        projectCommentRepository.delete(comment);
    }

    /**
     * Сведения, зависящие от смотрящего, сразу по всей странице: его группы,
     * отмеченные им проекты, число отметок и число комментариев. Каждая величина —
     * один запрос на всю страницу, а не запрос на каждый проект.
     */
    private ProjectViewContext contextFor(List<Project> projects, User viewer) {
        final List<Long> projectIds = projects.stream()
                .map(Project::getId)
                .collect(Collectors.toList());

        if (projectIds.isEmpty()) {
            return new ProjectViewContext(viewer.getId(), groupIdsOf(viewer), Set.of(), Map.of(), Map.of());
        }

        return new ProjectViewContext(
                viewer.getId(),
                groupIdsOf(viewer),
                projectLikeRepository.findLikedProjectIds(viewer.getId(), projectIds),
                toCountMap(projectLikeRepository.countLikesForProjects(projectIds)),
                toCountMap(projectCommentRepository.countCommentsForProjects(projectIds))
        );
    }

    /** Пары «номер проекта, количество» из запроса подсчёта — в соответствие по номеру. */
    private Map<Long, Long> toCountMap(List<Object[]> rows) {
        final Map<Long, Long> counts = new HashMap<>();
        for (Object[] row : rows) {
            counts.put((Long) row[0], (Long) row[1]);
        }
        return counts;
    }

    /**
     * Группы смотрящего: и те, где он ученик, и те, где он преподаватель —
     * признак «проект моей группы» нужен и преподавателю.
     */
    private Set<Long> groupIdsOf(User user) {
        final Set<Long> groupIds = new HashSet<>();
        groupRepository.findAllGroupsByStudentId(user.getId()).forEach(group -> groupIds.add(group.getId()));
        groupRepository.findByTeacherId(user.getId()).forEach(group -> groupIds.add(group.getId()));
        return groupIds;
    }

    private void validateFields(String title, ProjectVisibility visibility, Long groupId) {
        if (StringUtils.isBlank(title)) {
            throw new IllegalArgumentException("Project title cannot be empty");
        }
        //без группы проект, видимый только группе, не виден никому, кроме автора
        if (ProjectVisibility.GROUP_ONLY.equals(visibility) && groupId == null) {
            throw new IllegalArgumentException("Project visible to a group only must have a group");
        }
    }

    /** Править и удалять проект может его автор или администратор. */
    private void requireProjectOwner(Project project, User user) {
        if (user.getRole().equals(Role.ADMIN)) {
            return;
        }
        if (project.getAuthor().getId().equals(user.getId())) {
            return;
        }
        throw new IllegalArgumentException(
                String.format("Project with id %d belongs to another user", project.getId()));
    }

    private void requireVisible(Project project, User viewer) {
        if (canSeeEverything(viewer)) {
            return;
        }
        if (!projectRepository.isVisibleTo(project.getId(), viewer.getId())) {
            throw new IllegalArgumentException(
                    String.format("Project with id %d is not available to current user", project.getId()));
        }
    }

    /**
     * Проект можно привязать к группе, в которой состоишь или которую ведёшь;
     * администратор — к любой.
     */
    private void requireGroupAccess(Group group, User user) {
        if (user.getRole().equals(Role.ADMIN)) {
            return;
        }
        if (group.getTeacher() != null && group.getTeacher().getId().equals(user.getId())) {
            return;
        }
        final boolean participant = groupRepository.findAllGroupsByStudentId(user.getId()).stream()
                .anyMatch(studentGroup -> studentGroup.getId().equals(group.getId()));
        if (!participant) {
            throw new IllegalArgumentException(
                    String.format("Group with id %d is not available to current user", group.getId()));
        }
    }

    /** Преподаватель и администратор видят проекты всех учеников. */
    private boolean canSeeEverything(User user) {
        return user.getRole().equals(Role.ADMIN) || user.getRole().equals(Role.TEACHER);
    }

    private Project requireProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Project with id: %d not found", projectId)));
    }

    private Group requireGroup(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Group with id: %d not found ", groupId)));
    }

    private User currentUser() {
        return userService.getCurrentUser()
                .orElseThrow(() -> new IllegalArgumentException("Current user not found"));
    }
}
