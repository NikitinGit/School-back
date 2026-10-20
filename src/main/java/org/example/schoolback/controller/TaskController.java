package org.example.schoolback.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.example.schoolback.dto.StudentTaskDTO;
import org.example.schoolback.dto.TaskAnswerDTO;
import org.example.schoolback.dto.TaskDTO;
import org.example.schoolback.dto.request.CreateTaskRequest;
import org.example.schoolback.dto.request.ReviewAnswerRequest;
import org.example.schoolback.dto.request.SubmitAnswerRequest;
import org.example.schoolback.dto.request.TaskFilter;
import org.example.schoolback.dto.request.TaskUpdateRequest;
import org.example.schoolback.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    @Autowired
    private TaskService taskService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Получить выданные задачи",
            description = """
                    Получить задачи, которые доступны пользователю.
                    
                    **Влияние роли:**
                    - ADMIN: получает все задачи;
                    - TEACHER: получает задачи, которые выдал сам;
                    """)
    public Page<TaskDTO> getTasks(
            @Parameter(description = "Номер страницы") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        return taskService.getTasks(pageable(page, size, sortBy, sortDir));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Получить свои задачи",
            description = """
                    Получить задачи ученика по всем его группам вместе с состоянием его последней сдачи.
                    
                    **Отбор:** ALL — все, ACTIVE — срок ещё не вышел, OVERDUE — срок прошёл.
                    """)
    public Page<StudentTaskDTO> getMyTasks(
            @Parameter(description = "Отбор по сроку") @RequestParam(defaultValue = "ALL") TaskFilter filter,
            @Parameter(description = "Номер страницы") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "dateTo") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        return taskService.getMyTasks(filter, pageable(page, size, sortBy, sortDir));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Получить задачу по её id")
    public ResponseEntity<TaskDTO> getTask(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskForTeacher(id));
    }

    @GetMapping("/my/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Получить свою задачу по её id",
            description = "То же, что и задача для преподавателя, но со состоянием сдачи текущего ученика.")
    public ResponseEntity<StudentTaskDTO> getMyTask(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getMyTask(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Выдать задачу группам",
            description = """
                    Создать задачу. Преподаватель может выдать её только своим группам,
                    администратор — любым.
                    """)
    public ResponseEntity<TaskDTO> createTask(@RequestBody CreateTaskRequest createTaskRequest) {
        final TaskDTO resource = taskService.createTask(createTaskRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(resource);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Изменить задачу",
            description = "Поля, оставленные пустыми, не меняются.")
    public ResponseEntity<TaskDTO> updateTask(@PathVariable Long id, @RequestBody TaskUpdateRequest taskUpdateRequest) {
        return ResponseEntity.ok(taskService.updateTask(id, taskUpdateRequest));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Удалить задачу")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/answers")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Сдать ответ на задачу",
            description = """
                    Каждая сдача — отдельная попытка с номером по порядку.
                    Сдать заново можно только после того, как преподаватель вернул ответ на доработку.
                    """)
    public ResponseEntity<TaskAnswerDTO> submitAnswer(@PathVariable Long id,
                                                      @RequestBody SubmitAnswerRequest submitAnswerRequest) {
        final TaskAnswerDTO resource = taskService.submitAnswer(id, submitAnswerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(resource);
    }

    @GetMapping("/{id}/answers")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Получить сдачи по задаче")
    public Page<TaskAnswerDTO> getAnswers(
            @PathVariable Long id,
            @Parameter(description = "Номер страницы") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "submittedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        return taskService.getAnswers(id, pageable(page, size, sortBy, sortDir));
    }

    @GetMapping("/{taskId}/answers/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Получить все попытки ученика по задаче",
            description = """
                    Попытки, свежие первыми.
                    
                    **Влияние роли:**
                    - ADMIN и TEACHER: попытки любого ученика по своей задаче;
                    - STUDENT: только свои попытки;
                    """)
    public List<TaskAnswerDTO> getStudentAnswers(@PathVariable Long taskId, @PathVariable Long studentId) {
        return taskService.getStudentAnswers(taskId, studentId);
    }

    @PutMapping("/{taskId}/answers/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Принять ответ или вернуть его на доработку",
            description = """
                    Проверяется последняя попытка ученика.
                    При ACCEPTED ученику начисляются монеты и опыт, появляется запись в истории начислений.
                    Опыт достаётся только первым ученикам, если у задачи задано ограничение.
                    """)
    public ResponseEntity<TaskAnswerDTO> reviewAnswer(@PathVariable Long taskId,
                                                      @PathVariable Long studentId,
                                                      @RequestBody ReviewAnswerRequest reviewAnswerRequest) {
        return ResponseEntity.ok(taskService.reviewAnswer(taskId, studentId, reviewAnswerRequest));
    }

    private Pageable pageable(int page, int size, String sortBy, String sortDir) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        return PageRequest.of(page, size, sort);
    }
}
