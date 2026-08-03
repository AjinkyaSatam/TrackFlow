package com.trackflow.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a user attempts to create a resource that already exists.
 *
 * <p>Common scenarios:</p>
 * <ul>
 *   <li>Registering with an email that's already taken</li>
 *   <li>Creating a project with a duplicate name in the same organization</li>
 *   <li>Adding a member who is already part of the project</li>
 * </ul>
 *
 * <h3>Why 409 Conflict?</h3>
 * <p>HTTP 409 means "the request conflicts with the current state of the server."
 * This is more semantically correct than 400 Bad Request for duplicate resources.</p>
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateResourceException extends RuntimeException {

    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;

    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s already exists with %s: '%s'", resourceName, fieldName, fieldValue));
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
