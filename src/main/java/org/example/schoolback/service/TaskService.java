package org.example.schoolback.service;

import org.apache.commons.lang3.StringUtils;
import org.example.schoolback.dto.StudentTaskDTO;
import org.example.schoolback.dto.TaskAnswerDTO;
import org.example.schoolback.dto.TaskDTO;
import org.example.schoolback.dto.request.CreateTaskRequest;
import org.example.schoolback.dto.request.ReviewAnswerRequest;
import org.example.schoolback.dto.request.SubmitAnswerRequest;
import org.example.schoolback.dto.request.TaskFilter;
import org.example.schoolback.dto.request.TaskUpdateRequest;
import org.example.schoolback.entity.AnswerStatus;
import org.example.schoolback.entity.Group;
import org.example.schoolback.entity.Role;
import org.example.schoolback.entity.Task;
import org.example.schoolback.entity.TaskAnswer;
import org.example.schoolback.entity.User;
import org.example.schoolback.repository.GroupRepository;
import org.example.schoolback.repository.TaskAnswerRepository;
import org.example.schoolback.repository.TaskRepository;
import org.example.schoolback.util.assembler.impl.TaskAnswerAssembler;
import org.example.schoolback.util.assembler.impl.TaskAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Задачи, выдаваемые преподавателем группам, и сдача ответов учениками.
 *
 * <p>В отличие от служб, написанных раньше, здесь наружу отдаются уже готовые
 * объекты передачи данных, а не сущности. Причина: для списка задач ученика
 * и для ленты надо одним запросом подтянуть сведения о его последних сдачах,
 * а это знание о выборках из базы — место ему в службе, а не в обработчике запросов.
 */
