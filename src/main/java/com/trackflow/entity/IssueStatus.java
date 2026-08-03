package com.trackflow.entity;

import java.util.List;
import java.util.Map;

/**
 * Enum representing the workflow status of an Issue.
 *
 * <p>Issues follow a defined workflow with valid transitions:</p>
 * <pre>
 *   OPEN → IN_PROGRESS → CODE_REVIEW → TESTING → DONE
 *                ↑              |           |
 *                └──────────────┘           |
 *                ↑                          |
 *                └──────────────────────────┘
 * </pre>
 *
 * <p>Only valid transitions are allowed. For example, you CANNOT go from
 * OPEN directly to DONE — the issue must pass through the workflow.</p>
 *
 * <h3>Why encode transitions in the enum?</h3>
 * <p>This is the <strong>State Pattern</strong>. Each state knows which
 * states it can transition to. This keeps the workflow logic in one place
 * instead of scattered across service methods with if-else chains.</p>
 *
 * <h3>Interview Question:</h3>
 * <p>"How would you implement a state machine for issue tracking?"</p>
 * <p>Answer: Define valid transitions in the enum itself. Each status
 * has a list of allowed next statuses. The service layer calls
 * {@code canTransitionTo()} before changing status, throwing a
 * BadRequestException if the transition is invalid.</p>
 */
public enum IssueStatus {

    /** Issue has been created but work hasn't started */
    OPEN,

    /** Developer is actively working on this issue */
    IN_PROGRESS,

    /** Code is written and submitted for peer review */
    CODE_REVIEW,

    /** Code is approved and being tested by QA */
    TESTING,

    /** Issue is resolved and verified */
    DONE;

    /**
     * Map of valid state transitions.
     *
     * <p>Each status maps to a list of statuses it CAN transition to.
     * Any transition not in this map is INVALID.</p>
     */
    private static final Map<IssueStatus, List<IssueStatus>> VALID_TRANSITIONS = Map.of(
            OPEN, List.of(IN_PROGRESS),
            IN_PROGRESS, List.of(CODE_REVIEW, OPEN),            // Can go back to OPEN (blocked/paused)
            CODE_REVIEW, List.of(TESTING, IN_PROGRESS),         // Can go back to IN_PROGRESS (review failed)
            TESTING, List.of(DONE, IN_PROGRESS),                // Can go back to IN_PROGRESS (bugs found)
            DONE, List.of(OPEN)                                 // Can reopen if issue resurfaces
    );

    /**
     * Checks if transitioning from this status to the target status is allowed.
     *
     * @param target The desired next status
     * @return true if the transition is valid
     */
    public boolean canTransitionTo(IssueStatus target) {
        List<IssueStatus> allowed = VALID_TRANSITIONS.get(this);
        return allowed != null && allowed.contains(target);
    }

    /**
     * Returns the list of valid next statuses from the current status.
     *
     * @return List of statuses this status can transition to
     */
    public List<IssueStatus> getValidTransitions() {
        return VALID_TRANSITIONS.getOrDefault(this, List.of());
    }
}
