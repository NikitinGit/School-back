package org.example.schoolback.util.assembler.impl;

import org.example.schoolback.dto.ProjectCommentDTO;
import org.example.schoolback.dto.request.CreateCommentRequest;
import org.example.schoolback.entity.Project;
import org.example.schoolback.entity.ProjectComment;
import org.example.schoolback.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProjectCommentAssembler {

    public ProjectCommentDTO toDto(ProjectComment comment) {
        return new ProjectCommentDTO(
                comment.getId(),
                comment.getProject().getId(),
                comment.getAuthor().getId(),
                comment.getAuthor().getFullName(),
                comment.getText(),
                comment.getCreatedAt()
        );
    }

    public List<ProjectCommentDTO> toDtoList(List<ProjectComment> comments) {
        return comments.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public ProjectComment toEntity(CreateCommentRequest request, Project project, User author) {
        ProjectComment comment = new ProjectComment();
        comment.setProject(project);
        comment.setAuthor(author);
        comment.setText(request.text());
        comment.setCreatedAt(LocalDateTime.now());
        return comment;
    }
}
