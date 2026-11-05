package org.example.schoolback.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.example.schoolback.dto.EnrollmentHistoryDTO;
import org.example.schoolback.dto.request.CreateCoinTransactionRequest;
import org.example.schoolback.entity.User;
import org.example.schoolback.service.EnrollmentHistoryService;
import org.example.schoolback.service.UserService;
import org.example.schoolback.util.assembler.impl.EnrollmentHistoryAssembler;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class CoinTransactionController {

    private final UserService userService;
    private final EnrollmentHistoryService enrollmentHistoryService;
    private final EnrollmentHistoryAssembler enrollmentHistoryAssembler;

    public CoinTransactionController(
            UserService userService,
            EnrollmentHistoryService enrollmentHistoryService,
            EnrollmentHistoryAssembler enrollmentHistoryAssembler
    ) {
        this.userService = userService;
        this.enrollmentHistoryService = enrollmentHistoryService;
        this.enrollmentHistoryAssembler = enrollmentHistoryAssembler;
    }

    @PostMapping("/api/coin-transactions")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Начислить коины ученику")
    public ResponseEntity<Void> createCoinTransaction(
            @RequestBody @Valid CreateCoinTransactionRequest request
    ) {
        userService.addCoins(request.studentId(), request.amount(), request.description());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/api/coin-transactions/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    @Operation(summary = "Получить информацию о истории начисления коинов для пользователя по его id")
    public Page<EnrollmentHistoryDTO> getCoinsHistoryById(
            @PathVariable Long studentId,
            @Parameter(description = "Номер страницы") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "enrollmentDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        final Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy)
        );
        final User user = userService.getUserById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("User with id " + studentId + " not found"));

        return enrollmentHistoryService.getEnrollmentHistoryByUser(user, pageable)
                .map(enrollmentHistoryAssembler::toDto);
    }

    @GetMapping("/api/coin-transactions/issued")
    @PreAuthorize("hasAnyRole('TEACHER')")
    @Operation(summary = "Получить историю всех начислений коинов текущим преподавателем")
    public Page<EnrollmentHistoryDTO> getAllCoinsHistory(
            @Parameter(description = "Номер страницы") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "enrollmentDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        final Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy)
        );
        final User currentTeacher = userService.getCurrentUser()
                .orElseThrow(() -> new IllegalArgumentException("Current teacher not found"));

        return enrollmentHistoryService.getEnrollmentHistoryByTeacherSortedByDateDesc(currentTeacher, pageable)
                .map(enrollmentHistoryAssembler::toDto);
    }
}
