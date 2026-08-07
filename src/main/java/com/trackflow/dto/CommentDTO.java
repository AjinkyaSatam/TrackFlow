package com.trackflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object representing a comment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {

    private Long id;
    private String content;
    private Long authorId;
    private String authorName;
    private String authorProfileImage;
    private Long issueId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
