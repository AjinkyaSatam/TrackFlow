package com.trackflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Issue entity — the core of TrackFlow.
 *
 * <p>An issue represents a unit of work: a bug to fix, a feature to build,
 * a task to complete. This is the entity everything revolves around.</p>
 *
 * <h3>Design Decisions:</h3>
 * <ul>
 *   <li><strong>Reporter vs Assignee:</strong> Reporter is who created the issue.
 *       Assignee is who is working on it. They can be different people.</li>
 *   <li><strong>Labels as comma-separated string:</strong> For simplicity, labels are
 *       stored as a comma-separated string ("backend,urgent,api"). In a more complex
 *       system, you'd use a separate Label entity with a ManyToMany relationship.</li>
 *   <li><strong>Estimated hours:</strong> Used by the Workload Balancer to calculate
 *       developer capacity and suggest assignments.</li>
 *   <li><strong>Issue key:</strong> A human-readable identifier like "TF-42".
 *       Composed of the project key + auto-incremented number.</li>
 * </ul>
 *
 * <h3>Interview Question:</h3>
 * <p>"How would you design an issue tracking system's data model?"</p>
 * <p>Answer: Discuss this entity — the relationships to User (reporter/assignee),
 * Project, Sprint, the status workflow, and how labels/priority/type are modeled.
 * Mention the trade-off of labels as strings vs a separate entity.</p>
 */
@Entity
@Table(name = "issues", indexes = {
        @Index(name = "idx_issue_project", columnList = "project_id"),
        @Index(name = "idx_issue_sprint", columnList = "sprint_id"),
        @Index(name = "idx_issue_assignee", columnList = "assignee_id"),
        @Index(name = "idx_issue_status", columnList = "status"),
        @Index(name = "idx_issue_priority", columnList = "priority"),
        @Index(name = "idx_issue_key", columnList = "issue_key", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Issue extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Human-readable issue key (e.g., "TF-42").
     * Generated from project key + sequence number.
     */
    @Column(name = "issue_key", nullable = false, unique = true, length = 20)
    private String issueKey;

    /** Issue title (short summary) */
    @Column(nullable = false, length = 200)
    private String title;

    /** Detailed description of the issue */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** Type of issue (BUG, FEATURE, TASK, STORY, IMPROVEMENT) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IssueType type;

    /** Priority level (LOW, MEDIUM, HIGH, CRITICAL) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private IssuePriority priority = IssuePriority.MEDIUM;

    /** Current workflow status (OPEN → IN_PROGRESS → CODE_REVIEW → TESTING → DONE) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private IssueStatus status = IssueStatus.OPEN;

    /** Due date for this issue */
    @Column(name = "due_date")
    private LocalDate dueDate;

    /** Estimated hours to complete this issue (used for workload calculations) */
    @Column(name = "estimated_hours")
    private Double estimatedHours;

    /** Comma-separated labels (e.g., "backend,api,urgent") */
    @Column(length = 500)
    private String labels;

    /** The user who created/reported this issue */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    /** The user assigned to work on this issue (nullable — unassigned issues) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    /** The project this issue belongs to */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    /** The sprint this issue is assigned to (nullable — backlog issues) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_id")
    private Sprint sprint;

    /** Comments on this issue */
    @OneToMany(mappedBy = "issue", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    /** File attachments on this issue */
    @OneToMany(mappedBy = "issue", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Attachment> attachments = new ArrayList<>();
}
