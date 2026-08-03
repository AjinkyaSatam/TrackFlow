package com.trackflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Sprint entity representing a time-boxed iteration within a Project.
 *
 * <p>Sprints are the heartbeat of agile development. Each sprint is a
 * fixed-duration period (usually 1-4 weeks) where a set of issues
 * are committed to be completed.</p>
 *
 * <h3>Business Rules (enforced in SprintService):</h3>
 * <ul>
 *   <li>End date must be after start date</li>
 *   <li>Only ONE sprint per project can be ACTIVE at a time</li>
 *   <li>Cannot start a sprint if another is already active</li>
 *   <li>Cannot add issues to a COMPLETED sprint</li>
 * </ul>
 *
 * <h3>Interview Question:</h3>
 * <p>"Where should business rules be enforced — entity, service, or database?"</p>
 * <p>Answer: All three layers, with increasing strictness:</p>
 * <ol>
 *   <li><strong>Entity:</strong> Basic constraints (@NotNull, @Column(nullable=false))</li>
 *   <li><strong>Service:</strong> Complex rules (only one active sprint per project)</li>
 *   <li><strong>Database:</strong> Ultimate safety net (unique constraints, check constraints)</li>
 * </ol>
 */
@Entity
@Table(name = "sprints", indexes = {
        @Index(name = "idx_sprint_project", columnList = "project_id"),
        @Index(name = "idx_sprint_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sprint extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Sprint name (e.g., "Sprint 1", "Release 2.0 Sprint") */
    @Column(nullable = false, length = 100)
    private String name;

    /** Sprint goal — what the team aims to achieve */
    @Column(length = 500)
    private String goal;

    /** Sprint start date */
    @Column(name = "start_date")
    private LocalDate startDate;

    /** Sprint end date */
    @Column(name = "end_date")
    private LocalDate endDate;

    /** Current sprint status */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SprintStatus status = SprintStatus.PLANNING;

    /** The project this sprint belongs to */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    /** Issues assigned to this sprint */
    @OneToMany(mappedBy = "sprint", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Issue> issues = new ArrayList<>();
}
