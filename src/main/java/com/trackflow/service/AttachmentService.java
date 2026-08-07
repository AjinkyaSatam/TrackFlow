package com.trackflow.service;

import com.trackflow.dto.AttachmentDTO;
import com.trackflow.entity.User;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service interface defining file metadata and upload operations.
 */
public interface AttachmentService {

    AttachmentDTO uploadAttachment(Long issueId, MultipartFile file, User uploader);

    void deleteAttachment(Long id, User user);

    List<AttachmentDTO> getAttachmentsByIssue(Long issueId);

    Resource downloadAttachment(Long id);
}
