package com.trackflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Organization entity representing a company or team in TrackFlow.
 *
 * <p>Organizations are the top-level container in the hierarchy:</p>
 * <pre>
 * Organization
 *   └── Project(s)
 *         └── Sprint(s)
 *               └── Issue(s)
 * </pre>
 *
 * <h3>Design Decisions:</h3>
 * <ul>
 *   <li><strong>One user = one organization:</strong> A user belongs to exactly one
 *       organization. This simplifies permission checking.</li>
 *   <li><strong>Owner field:</strong> The user who created the organization.
 *       This is separate from ORG_ADMIN role — an org can have multiple admins
 *       but only one owner.</li>
 *   <li><strong>mappedBy:</strong> The Organization does NOT own the relationship.
 *       The "organization_id" foreign key lives in the users table.
 *       Organization just provides a convenient way to access its members.</li>
 * </ul>
 *
 * <h3>Interview Question:</h3>
 * <p>"What does 'mappedBy' mean in JPA?"</p>
 * <p>Answer: It tells JPA that the OTHER side of the relationship owns the
 * foreign key. Here, {@code mappedBy = "organization"} means the User entity
 * has the {@code @JoinColumn} with "organization_id". Without mappedBy,
 * JPA would create a join table, which is unnecessary for @OneToMany.</p>
 */
@Entity
@Table(name = "organizations", indexes = {
        @Index(name = "idx_org_name", columnList = "name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Organization extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Organization name (e.g., "Acme Corp", "StartupXYZ").
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Brief description of the organization.
     */
    @Column(length = 500)
    private String description;

    /**
     * Organization's website URL.
     */
    @Column(length = 255)
    private String website;

    /**
     * Organization logo file path (stored via StorageService).
     */
    @Column(name = "logo_url")
    private String logoUrl;

    /**
     * The user who created/owns this organization.
     *
     * <p>{@code @OneToOne} because each organization has exactly one owner.
     * {@code FetchType.LAZY} to avoid unnecessary loading.</p>
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    /**
     * List of users belonging to this organization.
     *
     * <p>{@code mappedBy = "organization"} means the User entity owns this relationship
     * (User has the foreign key column "organization_id").
     * We don't use {@code CascadeType.ALL} here because deleting an organization
     * should NOT automatically delete all users — that's a business decision
     * handled in the service layer.</p>
     *
     * <p>Initialize with {@code new ArrayList<>()} to avoid NullPointerException
     * when calling {@code organization.getMembers()} on a new organization.</p>
     */
    @OneToMany(mappedBy = "organization", fetch = FetchType.LAZY)
    @Builder.Default
    private List<User> members = new ArrayList<>();
}
