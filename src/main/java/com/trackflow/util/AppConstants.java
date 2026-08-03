package com.trackflow.util;

/**
 * Application-wide constants.
 *
 * <p>Centralizing constants prevents "magic strings" and "magic numbers"
 * scattered throughout the codebase. If a value needs to change,
 * you change it in one place.</p>
 *
 * <h3>Why not use an enum?</h3>
 * <p>Enums are for types with behavior. Constants are for static values
 * used across the application. Use enums for domain types (IssueStatus, Priority),
 * use constants for configuration defaults and repeated strings.</p>
 *
 * <h3>Interview Question:</h3>
 * <p>"How do you avoid magic numbers in Java?"</p>
 * <p>Answer: Use a constants class with public static final fields,
 * or use application.yml for externalized configuration.</p>
 */
public final class AppConstants {

    // Prevent instantiation
    private AppConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // ---------------------------------------------------------------
    // Pagination Defaults
    // ---------------------------------------------------------------
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "20";
    public static final String DEFAULT_SORT_BY = "createdAt";
    public static final String DEFAULT_SORT_DIR = "desc";
    public static final int MAX_PAGE_SIZE = 100;

    // ---------------------------------------------------------------
    // API Paths
    // ---------------------------------------------------------------
    public static final String AUTH_PATH = "/auth";
    public static final String USERS_PATH = "/users";
    public static final String ORGANIZATIONS_PATH = "/organizations";
    public static final String PROJECTS_PATH = "/projects";
    public static final String SPRINTS_PATH = "/sprints";
    public static final String ISSUES_PATH = "/issues";
    public static final String COMMENTS_PATH = "/comments";
    public static final String ATTACHMENTS_PATH = "/attachments";
    public static final String NOTIFICATIONS_PATH = "/notifications";

    // ---------------------------------------------------------------
    // Validation Messages
    // ---------------------------------------------------------------
    public static final String REQUIRED_FIELD = "This field is required";
    public static final String INVALID_EMAIL = "Please provide a valid email address";
    public static final String PASSWORD_MIN_LENGTH = "Password must be at least 8 characters";
    public static final String PASSWORD_MAX_LENGTH = "Password must be at most 128 characters";

    // ---------------------------------------------------------------
    // Response Messages
    // ---------------------------------------------------------------
    public static final String CREATED_SUCCESSFULLY = "%s created successfully";
    public static final String UPDATED_SUCCESSFULLY = "%s updated successfully";
    public static final String DELETED_SUCCESSFULLY = "%s deleted successfully";
    public static final String FETCHED_SUCCESSFULLY = "%s fetched successfully";
}
