package com.trackflow.service;

import com.trackflow.dto.ActivityLogDTO;
import com.trackflow.entity.ActivityType;
import com.trackflow.entity.Issue;
import com.trackflow.entity.Project;
import com.trackflow.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface defining system audit logging operations.
 */
public interface ActivityLogService {

    void logActivity(ActivityType type, String description, String oldValue, String newValue, User user, Project project, Issue issue);

    Page<ActivityLogDTO> getIssueTimeline(Long issueId, Pageable pageable);

    Page<ActivityLogDTO> getProjectTimeline(Long projectId, Pageable pageable);
}
