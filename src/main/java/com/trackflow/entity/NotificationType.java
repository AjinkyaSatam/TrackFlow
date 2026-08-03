package com.trackflow.entity;

/**
 * Enum representing types of notifications sent to users.
 *
 * <p>Each type corresponds to an event that triggers a notification:</p>
 * <ul>
 *   <li>ISSUE_ASSIGNED — "You've been assigned issue TF-42"</li>
 *   <li>ISSUE_STATUS_CHANGED — "Issue TF-42 moved to IN_PROGRESS"</li>
 *   <li>COMMENT_ADDED — "John commented on issue TF-42"</li>
 *   <li>MENTIONED — "@you was mentioned in a comment"</li>
 *   <li>DEADLINE_APPROACHING — "Issue TF-42 is due in 24 hours"</li>
 *   <li>SPRINT_STARTED — "Sprint 5 has started"</li>
 *   <li>SPRINT_COMPLETED — "Sprint 5 is completed"</li>
 * </ul>
 */
public enum NotificationType {

    ISSUE_ASSIGNED,
    ISSUE_STATUS_CHANGED,
    COMMENT_ADDED,
    MENTIONED,
    DEADLINE_APPROACHING,
    SPRINT_STARTED,
    SPRINT_COMPLETED
}
