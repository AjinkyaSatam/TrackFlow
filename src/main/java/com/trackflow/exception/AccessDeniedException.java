package com.trackflow.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a user doesn't have permission to perform an action.
 *
 * <p>This is different from authentication (401 Unauthorized).
 * This exception means the user IS authenticated but LACKS the required role/permission.</p>
 *
 * <h3>Interview Question:</h3>
 * <p>"What's the difference between 401 and 403?"</p>
 * <ul>
 *   <li><strong>401 Unauthorized</strong>: "I don't know who you are" (not authenticated)</li>
 *   <li><strong>403 Forbidden</strong>: "I know who you are, but you can't do this" (not authorized)</li>
 * </ul>
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }

    public AccessDeniedException(String action, String resource) {
        super(String.format("You don't have permission to %s this %s", action, resource));
    }
}
