package com.trackflow.entity;

/**
 * Enum representing the priority level of an Issue.
 *
 * <p>Priority determines the urgency of work:</p>
 * <ul>
 *   <li><strong>LOW</strong> — Can be done when convenient</li>
 *   <li><strong>MEDIUM</strong> — Should be done in the current sprint</li>
 *   <li><strong>HIGH</strong> — Must be addressed soon</li>
 *   <li><strong>CRITICAL</strong> — Production blocker, needs immediate attention</li>
 * </ul>
 *
 * <p>Used in Sprint Health Score calculations — CRITICAL bugs
 * heavily reduce the health score.</p>
 */
public enum IssuePriority {

    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
