package com.trackflow.entity;

/**
 * Enum representing user roles in TrackFlow.
 *
 * <p>Roles follow a hierarchy of permissions:</p>
 * <ol>
 *   <li><strong>SUPER_ADMIN</strong> — Full system access. Can manage all organizations.</li>
 *   <li><strong>ORG_ADMIN</strong> — Manages one organization: users, projects, settings.</li>
 *   <li><strong>PROJECT_MANAGER</strong> — Manages sprints, assigns issues, views analytics.</li>
 *   <li><strong>DEVELOPER</strong> — Works on issues, updates status, adds comments.</li>
 *   <li><strong>TESTER</strong> — Creates bug reports, verifies fixes, updates issue status.</li>
 * </ol>
 *
 * <h3>Why use an enum instead of a database table for roles?</h3>
 * <p>For a fixed set of roles like this, an enum is simpler and type-safe.
 * The compiler catches typos (e.g., "DEVLOPER" would be a compile error).
 * If you need dynamic roles (e.g., custom roles created at runtime),
 * then a database table is better.</p>
 *
 * <h3>How JPA stores enums:</h3>
 * <ul>
 *   <li>{@code @Enumerated(EnumType.STRING)} — Stores "DEVELOPER" as text (recommended)</li>
 *   <li>{@code @Enumerated(EnumType.ORDINAL)} — Stores 3 as integer (fragile — adding
 *       a new enum value shifts all ordinals!)</li>
 * </ul>
 *
 * <h3>Interview Question:</h3>
 * <p>"Why use EnumType.STRING over EnumType.ORDINAL?"</p>
 * <p>Answer: ORDINAL stores the index (0, 1, 2...). If you reorder enums or
 * add one in the middle, existing database values become wrong.
 * STRING stores the actual name, which is safe against reordering.</p>
 */
public enum Role {

    /**
     * Full system administrator. Can manage all organizations and users.
     * Typically only 1-2 users have this role.
     */
    SUPER_ADMIN,

    /**
     * Organization administrator. Can manage users and projects
     * within their organization.
     */
    ORG_ADMIN,

    /**
     * Project manager. Can create sprints, assign issues,
     * and view project analytics.
     */
    PROJECT_MANAGER,

    /**
     * Software developer. Can work on assigned issues,
     * update status, and add comments.
     */
    DEVELOPER,

    /**
     * Quality assurance tester. Can create bug reports,
     * verify fixes, and update issue status.
     */
    TESTER
}
