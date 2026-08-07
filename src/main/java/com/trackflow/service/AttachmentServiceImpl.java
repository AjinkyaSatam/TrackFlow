package com.trackflow.service;

import com.trackflow.dto.AttachmentDTO;
import com.trackflow.entity.Attachment;
import com.trackflow.entity.Issue;
import com.trackflow.entity.Role;
import com.trackflow.entity.User;
import com.trackflow.exception.AccessDeniedException;
import com.trackflow.exception.ResourceNotFoundException;
import com.trackflow.mapper.AttachmentMapper;
import com.trackflow.repository.AttachmentRepository;
import com.trackflow.repository.IssueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service implementation providing File Attachment Management.
 */
@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final IssueRepository issueRepository;
    private final StorageService storageService;
    private final AttachmentMapper attachmentMapper;

    @Override
    @Transactional
    public AttachmentDTO uploadAttachment(Long issueId, MultipartFile file, User uploader) {
        Issue issue = issueRepository.findById(issueId)
                .filter(i -> !i.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Issue", "id", issueId));

        // 1. Save file to storage backend (Disk/S3)
        String storedPath = storageService.store(file);

        // 2. Save metadata record to DB
        Attachment attachment = Attachment.builder()
                .fileName(file.getOriginalFilename())
                .filePath(storedPath)
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .uploadedBy(uploader)
                .issue(issue)
                .build();

        Attachment savedAttachment = attachmentRepository.save(attachment);
        return attachmentMapper.toDTO(savedAttachment);
    }

    @Override
    @Transactional
    public void deleteAttachment(Long id, User user) {
        Attachment attachment = attachmentRepository.findById(id)
                .filter(a -> !a.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", "id", id));

        // Authorization validation (Author, Org Admin, Super Admin)
        boolean isOwner = attachment.getUploadedBy().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ORG_ADMIN || user.getRole() == Role.SUPER_ADMIN;

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You do not have permission to delete this attachment");
        }

        // 1. Delete physical file
        storageService.delete(attachment.getFilePath());

        // 2. Soft delete metadata record in DB
        attachment.setDeleted(true);
        attachmentRepository.save(attachment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttachmentDTO> getAttachmentsByIssue(Long issueId) {
        if (!issueRepository.existsById(issueId)) {
            throw new ResourceNotFoundException("Issue", "id", issueId);
        }
        return attachmentRepository.findByIssueIdAndIsDeletedFalse(issueId).stream()
                .map(attachmentMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Resource downloadAttachment(Long id) {
        Attachment attachment = attachmentRepository.findById(id)
                .filter(a -> !a.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", "id", id));

        return storageService.load(attachment.getFilePath());
    }
}
