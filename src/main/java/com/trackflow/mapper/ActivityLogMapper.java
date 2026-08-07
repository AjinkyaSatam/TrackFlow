package com.trackflow.mapper;

import com.trackflow.dto.ActivityLogDTO;
import com.trackflow.entity.ActivityLog;
import org.springframework.stereotype.Component;

/**
 * Mapper utility to translate between ActivityLog entity and ActivityLogDTO.
 */
@Component
public class ActivityLogMapper {

    public ActivityLogDTO toDTO(ActivityLog log) {
        if (log == null) {
            return null;
        }

        Long userId = null;
        String userName = null;

        if (log.getUser() != null) {
            userId = log.getUser().getId();
            userName = log.getUser().getFullName();
        }

        return ActivityLogDTO.builder()
                .id(log.getId())
                .activityType(log.getActivityType().name())
                .description(log.getDescription())
                .oldValue(log.getOldValue())
                .newValue(log.getNewValue())
                .userId(userId)
                .userName(userName)
                .projectId(log.getProject() != null ? log.getProject().getId() : null)
                .issueId(log.getIssue() != null ? log.getIssue().getId() : null)
                .createdAt(log.getCreatedAt())
                .build();
    }
}
