package com.trackflow.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a business rule is violated.
 *
 * <p>Examples:</p>
 * <ul>
 *   <li>Trying to close a sprint with unresolved critical bugs</li>
 *   <li>Attempting an invalid issue status transition (e.g., Open → Done)</li>
 *   <li>Assigning more issues to a developer who is at capacity</li>
 * </ul>
 *
 * <h3>Why a separate exception?</h3>
 * <p>Business rule violations are not the same as validation errors.
 * Validation checks the format of input data. Business rules check
 * whether the action makes sense in the domain context.</p>
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
