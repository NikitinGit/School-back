package org.example.schoolback.service;

import org.example.schoolback.entity.EnrollmentHistory;
import org.example.schoolback.entity.User;
import org.example.schoolback.repository.EnrollmentHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EnrollmentHistoryService {

    @Autowired
    private EnrollmentHistoryRepository enrollmentHistoryRepository;

    public void saveEnrollmentHistory(User teacher, User student, Long coins) {
        saveEnrollmentHistory(teacher, student, coins, 0L, null);
    }

    /**
     * Запись о начислении с опытом и причиной: так выглядит начисление за принятую задачу.
     *
     * @param description за что начислено, например название задачи
     */
    public void saveEnrollmentHistory(User teacher, User student, Long coins, Long experience, String description) {
        EnrollmentHistory history = new EnrollmentHistory();
        history.setTeacher(teacher);
        history.setUser(student);
        history.setEnrolledCoins(coins);
        history.setEnrolledExp(experience == null ? 0L : experience);
        history.setDescription(description);
        history.setEnrollmentDate(LocalDateTime.now());

        enrollmentHistoryRepository.save(history);
    }

    public Page<EnrollmentHistory> getEnrollmentHistoryByUser(User user, Pageable pageable) {
        return enrollmentHistoryRepository.findByUser(user, pageable);
    }

    public Page<EnrollmentHistory> getEnrollmentHistoryByTeacherSortedByDateDesc(User teacher, Pageable pageable) {
        return enrollmentHistoryRepository.findByTeacher(teacher, pageable);
    }
}
