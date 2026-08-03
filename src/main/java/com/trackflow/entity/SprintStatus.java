package com.trackflow.entity;

/**
 * Enum representing the lifecycle status of a Sprint.
 *
 * <p>Sprints follow a strict linear lifecycle:</p>
 * <pre>
 * PLANNING → ACTIVE → COMPLETED
 * </pre>
 *
 * <p><strong>Business Rule:</strong> Only ONE sprint per project can be ACTIVE at a time.
 * This will be enforced in the SprintService.</p>
 */
public enum SprintStatus {

    /** Sprint is being planned, issues are being added */
    PLANNING,

    /** Sprint is currently in progress */
    ACTIVE,

    /** Sprint is finished */
    COMPLETED
}
