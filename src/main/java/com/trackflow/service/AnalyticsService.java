package com.trackflow.service;

import com.trackflow.dto.SprintHealthDTO;
import com.trackflow.dto.WorkloadSuggestionDTO;

import java.util.List;

/**
 * Service interface defining algorithms for workload balancing and sprint health monitoring.
 */
public interface AnalyticsService {

    List<WorkloadSuggestionDTO> getWorkloadSuggestions(Long projectId);

    SprintHealthDTO getSprintHealth(Long sprintId);
}
