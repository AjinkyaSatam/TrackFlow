package com.trackflow.service;

import com.trackflow.dto.ActivityLogDTO;
import com.trackflow.entity.ActivityLog;
import com.trackflow.entity.ActivityType;
import com.trackflow.entity.Issue;
import com.trackflow.entity.Project;
import com.trackflow.entity.User;
import com.trackflow.mapper.ActivityLogMapper;
import com.trackflow.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation providing System Audit Logging.
 */
@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final ActivityLogMapper activityLogMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logActivity(ActivityType type, String description, String oldValue, String newValue,
                            User user, Project project, Issue issue) {
        ActivityLog log = ActivityLog.builder()
                .activityType(type)
                .description(description)
                .oldValue(oldValue)
                .newValue(newValue)
                .user(user)
                .project(project)
                .issue(issue)
                .build();
        activityLogRepository.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityLogDTO> getIssueTimeline(Long issueId, Pageable pageable) {
        return activityLogRepository.findByIssueIdAndIsDeletedFalse(issueId, pageable)
                .map(activityLogMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityLogDTO> getProjectTimeline(Long projectId, Pageable pageable) {
        return activityLogRepository.findByProjectIdAndIsDeletedFalse(projectId, pageable)
                .map(activityLogMapper::toDTO);
    }
}
