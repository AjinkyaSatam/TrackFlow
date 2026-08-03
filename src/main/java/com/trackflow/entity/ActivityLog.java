package com.trackflow.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * ActivityLog entity for recording every important action in the system.
 *
 * <p>This is the audit trail of TrackFlow. Every significant action
 * (issue created, status changed, comment added, etc.) is recorded here.</p>
 *
 * <h3>Difference from Notifications:</h3>
 * <ul>
 *   <li><strong>Notifications</strong> are user-facing alerts ("You have a new assignment")</li>
 *   <li><strong>Activity Logs</strong> are system records ("User X assigned issue Y to User Z at 3:42 PM")</li>
 * </ul>
 *
 * <p>Activity logs are used for:</p>
 * <ul>
 *   <li>Issue timeline ("Show me everything that happened on this issue")</li>
 *   <li>Project activity feed</li>
 *   <li>Analytics and metrics calculation</li>
 *   <li>Compliance and audit requirements</li>
 * </ul>
 *
 * <h3>Interview Question:</h3>
 * <p>"How would you implement an audit log in a Spring Boot application?"</p>
 * <p>Answer: Create an ActivityLog entity with who, what, when, and where.
 * Use the service layer to log activities after successful operations.
 * For more advanced cases, use Hibernate Envers or Spring AOP.</p>
 */
@Entity
@Table(name = "activity_logs", indexes = {
        @Index(name = "idx_activity_project", columnList = "project_id"),
        @Index(name = "idx_activity_issue", columnList = "issue_id"),
        @Index(name = "idx_activity_user", columnList = "user_id"),
        @Index(name = "idx_activity_type", columnList = "activity_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Type of activity that occurred */
    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 30)
    private ActivityType activityType;

    /** Human-readable description (e.g., "John assigned TF-42 to Jane") */
    @Column(nullable = false, length = 500)
    private String description;

    /** Old value before the change (e.g., "OPEN") — nullable for create actions */
    @Column(name = "old_value", length = 255)
    private String oldValue;

    /** New value after the change (e.g., "IN_PROGRESS") — nullable for delete actions */
    @Column(name = "new_value", length = 255)
    private String newValue;

    /** The user who performed this action */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** The project where this activity occurred */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    /** The issue related to this activity (nullable for non-issue activities) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id")
    private Issue issue;
}
