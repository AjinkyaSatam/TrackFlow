package com.trackflow.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standardized API response wrapper for all REST endpoints.
 *
 * <p>Every API response in TrackFlow follows this consistent structure,
 * making it predictable for frontend consumers and simplifying error handling.</p>
 *
 * <h3>Why use a consistent response wrapper?</h3>
 * <ul>
 *   <li>Frontend developers always know where to find data, messages, and errors</li>
 *   <li>Error handling becomes uniform — no guessing the response shape</li>
 *   <li>Metadata like timestamps and status codes aid debugging</li>
 *   <li>Pagination info can be included via the generic data field</li>
 * </ul>
 *
 * <h3>Interview Tip:</h3>
 * <p>This is an application of the <strong>Envelope Pattern</strong>.
 * Interviewers love asking: "How do you ensure consistent API responses?"
 * This class is your answer.</p>
 *
 * @param <T> The type of data payload
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)  // Don't serialize null fields
public class ApiResponse<T> {

    /** Indicates whether the request was successful */
    private boolean success;

    /** HTTP status code */
    private int status;

    /** Human-readable message */
    private String message;

    /** The actual response payload (nullable for error responses) */
    private T data;

    /** Timestamp of the response */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /** Validation errors or additional error details (nullable for success responses) */
    private Object errors;

    // ---------------------------------------------------------------
    // Factory Methods — Clean API for creating responses
    // ---------------------------------------------------------------

    /**
     * Creates a successful response with data.
     *
     * @param data    The response payload
     * @param message Success message
     * @param <T>     Type of data
     * @return ApiResponse with success=true
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .status(200)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a successful response with data and custom status code.
     */
    public static <T> ApiResponse<T> success(T data, String message, int status) {
        return ApiResponse.<T>builder()
                .success(true)
                .status(status)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a successful response with only a message (no data payload).
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .status(200)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a 201 Created response.
     */
    public static <T> ApiResponse<T> created(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .status(201)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates an error response.
     *
     * @param status  HTTP status code
     * @param message Error message
     * @param <T>     Type parameter (usually Void for errors)
     * @return ApiResponse with success=false
     */
    public static <T> ApiResponse<T> error(int status, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .status(status)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates an error response with detailed error information.
     */
    public static <T> ApiResponse<T> error(int status, String message, Object errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .status(status)
                .message(message)
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
