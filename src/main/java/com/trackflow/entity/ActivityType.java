package com.trackflow.entity;

/**
 * Enum representing types of activities recorded in the Activity Log.
 *
 * <p>Every important action in TrackFlow is recorded for audit trail purposes.
 * This enum categorizes those actions.</p>
 *
 * <p><strong>Why log activities?</strong></p>
 * <ul>
 *   <li>Audit compliance — "Who changed what and when?"</li>
 *   <li>User timeline — Show recent activity on an issue or project</li>
 *   <li>Debugging — Trace back to understand how a state was reached</li>
 *   <li>Analytics — Calculate metrics like average resolution time</li>
 * </ul>
 */
public enum ActivityType {

    // Issue activities
    ISSUE_CREATED,
    ISSUE_UPDATED,
    ISSUE_ASSIGNED,
    ISSUE_STATUS_CHANGED,
    ISSUE_CLOSED,

    // Comment activities
    COMMENT_ADDED,
    COMMENT_UPDATED,
    COMMENT_DELETED,

    // Attachment activities
    ATTACHMENT_UPLOADED,
    ATTACHMENT_DELETED,

    // Sprint activities
    SPRINT_CREATED,
    SPRINT_STARTED,
    SPRINT_COMPLETED,

    // Project activities
    PROJECT_CREATED,
    PROJECT_UPDATED,
    PROJECT_MEMBER_ADDED,
    PROJECT_MEMBER_REMOVED,

    // User activities
    USER_REGISTERED,
    USER_UPDATED
}
