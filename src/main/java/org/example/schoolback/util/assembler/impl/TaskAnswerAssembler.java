package org.example.schoolback.util.assembler.impl;

import org.example.schoolback.dto.TaskAnswerDTO;
import org.example.schoolback.entity.TaskAnswer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TaskAnswerAssembler {

    public TaskAnswerDTO toDto(TaskAnswer answer) {
        return new TaskAnswerDTO(
                answer.getId(),
                answer.getTask().getId(),
                answer.getStudent().getId(),
                answer.getStudent().getFullName(),
                answer.getAttempt(),
                answer.getAnswerText(),
                answer.getStatus(),
                answer.getTeacherComment(),
                answer.getSubmittedAt(),
                answer.getReviewedAt()
        );
    }

    public List<TaskAnswerDTO> toDtoList(List<TaskAnswer> answers) {
        return answers.stream().map(this::toDto).collect(Collectors.toList());
    }
}
