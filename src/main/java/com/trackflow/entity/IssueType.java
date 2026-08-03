package com.trackflow.entity;

/**
 * Enum representing the type/category of an Issue.
 *
 * <p>Issue types help teams categorize work items:</p>
 * <ul>
 *   <li><strong>BUG</strong> — Something is broken and needs fixing</li>
 *   <li><strong>FEATURE</strong> — New functionality to be implemented</li>
 *   <li><strong>TASK</strong> — General work item (setup, config, etc.)</li>
 *   <li><strong>STORY</strong> — User story describing a feature from user's perspective</li>
 *   <li><strong>IMPROVEMENT</strong> — Enhancement to existing functionality</li>
 * </ul>
 */
public enum IssueType {

    BUG,
    FEATURE,
    TASK,
    STORY,
    IMPROVEMENT
}
