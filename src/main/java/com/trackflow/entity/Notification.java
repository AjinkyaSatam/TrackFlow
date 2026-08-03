package com.trackflow.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Notification entity for user-facing alerts and messages.
 *
 * <p>Notifications are created when events occur (issue assigned,
 * comment added, deadline approaching, etc.). Users can mark them as read.</p>
 *
 * <h3>Design Decisions:</h3>
 * <ul>
 *   <li><strong>Recipient only:</strong> Each notification belongs to one user.
 *       If 5 users need to be notified, 5 notification records are created.
 *       This allows per-user read/unread tracking.</li>
 *   <li><strong>Reference fields:</strong> {@code referenceId} and {@code referenceType}
 *       link the notification to the related entity (e.g., issue ID).
 *       This avoids separate foreign keys for every possible entity type.</li>
 * </ul>
 */
@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notification_recipient", columnList = "recipient_id"),
        @Index(name = "idx_notification_read", columnList = "is_read")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Notification title (e.g., "Issue Assigned to You") */
    @Column(nullable = false, length = 200)
    private String title;

    /** Detailed notification message */
    @Column(nullable = false, length = 500)
    private String message;

    /** Type of notification event */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    /** Whether the user has read this notification */
    @Column(name = "is_read", nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private boolean read = false;

    /** ID of the related entity (e.g., issue ID, sprint ID) */
    @Column(name = "reference_id")
    private Long referenceId;

    /** Type of the related entity (e.g., "ISSUE", "SPRINT", "COMMENT") */
    @Column(name = "reference_type", length = 30)
    private String referenceType;

    /** The user who receives this notification */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    /** The user who triggered the notification (nullable for system notifications) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "triggered_by")
    private User triggeredBy;
}
