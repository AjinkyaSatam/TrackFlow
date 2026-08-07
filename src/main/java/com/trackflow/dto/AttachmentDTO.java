package com.trackflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object representing an uploaded file's metadata.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentDTO {

    private Long id;
    private String fileName;
    private String filePath;
    private String contentType;
    private Long fileSize;
    private Long uploadedById;
    private String uploadedByName;
    private Long issueId;
    private LocalDateTime createdAt;
}