@Service
@Transactional
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private TaskAnswerRepository taskAnswerRepository;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private EnrollmentHistoryService enrollmentHistoryService;
    @Autowired
    private TaskAssembler taskAssembler;
    @Autowired
    private TaskAnswerAssembler taskAnswerAssembler;

    /**
     * Задачи для преподавателя: администратор видит все, преподаватель — только свои.
     */
    @Transactional(readOnly = true)
    public Page<TaskDTO> getTasks(Pageable pageable) {
        final User current = currentUser();
        final Page<Task> tasks = current.getRole().equals(Role.ADMIN)
                ? taskRepository.findAll(pageable)
                : taskRepository.findByTeacherId(current.getId(), pageable);

        return tasks.map(taskAssembler::toDto);
    }

    /**
     * Задачи текущего ученика по всем его группам вместе с состоянием его последней сдачи.
     */
    @Transactional(readOnly = true)
    public Page<StudentTaskDTO> getMyTasks(TaskFilter filter, Pageable pageable) {
        final User student = currentUser();
        final LocalDate today = LocalDate.now();

        final Page<Task> tasks = switch (filter == null ? TaskFilter.ALL : filter) {
            case ACTIVE -> taskRepository.findActiveByStudentId(student.getId(), today, pageable);
            case OVERDUE -> taskRepository.findOverdueByStudentId(student.getId(), today, pageable);
            case ALL -> taskRepository.findAllByStudentId(student.getId(), pageable);
        };

        return withMyAnswers(tasks, student);
    }

    /** Одна задача глазами преподавателя или администратора. */
    @Transactional(readOnly = true)
    public TaskDTO getTaskForTeacher(Long taskId) {
        final Task task = requireTask(taskId);
        requireTaskManager(task, currentUser());

        return taskAssembler.toDto(task);
    }

    /** Одна задача глазами ученика: вместе с его последней сдачей. */
    @Transactional(readOnly = true)
    public StudentTaskDTO getMyTask(Long taskId) {
        final User student = currentUser();
        final Task task = requireTask(taskId);
        requireVisibleToStudent(taskId, student);

        final TaskAnswer lastAnswer = taskAnswerRepository
                .findFirstByTaskIdAndStudentIdOrderByAttemptDesc(taskId, student.getId())
                .orElse(null);

        return taskAssembler.toStudentDto(task, lastAnswer);
    }

    /**
     * Задачи одной группы. Доступны администратору, преподавателю этой группы
     * и её ученикам — чтобы по номеру группы нельзя было посмотреть чужие задачи.
     */
    @Transactional(readOnly = true)
    public Page<TaskDTO> getGroupTasks(Long groupId, TaskFilter filter, Pageable pageable) {
        final Group group = requireGroup(groupId);
        requireGroupAccess(group, currentUser());

        final LocalDate today = LocalDate.now();
        final Page<Task> tasks = switch (filter == null ? TaskFilter.ALL : filter) {
            case ACTIVE -> taskRepository.findByGroupsIdAndDateToGreaterThanEqual(groupId, today, pageable);
            case OVERDUE -> taskRepository.findByGroupsIdAndDateToLessThan(groupId, today, pageable);
            case ALL -> taskRepository.findByGroupsId(groupId, pageable);
        };

        return tasks.map(taskAssembler::toDto);
    }

    public TaskDTO createTask(CreateTaskRequest request) {
        final User author = currentUser();
        requireTeacherOrAdmin(author);
        validateCreate(request);

        final Set<Group> groups = findGroups(request.groupIds(), author);
        final Task task = taskAssembler.toEntity(request, author, groups);

        return taskAssembler.toDto(taskRepository.save(task));
    }

    public TaskDTO updateTask(Long taskId, TaskUpdateRequest request) {
        final User author = currentUser();
        final Task task = requireTask(taskId);
        requireTaskManager(task, author);
        validateUpdate(request);

        //группы ищем только если их просят поменять: пустое поле означает «оставить как есть»
        final Set<Group> groups = request.groupIds() == null
                ? null
                : findGroups(request.groupIds(), author);
        taskAssembler.applyUpdate(task, request, groups);

        return taskAssembler.toDto(taskRepository.save(task));
    }

    public void deleteTask(Long taskId) {
        final Task task = requireTask(taskId);
        requireTaskManager(task, currentUser());

        taskRepository.delete(task);
    }

    /**
     * Ученик сдаёт ответ. Каждая сдача — отдельная попытка с номером по порядку:
     * прежние ответы и замечания преподавателя к ним остаются в базе.
     */
    public TaskAnswerDTO submitAnswer(Long taskId, SubmitAnswerRequest request) {
        final User student = currentUser();
        if (!student.getRole().equals(Role.STUDENT)) {
            throw new IllegalArgumentException("Only a student can submit an answer");
        }
        if (request == null || StringUtils.isBlank(request.answerText())) {
            throw new IllegalArgumentException("Answer cannot be empty");
        }

        final Task task = requireTask(taskId);
        requireVisibleToStudent(taskId, student);

        if (task.getDateFrom() != null && task.getDateFrom().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Task is not open for answers yet");
        }
        if (task.isOverdue()) {
            throw new IllegalArgumentException("Task is overdue, the answer cannot be submitted");
        }

        final TaskAnswer lastAnswer = taskAnswerRepository
                .findFirstByTaskIdAndStudentIdOrderByAttemptDesc(taskId, student.getId())
                .orElse(null);
        if (lastAnswer != null && lastAnswer.getStatus().equals(AnswerStatus.ACCEPTED)) {
            throw new IllegalArgumentException("Answer is already accepted");
        }
        if (lastAnswer != null && lastAnswer.getStatus().equals(AnswerStatus.SUBMITTED)) {
            throw new IllegalArgumentException("Answer is already submitted and waiting for review");
        }

        final TaskAnswer answer = new TaskAnswer();
        answer.setTask(task);
        answer.setStudent(student);
        answer.setAttempt(lastAnswer == null ? 1 : lastAnswer.getAttempt() + 1);
        answer.setAnswerText(request.answerText());
        answer.setStatus(AnswerStatus.SUBMITTED);
        answer.setSubmittedAt(LocalDateTime.now());

        return taskAnswerAssembler.toDto(taskAnswerRepository.save(answer));
    }

    /** Сдачи по задаче — преподавателю на проверку. */
    @Transactional(readOnly = true)
    public Page<TaskAnswerDTO> getAnswers(Long taskId, Pageable pageable) {
        final Task task = requireTask(taskId);
        requireTaskManager(task, currentUser());

        return taskAnswerRepository.findByTaskId(taskId, pageable).map(taskAnswerAssembler::toDto);
    }

    /** Все попытки одного ученика по задаче, свежие первыми. */
    @Transactional(readOnly = true)
    public List<TaskAnswerDTO> getStudentAnswers(Long taskId, Long studentId) {
        final Task task = requireTask(taskId);
        final User current = currentUser();
        //свои попытки ученик смотреть может, чужие — нет
        if (!current.getId().equals(studentId)) {
            requireTaskManager(task, current);
        } else {
            requireVisibleToStudent(taskId, current);
        }

        return taskAnswerAssembler.toDtoList(
                taskAnswerRepository.findByTaskIdAndStudentIdOrderByAttemptDesc(taskId, studentId));
    }

    /**
     * Преподаватель принимает ответ или возвращает его на доработку.
     * Проверяется последняя попытка ученика: именно она ждёт проверки.
     */
    public TaskAnswerDTO reviewAnswer(Long taskId, Long studentId, ReviewAnswerRequest request) {
        final User teacher = currentUser();
        final Task task = requireTask(taskId);
        requireTaskManager(task, teacher);

        if (request == null || request.status() == null
                || (!request.status().equals(AnswerStatus.ACCEPTED) && !request.status().equals(AnswerStatus.REVISION))) {
            throw new IllegalArgumentException("Review status must be ACCEPTED or REVISION");
        }

        final TaskAnswer answer = taskAnswerRepository
                .findFirstByTaskIdAndStudentIdOrderByAttemptDesc(taskId, studentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Student with id %d has not submitted an answer to task with id %d", studentId, taskId)));

        if (answer.getStatus().equals(AnswerStatus.ACCEPTED)) {
            throw new IllegalArgumentException("Answer is already accepted");
        }
        if (answer.getStatus().equals(AnswerStatus.REVISION)) {
            throw new IllegalArgumentException("Answer is already sent for revision, a new attempt is expected");
        }

        //сколько учеников уже получили «принято», считаем до того, как примем этот ответ,
        //иначе текущий ученик занял бы своё же место среди первых сдавших
        final long acceptedBefore = taskAnswerRepository.countStudentsWithStatus(taskId, AnswerStatus.ACCEPTED);

        answer.setStatus(request.status());
        answer.setTeacherComment(request.comment());
        answer.setReviewedAt(LocalDateTime.now());
        final TaskAnswer reviewed = taskAnswerRepository.save(answer);

        if (request.status().equals(AnswerStatus.ACCEPTED)) {
            giveReward(task, reviewed.getStudent().getId(), teacher, acceptedBefore);
        }

        return taskAnswerAssembler.toDto(reviewed);
    }

    /**
     * Награда за принятый ответ: монеты достаются каждому, чей ответ приняли,
     * а опыт — только первым rewardExpSlots ученикам. Если ограничение не задано,
     * опыт получают все.
     *
     * @param acceptedBefore сколько учеников получили «принято» до этого ответа
     */
    private void giveReward(Task task, Long studentId, User teacher, long acceptedBefore) {
        final long coins = task.getRewardCoins() == null ? 0L : task.getRewardCoins();
        final Integer slots = task.getRewardExpSlots();
        final boolean expEarned = slots == null || acceptedBefore < slots;
        final long experience = expEarned && task.getRewardExp() != null ? task.getRewardExp() : 0L;

        if (coins == 0L && experience == 0L) {
            return;
        }

        final User student = userService.addTaskReward(studentId, coins, experience);
        enrollmentHistoryService.saveEnrollmentHistory(teacher, student, coins, experience,
                String.format("Задача: %s", task.getTitle()));
    }

    /**
     * Последние сдачи учеником сразу по всей странице задач — одним запросом,
     * чтобы не обращаться к базе отдельно по каждой задаче списка.
     */
    private Page<StudentTaskDTO> withMyAnswers(Page<Task> tasks, User student) {
        final List<Long> taskIds = tasks.getContent().stream()
                .map(Task::getId)
                .collect(Collectors.toList());

        final Map<Long, TaskAnswer> lastAnswers = taskIds.isEmpty()
                ? Map.of()
                : taskAnswerRepository.findLastAnswersForTasks(student.getId(), taskIds).stream()
                .collect(Collectors.toMap(answer -> answer.getTask().getId(), Function.identity()));

        return tasks.map(task -> taskAssembler.toStudentDto(task, lastAnswers.get(task.getId())));
    }

    /**
     * Преподаватель может выдать задачу только своим группам, администратор — любым.
     */
    private Set<Group> findGroups(List<Long> groupIds, User author) {
        final Set<Group> groups = new HashSet<>();
        for (Long groupId : groupIds) {
            final Group group = requireGroup(groupId);
            if (!author.getRole().equals(Role.ADMIN)
                    && (group.getTeacher() == null || !group.getTeacher().getId().equals(author.getId()))) {
                throw new IllegalArgumentException(
                        String.format("Group with id %d belongs to another teacher", groupId));
            }
            groups.add(group);
        }
        return groups;
    }

    private void validateCreate(CreateTaskRequest request) {
        if (StringUtils.isBlank(request.title())) {
            throw new IllegalArgumentException("Task title cannot be empty");
        }
        if (request.groupIds() == null || request.groupIds().isEmpty()) {
            throw new IllegalArgumentException("Task must be given to at least one group");
        }
        if (request.dateFrom() == null || request.dateTo() == null) {
            throw new IllegalArgumentException("Task must have a start date and a deadline");
        }
        validatePeriod(request.dateFrom(), request.dateTo());
        validateReward(request.rewardCoins(), request.rewardExp(), request.rewardExpSlots());
    }

    private void validateUpdate(TaskUpdateRequest request) {
        if (request.title() != null && StringUtils.isBlank(request.title())) {
            throw new IllegalArgumentException("Task title cannot be empty");
        }
        if (request.groupIds() != null && request.groupIds().isEmpty()) {
            throw new IllegalArgumentException("Task must be given to at least one group");
        }
        //срок проверяем только когда обе даты пришли в запросе: иначе сравнивать не с чем
        if (request.dateFrom() != null && request.dateTo() != null) {
            validatePeriod(request.dateFrom(), request.dateTo());
        }
        validateReward(request.rewardCoins(), request.rewardExp(), request.rewardExpSlots());
    }

    private void validatePeriod(LocalDate dateFrom, LocalDate dateTo) {
        if (dateTo.isBefore(dateFrom)) {
            throw new IllegalArgumentException("Task deadline cannot be earlier than its start date");
        }
    }

    private void validateReward(Long rewardCoins, Long rewardExp, Integer rewardExpSlots) {
        if (rewardCoins != null && rewardCoins < 0) {
            throw new IllegalArgumentException("Reward in coins cannot be negative");
        }
        if (rewardExp != null && rewardExp < 0) {
            throw new IllegalArgumentException("Reward in experience cannot be negative");
        }
        if (rewardExpSlots != null && rewardExpSlots <= 0) {
            throw new IllegalArgumentException("Number of students getting experience must be positive");
        }
    }

    /** Править задачу может выдавший её преподаватель или администратор. */
    private void requireTaskManager(Task task, User user) {
        if (user.getRole().equals(Role.ADMIN)) {
            return;
        }
        if (task.getTeacher() != null && task.getTeacher().getId().equals(user.getId())) {
            return;
        }
        throw new IllegalArgumentException(
                String.format("Task with id %d belongs to another teacher", task.getId()));
    }

    private void requireVisibleToStudent(Long taskId, User student) {
        if (!taskRepository.isVisibleToStudent(taskId, student.getId())) {
            throw new IllegalArgumentException(
                    String.format("Task with id %d is not available to current user", taskId));
        }
    }

    /**
     * Задачи группы доступны администратору, её преподавателю и её ученикам.
     * Участие проверяем запросом к базе, а не через список участников группы:
     * список загружается отложенно, и обращение к нему тянуло бы его целиком.
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

    private void requireTeacherOrAdmin(User user) {
        if (!user.getRole().equals(Role.TEACHER) && !user.getRole().equals(Role.ADMIN)) {
            throw new IllegalArgumentException("Only a teacher or an administrator can manage tasks");
        }
    }

    private Task requireTask(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Task with id: %d not found", taskId)));
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
