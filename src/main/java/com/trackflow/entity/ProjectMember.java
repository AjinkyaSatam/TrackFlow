package com.trackflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Join entity representing a User's membership in a Project.
 *
 * <p>This is a <strong>join entity</strong> (also called an association entity).
 * Instead of using {@code @ManyToMany} between User and Project,
 * we create an explicit entity because the relationship has attributes:</p>
 * <ul>
 *   <li>{@code role} — What role does this user have in THIS project?</li>
 *   <li>{@code joinedAt} — When did they join this project?</li>
 * </ul>
 *
 * <p>With a raw {@code @ManyToMany}, JPA creates a join table with only
 * two columns (user_id, project_id). We can't add extra columns to it.
 * The join entity approach gives us full control.</p>
 *
 * <h3>Interview Question:</h3>
 * <p>"How do you model a many-to-many relationship with extra attributes?"</p>
 * <p>Answer: Break it into two @ManyToOne relationships with a join entity.
 * The join entity has its own @Id and any additional fields the relationship needs.</p>
 */
@Entity
@Table(name = "project_members", uniqueConstraints = {
        @UniqueConstraint(name = "uk_project_member", columnNames = {"project_id", "user_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The project this membership belongs to */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    /** The user who is a member of the project */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The user's role within this specific project.
     * A user might be a DEVELOPER in one project and a TESTER in another.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Role role;

    /** When the user joined this project */
    @Column(name = "joined_at", nullable = false)
    @Builder.Default
    private LocalDateTime joinedAt = LocalDateTime.now();
}
