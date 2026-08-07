package com.trackflow.mapper;

import com.trackflow.dto.CommentDTO;
import com.trackflow.entity.Comment;
import org.springframework.stereotype.Component;

/**
 * Mapper utility to translate between Comment entity and CommentDTO.
 */
@Component
public class CommentMapper {

    public CommentDTO toDTO(Comment comment) {
        if (comment == null) {
            return null;
        }

        Long authorId = null;
        String authorName = null;
        String profileImage = null;

        if (comment.getAuthor() != null) {
            authorId = comment.getAuthor().getId();
            authorName = comment.getAuthor().getFullName();
            profileImage = comment.getAuthor().getProfileImage();
        }

        return CommentDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorId(authorId)
                .authorName(authorName)
                .authorProfileImage(profileImage)
                .issueId(comment.getIssue() != null ? comment.getIssue().getId() : null)
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
