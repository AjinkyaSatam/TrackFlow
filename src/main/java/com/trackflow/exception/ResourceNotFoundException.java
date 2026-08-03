package com.trackflow.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a requested resource is not found.
 *
 * <p>This is the most common custom exception in any CRUD application.
 * The {@code @ResponseStatus} annotation ensures Spring automatically
 * returns a 404 status code when this exception is thrown.</p>
 *
 * <h3>Why not just return null?</h3>
 * <p>Returning null forces the caller to check for it everywhere.
 * Throwing an exception centralizes error handling and makes the
 * code more expressive: "This resource MUST exist."</p>
 *
 * <h3>Interview Question:</h3>
 * <p>"How do you handle 'not found' scenarios in Spring Boot?"</p>
 * <p>Answer: Create a custom ResourceNotFoundException, annotate it with
 * {@code @ResponseStatus(HttpStatus.NOT_FOUND)}, and handle it in a
 * {@code @ControllerAdvice} for consistent error responses.</p>
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;

    /**
     * @param resourceName Name of the entity (e.g., "User", "Project")
     * @param fieldName    Field used for lookup (e.g., "id", "email")
     * @param fieldValue   The value that was searched for
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getFieldValue() {
        return fieldValue;
    }
}
