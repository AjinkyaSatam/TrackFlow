package com.trackflow.mapper;

import com.trackflow.dto.AttachmentDTO;
import com.trackflow.entity.Attachment;
import org.springframework.stereotype.Component;

/**
 * Mapper utility to translate between Attachment entity and AttachmentDTO.
 */
@Component
public class AttachmentMapper {

    public AttachmentDTO toDTO(Attachment attachment) {
        if (attachment == null) {
            return null;
        }

        Long uploadedById = null;
        String uploadedByName = null;

        if (attachment.getUploadedBy() != null) {
            uploadedById = attachment.getUploadedBy().getId();
            uploadedByName = attachment.getUploadedBy().getFullName();
        }

        return AttachmentDTO.builder()
                .id(attachment.getId())
                .fileName(attachment.getFileName())
                .filePath(attachment.getFilePath())
                .contentType(attachment.getContentType())
                .fileSize(attachment.getFileSize())
                .uploadedById(uploadedById)
                .uploadedByName(uploadedByName)
                .issueId(attachment.getIssue() != null ? attachment.getIssue().getId() : null)
                .createdAt(attachment.getCreatedAt())
                .build();
    }
}
