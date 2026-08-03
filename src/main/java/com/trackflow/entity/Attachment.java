package com.trackflow.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Attachment entity — stores metadata about uploaded files.
 *
 * <p><strong>Key principle:</strong> Only METADATA is stored in the database.
 * The actual file is stored via {@code StorageService} (local disk, S3, MinIO).
 * This entity stores the file path, name, size, and content type.</p>
 *
 * <h3>Why separate metadata from file storage?</h3>
 * <ul>
 *   <li>Database stays fast (no BLOBs bloating table size)</li>
 *   <li>Files can be served via CDN for better performance</li>
 *   <li>Storage backend can be swapped (local → S3) without touching the DB</li>
 *   <li>Backups are smaller and faster</li>
 * </ul>
 *
 * <h3>Interview Question:</h3>
 * <p>"How do you handle file uploads in a Spring Boot application?"</p>
 * <p>Answer: Use MultipartFile to receive the upload, validate type & size,
 * save the file via a StorageService (Strategy Pattern), and store only
 * the metadata (path, name, size) in the database.</p>
 */
@Entity
@Table(name = "attachments", indexes = {
        @Index(name = "idx_attachment_issue", columnList = "issue_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attachment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Original filename as uploaded by the user */
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    /** Stored file path (relative path in StorageService) */
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    /** File MIME type (e.g., "image/png", "application/pdf") */
    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    /** File size in bytes */
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    /** The user who uploaded this attachment */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    /** The issue this attachment belongs to */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id", nullable = false)
    private Issue issue;
}
