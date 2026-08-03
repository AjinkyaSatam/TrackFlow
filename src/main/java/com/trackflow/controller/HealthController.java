package com.trackflow.controller;

import com.trackflow.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Health Check Controller.
 *
 * <p>Provides a simple endpoint to verify the application is running.
 * This is essential for:</p>
 * <ul>
 *   <li>Docker health checks</li>
 *   <li>Load balancer probes</li>
 *   <li>Monitoring systems (Prometheus, Grafana)</li>
 *   <li>Quick smoke testing after deployment</li>
 * </ul>
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    /**
     * Returns the application health status.
     *
     * @return ApiResponse with status "UP"
     */
    @GetMapping
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(
                ApiResponse.success("TrackFlow is running!", "Health check passed")
        );
    }
}
