package com.trackflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Project entity representing a software project within an Organization.
 *
 * <p>Hierarchy: Organization → Project → Sprint → Issue</p>
 *
 * <h3>Design Decisions:</h3>
 * <ul>
 *   <li><strong>Project belongs to an Organization:</strong> {@code @ManyToOne} because
 *       one organization can have many projects.</li>
 *   <li><strong>Members are managed via ProjectMember:</strong> We use a join entity
 *       instead of {@code @ManyToMany} because the membership has extra data
 *       (role within the project, joined date). A raw @ManyToMany can't store
 *       attributes on the relationship.</li>
 * </ul>
 *
 * <h3>Interview Question:</h3>
 * <p>"When would you use a join entity instead of @ManyToMany?"</p>
 * <p>Answer: When the relationship itself has attributes. For example,
 * a project member has a "role" and "joinedAt" date — these belong to
 * the relationship, not to the User or Project. A join entity
 * (ProjectMember) models this cleanly.</p>
 */
@Entity
@Table(name = "projects", indexes = {
        @Index(name = "idx_project_org", columnList = "organization_id"),
        @Index(name = "idx_project_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Project name (e.g., "TrackFlow Backend", "Mobile App") */
    @Column(nullable = false, length = 100)
    private String name;

    /** Detailed description of the project */
    @Column(length = 1000)
    private String description;

    /** Project deadline */
    @Column(name = "deadline")
    private LocalDate deadline;

    /** Current status of the project */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ProjectStatus status = ProjectStatus.PLANNING;

    /** URL of the code repository (GitHub, GitLab, etc.) */
    @Column(name = "repository_url", length = 255)
    private String repositoryUrl;

    /** Project key prefix for issue IDs (e.g., "TF" for TF-001) */
    @Column(name = "project_key", nullable = false, unique = true, length = 10)
    private String projectKey;

    /** The organization this project belongs to */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    /** The user who created this project */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    /** Project members via join entity */
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProjectMember> members = new ArrayList<>();

    /** Sprints within this project */
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Sprint> sprints = new ArrayList<>();
}
