package com.trackflow.service;

import com.trackflow.dto.CreateSprintRequest;
import com.trackflow.dto.SprintDTO;
import com.trackflow.dto.UpdateSprintStatusRequest;

import java.util.List;

/**
 * Service interface defining sprint workflow operations.
 */
public interface SprintService {

    SprintDTO createSprint(Long projectId, CreateSprintRequest request);

    SprintDTO getSprintById(Long id);

    List<SprintDTO> getSprintsByProject(Long projectId);

    SprintDTO updateSprintStatus(Long id, UpdateSprintStatusRequest request);
}
