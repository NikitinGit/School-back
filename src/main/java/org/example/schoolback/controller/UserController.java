package org.example.schoolback.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.example.schoolback.dto.UserDTO;
import org.example.schoolback.dto.response.StudentResponse;
import org.example.schoolback.dto.response.TeacherResponse;
import org.example.schoolback.dto.response.UserResponse;
import org.example.schoolback.entity.*;
import org.example.schoolback.service.UserService;
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

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получить информацию о пользователе по его id")
    public ResponseEntity<UserResponse> getUserById(@PathVariable @Positive Long id) {
        Optional<User> user = userService.getUserById(id);
        if (user.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        UserResponse resource = userResourceAssembler.toModel(user.get());
        return ResponseEntity.ok(resource);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создать нового пользователя")
    public ResponseEntity<UserResponse> createUser(@RequestBody @Valid UserDTO userDTO) {
        final User createdUser = userService.createUser(userDTO,
                resource -> userResourceAssembler.createEntity(resource)
        );
        final UserResponse resource = userResourceAssembler.toModel(createdUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(resource);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Обновить данные пользователя")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        final User updatedUser = userService.updateUser(id, userDTO, (userEntity, resource) -> userResourceAssembler.updateEntity(userEntity, resource));
        final UserResponse resource = userResourceAssembler.toModel(updatedUser);
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
    public ResponseEntity<UserResponse> getCurrentUser() {
        Optional<User> user = userService.getCurrentUser();
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserResponse resource = userResourceAssembler.toModel(user.get());
        return ResponseEntity.ok(resource);
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
        response.setBirthDate(student.getBirthDate());
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
