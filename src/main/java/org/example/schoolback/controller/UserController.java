package org.example.schoolback.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Parameter;
import org.example.schoolback.dto.EnrollmentHistoryDTO;
import org.example.schoolback.dto.UserDTO;
import org.example.schoolback.dto.response.StudentResponse;
import org.example.schoolback.dto.response.TeacherResponse;
import org.example.schoolback.entity.*;
import org.example.schoolback.service.EnrollmentHistoryService;
import org.example.schoolback.service.UserService;
import org.example.schoolback.util.assembler.impl.EnrollmentHistoryAssembler;
import org.example.schoolback.util.assembler.impl.UserResourceAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserResourceAssembler userResourceAssembler;
    @Autowired
    private EnrollmentHistoryAssembler enrollmentHistoryAssembler;
    @Autowired
    private EnrollmentHistoryService enrollmentHistoryService;

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить информацию о пользователе по его id")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.getUserById(id);
        if (user.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        UserDTO resource = userResourceAssembler.toModel(user.get());
        return ResponseEntity.ok(resource);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создать нового пользователя")
    public ResponseEntity<UserDTO> createUser(@RequestBody @Valid UserDTO userDTO) {
        final User createdUser = userService.createUser(userDTO,
                resource -> userResourceAssembler.createEntity(resource)
        );
        final UserDTO resource = userResourceAssembler.toModel(createdUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(resource);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Обновить данные пользователя")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        final User updatedUser = userService.updateUser(id, userDTO, (userEntity, resource) -> userResourceAssembler.updateEntity(userEntity, resource));
        final UserDTO resource = userResourceAssembler.toModel(updatedUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(resource);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Удалить пользователя")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Получить информацию о своём профиле")
    public ResponseEntity<UserDTO> getCurrentUser() {
        Optional<User> user = userService.getCurrentUser();
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserDTO resource = userResourceAssembler.toModel(user.get());
        return ResponseEntity.ok(resource);
    }

    @PutMapping("/{id}/coins")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Начислить коины ученику")
    public ResponseEntity<UserDTO> addCoins(@PathVariable Long id, @RequestBody Long countCoins) {
        try {
            userService.addCoins(id, countCoins);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}/coinsHistory")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Получить информацию о истории начисления коинов для пользователя по его id")
    public Page<EnrollmentHistoryDTO> getCoinsHistoryById(
            @PathVariable Long id,
            @Parameter(description = "Номер страницы") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "enrollmentDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        User user = userService.getUserById(id).orElseThrow(() -> new IllegalArgumentException("User with id " + id + " not found"));

        Page<EnrollmentHistory> enrollmentHistory = enrollmentHistoryService.getEnrollmentHistoryByUser(user, pageable);
        return enrollmentHistory.map(entity->enrollmentHistoryAssembler.toDto(entity));
    }

    @GetMapping("allCoinsHistory")
    @PreAuthorize("hasAnyRole('TEACHER')")
    @Operation(summary = "Получить информацию о истории начисления всех коинов начисленных преподавателем")
    public Page<EnrollmentHistoryDTO> getAllCoinsHistory(
            @Parameter(description = "Номер страницы") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "enrollmentDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        User currentTeacher = userService.getCurrentUser().get();

        Page<EnrollmentHistory> enrollmentHistory = enrollmentHistoryService.getEnrollmentHistoryByTeacherSortedByDateDesc(currentTeacher, pageable);
        return enrollmentHistory.map(entity->enrollmentHistoryAssembler.toDto(entity));
    }

    @GetMapping("/students")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить список студентов")
    public Page<StudentResponse> getStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "secondName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> allStudents = userService.getByRole(Role.STUDENT, pageable);
        return allStudents.map(this::convertToStudentResponse);
    }

    @GetMapping("/teachers")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить список учителей")
    public Page<TeacherResponse> getTeachers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "secondName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> allStudents = userService.getByRole(Role.TEACHER, pageable);
        return allStudents.map(this::convertToTeacherResponse);
    }

    private StudentResponse convertToStudentResponse(User student) {
        StudentResponse response = new StudentResponse();
        response.setId(student.getId());
        response.setFullName(student.getFullName());
        response.setLogin(student.getLogin());
        response.setCoins(String.valueOf(student.getCoins()));
        return response;
    }

    private TeacherResponse convertToTeacherResponse(User teacher) {
        TeacherResponse response = new TeacherResponse();
        response.setId(teacher.getId());
        response.setFullName(teacher.getFullName());
        response.setLogin(teacher.getLogin());
        return response;
    }
}
