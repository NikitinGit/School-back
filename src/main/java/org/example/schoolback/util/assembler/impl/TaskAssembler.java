package org.example.schoolback.util.assembler.impl;

import org.example.schoolback.dto.StudentTaskDTO;
import org.example.schoolback.dto.TaskDTO;
import org.example.schoolback.dto.request.CreateTaskRequest;
import org.example.schoolback.dto.request.TaskUpdateRequest;
import org.example.schoolback.entity.Group;
import org.example.schoolback.entity.Task;
import org.example.schoolback.entity.TaskAnswer;
import org.example.schoolback.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class TaskAssembler {

    /** Задача для преподавателя и администратора. */
    public TaskDTO toDto(Task task) {
        List<Group> groups = sortedGroups(task);

        return new TaskDTO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getRewardCoins(),
                task.getRewardExp(),
                task.getRewardExpSlots(),
                task.getRewardNote(),
                task.getDateFrom(),
                task.getDateTo(),
                groups.stream().map(Group::getId).collect(Collectors.toList()),
                groups.stream().map(Group::getName).collect(Collectors.toList()),
                task.getTeacher() == null ? null : task.getTeacher().getId(),
                task.getTeacher() == null ? null : task.getTeacher().getFullName(),
                task.isOverdue(),
                task.getCreatedAt()
        );
    }

    public List<TaskDTO> toDtoList(List<Task> tasks) {
        return tasks.stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Задача для ученика: те же сведения плюс состояние его последней сдачи.
     *
     * @param lastAnswer последняя попытка ученика; null — он ещё не сдавал
     */
    public StudentTaskDTO toStudentDto(Task task, TaskAnswer lastAnswer) {
        List<Group> groups = sortedGroups(task);

        return new StudentTaskDTO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getRewardCoins(),
                task.getRewardExp(),
                task.getRewardExpSlots(),
                task.getRewardNote(),
                task.getDateFrom(),
                task.getDateTo(),
                groups.isEmpty() ? null : groups.get(0).getName(),
                task.getTeacher() == null ? null : task.getTeacher().getFullName(),
                task.isOverdue(),
                task.getCreatedAt(),

                lastAnswer == null ? null : lastAnswer.getStatus(),
                lastAnswer == null ? null : lastAnswer.getAttempt(),
                lastAnswer == null ? null : lastAnswer.getAnswerText(),
                lastAnswer == null ? null : lastAnswer.getTeacherComment(),
                lastAnswer == null ? null : lastAnswer.getSubmittedAt()
        );
    }

    /** Новая задача. Группы передаются уже найденными, здесь к базе не обращаемся. */
    public Task toEntity(CreateTaskRequest request, User teacher, Set<Group> groups) {
        Task task = new Task();
        task.setTeacher(teacher);
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setRewardCoins(request.rewardCoins() == null ? 0L : request.rewardCoins());
        task.setRewardExp(request.rewardExp() == null ? 0L : request.rewardExp());
        task.setRewardExpSlots(request.rewardExpSlots());
        task.setRewardNote(request.rewardNote());
        task.setDateFrom(request.dateFrom());
        task.setDateTo(request.dateTo());
        task.setGroups(groups == null ? new HashSet<>() : new HashSet<>(groups));
        task.setCreatedAt(LocalDateTime.now());
        return task;
    }

    /** Изменение задачи: поля, оставленные пустыми, не трогаем. */
    public void applyUpdate(Task task, TaskUpdateRequest request, Set<Group> groups) {
        if (request.title() != null) {
            task.setTitle(request.title());
        }
        if (request.description() != null) {
            task.setDescription(request.description());
        }
        if (request.rewardCoins() != null) {
            task.setRewardCoins(request.rewardCoins());
        }
        if (request.rewardExp() != null) {
            task.setRewardExp(request.rewardExp());
        }
        if (request.rewardExpSlots() != null) {
            task.setRewardExpSlots(request.rewardExpSlots());
        }
        if (request.rewardNote() != null) {
            task.setRewardNote(request.rewardNote());
        }
        if (request.dateFrom() != null) {
            task.setDateFrom(request.dateFrom());
        }
        if (request.dateTo() != null) {
            task.setDateTo(request.dateTo());
        }
        if (request.groupIds() != null) {
            task.getGroups().clear();
            if (groups != null) {
                task.getGroups().addAll(groups);
            }
        }
    }

    /** Группы в устойчивом порядке: иначе списки в ответе прыгали бы от запроса к запросу. */
    private List<Group> sortedGroups(Task task) {
        List<Group> groups = new ArrayList<>(task.getGroups());
        groups.sort(Comparator.comparing(Group::getId));
        return groups;
    }
}
