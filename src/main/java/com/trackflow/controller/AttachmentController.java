package com.trackflow.controller;

import com.trackflow.dto.ApiResponse;
import com.trackflow.dto.AttachmentDTO;
import com.trackflow.entity.User;
import com.trackflow.exception.AccessDeniedException;
import com.trackflow.security.CustomUserDetails;
import com.trackflow.service.AttachmentService;
import com.trackflow.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST controller for file upload configurations and downloads.
 */
@RestController
@RequestMapping("/projects/{projectId}/issues/{issueId}/attachments")
@RequiredArgsConstructor
@Tag(name = "Attachments", description = "Endpoints for managing file attachments and screenshots")
@SecurityRequirement(name = "Bearer Authentication")
public class AttachmentController {

    private final AttachmentService attachmentService;
    private final ProjectService projectService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload file attachment", description = "Uploads a file (screenshot, log) to a specific issue.")
    public ResponseEntity<ApiResponse<AttachmentDTO>> uploadAttachment(
            @PathVariable Long projectId,
            @PathVariable Long issueId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails loggedInUser
    ) {
        validateProjectAccess(loggedInUser.getUser(), projectId);
        AttachmentDTO response = attachmentService.uploadAttachment(issueId, file, loggedInUser.getUser());
        return ResponseEntity.status(201).body(
                ApiResponse.created(response, "Attachment uploaded successfully")
        );
    }

    @GetMapping
    @Operation(summary = "Get issue attachments", description = "Retrieves a list of all attachments for an issue.")
    public ResponseEntity<ApiResponse<List<AttachmentDTO>>> getAttachments(
            @PathVariable Long projectId,
            @PathVariable Long issueId,
            @AuthenticationPrincipal CustomUserDetails loggedInUser
    ) {
        validateProjectAccess(loggedInUser.getUser(), projectId);
        List<AttachmentDTO> response = attachmentService.getAttachmentsByIssue(issueId);
        return ResponseEntity.ok(
                ApiResponse.success(response, "Attachments fetched successfully")
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete attachment", description = "Deletes an attachment. Restricted to owner or administrators.")
    public ResponseEntity<ApiResponse<String>> deleteAttachment(
            @PathVariable Long projectId,
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails loggedInUser
    ) {
        validateProjectAccess(loggedInUser.getUser(), projectId);
        attachmentService.deleteAttachment(id, loggedInUser.getUser());
        return ResponseEntity.ok(
                ApiResponse.success("Attachment deleted successfully")
        );
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Download attachment", description = "Downloads a file attachment.")
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable Long projectId,
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails loggedInUser
    ) {
        validateProjectAccess(loggedInUser.getUser(), projectId);
        Resource resource = attachmentService.downloadAttachment(id);
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    // -----------------------------------------------------------------
    // Access Validations
    // -----------------------------------------------------------------

    private void validateProjectAccess(User loggedInUser, Long projectId) {
        if (loggedInUser.getRole() == com.trackflow.entity.Role.SUPER_ADMIN) {
            return;
        }
        var project = projectService.getProjectById(projectId);
        if (loggedInUser.getOrganization() == null || !loggedInUser.getOrganization().getId().equals(project.getOrganizationId())) {
            throw new AccessDeniedException("Access Denied: You do not belong to this organization");
        }
    }
}
