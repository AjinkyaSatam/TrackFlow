package com.trackflow.mapper;

import com.trackflow.dto.SprintDTO;
import com.trackflow.entity.Sprint;
import org.springframework.stereotype.Component;

/**
 * Mapper utility to translate between Sprint entity and SprintDTO.
 */
@Component
public class SprintMapper {

    public SprintDTO toDTO(Sprint sprint) {
        if (sprint == null) {
            return null;
        }

        return SprintDTO.builder()
                .id(sprint.getId())
                .name(sprint.getName())
                .goal(sprint.getGoal())
                .startDate(sprint.getStartDate())
                .endDate(sprint.getEndDate())
                .status(sprint.getStatus().name())
                .projectId(sprint.getProject() != null ? sprint.getProject().getId() : null)
                .createdAt(sprint.getCreatedAt())
                .build();
    }
}
