package com.trackflow.service;

import com.trackflow.dto.CreateSprintRequest;
import com.trackflow.dto.SprintDTO;
import com.trackflow.dto.UpdateSprintStatusRequest;
import com.trackflow.entity.Project;
import com.trackflow.entity.Sprint;
import com.trackflow.entity.SprintStatus;
import com.trackflow.exception.BadRequestException;
import com.trackflow.exception.ResourceNotFoundException;
import com.trackflow.mapper.SprintMapper;
import com.trackflow.repository.ProjectRepository;
import com.trackflow.repository.SprintRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation providing Sprint Lifecycle Management.
 */
@Service
@RequiredArgsConstructor
public class SprintServiceImpl implements SprintService {

    private final SprintRepository sprintRepository;
    private final ProjectRepository projectRepository;
    private final SprintMapper sprintMapper;

    @Override
    @Transactional
    public SprintDTO createSprint(Long projectId, CreateSprintRequest request) {
        Project project = projectRepository.findById(projectId)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        // 1. Validate Sprint Dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("Sprint end date must be after the start date");
        }

        // 2. Build and save the Sprint
        Sprint sprint = Sprint.builder()
                .name(request.getName())
                .goal(request.getGoal())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(SprintStatus.PLANNING)
                .project(project)
                .build();

        Sprint savedSprint = sprintRepository.save(sprint);
        return sprintMapper.toDTO(savedSprint);
    }

    @Override
    @Transactional(readOnly = true)
    public SprintDTO getSprintById(Long id) {
        Sprint sprint = sprintRepository.findById(id)
                .filter(s -> !s.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Sprint", "id", id));
        return sprintMapper.toDTO(sprint);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SprintDTO> getSprintsByProject(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", "id", projectId);
        }
        return sprintRepository.findByProjectIdAndIsDeletedFalse(projectId).stream()
                .map(sprintMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public SprintDTO updateSprintStatus(Long id, UpdateSprintStatusRequest request) {
        Sprint sprint = sprintRepository.findById(id)
                .filter(s -> !s.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Sprint", "id", id));

        SprintStatus newStatus = request.getStatus();
        SprintStatus currentStatus = sprint.getStatus();

        // 1. Validate State Transitions (PLANNING -> ACTIVE -> COMPLETED)
        if (currentStatus == SprintStatus.PLANNING && newStatus == SprintStatus.ACTIVE) {
            // Enforce constraint: Only ONE active sprint allowed per project
            boolean hasActiveSprint = sprintRepository.existsByProjectIdAndStatusAndIsDeletedFalse(
                    sprint.getProject().getId(), SprintStatus.ACTIVE
            );
            if (hasActiveSprint) {
                throw new BadRequestException("Cannot start this sprint: Another sprint is already active in this project");
            }
        } else if (currentStatus == SprintStatus.ACTIVE && newStatus == SprintStatus.COMPLETED) {
            // Sprints can be completed. Sprints cannot go from completed back to active.
        } else if (currentStatus == newStatus) {
            // No-op transition
        } else {
            throw new BadRequestException(String.format("Invalid sprint status transition from %s to %s", currentStatus, newStatus));
        }

        sprint.setStatus(newStatus);
        Sprint updatedSprint = sprintRepository.save(sprint);
        return sprintMapper.toDTO(updatedSprint);
    }
}
