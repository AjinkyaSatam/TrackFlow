package com.trackflow.entity;

/**
 * Enum representing the lifecycle status of a Project.
 *
 * <p>Projects follow this lifecycle:</p>
 * <pre>
 * PLANNING → ACTIVE → ON_HOLD (optional) → COMPLETED → ARCHIVED
 * </pre>
 */
public enum ProjectStatus {

    /** Project is being planned, not yet started */
    PLANNING,

    /** Project is actively being worked on */
    ACTIVE,

    /** Project is temporarily paused */
    ON_HOLD,

    /** Project work is finished */
    COMPLETED,

    /** Project is archived and read-only */
    ARCHIVED
}
