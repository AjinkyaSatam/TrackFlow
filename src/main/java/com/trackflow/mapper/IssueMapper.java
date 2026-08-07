package com.trackflow.mapper;

import com.trackflow.dto.IssueDTO;
import com.trackflow.entity.Issue;
import org.springframework.stereotype.Component;

/**
 * Mapper utility to translate between Issue entity and IssueDTO.
 */
@Component
public class IssueMapper {

    public IssueDTO toDTO(Issue issue) {
        if (issue == null) {
            return null;
        }

        Long reporterId = null;
        String reporterName = null;
        Long assigneeId = null;
        String assigneeName = null;
        Long sprintId = null;
        String sprintName = null;

        if (issue.getReporter() != null) {
            reporterId = issue.getReporter().getId();
            reporterName = issue.getReporter().getFullName();
        }

        if (issue.getAssignee() != null) {
            assigneeId = issue.getAssignee().getId();
            assigneeName = issue.getAssignee().getFullName();
        }

        if (issue.getSprint() != null) {
            sprintId = issue.getSprint().getId();
            sprintName = issue.getSprint().getName();
        }

        return IssueDTO.builder()
                .id(issue.getId())
                .issueKey(issue.getIssueKey())
                .title(issue.getTitle())
                .description(issue.getDescription())
                .type(issue.getType())
                .priority(issue.getPriority())
                .status(issue.getStatus().name())
                .dueDate(issue.getDueDate())
                .estimatedHours(issue.getEstimatedHours())
                .labels(issue.getLabels())
                .reporterId(reporterId)
                .reporterName(reporterName)
                .assigneeId(assigneeId)
                .assigneeName(assigneeName)
                .projectId(issue.getProject() != null ? issue.getProject().getId() : null)
                .sprintId(sprintId)
                .sprintName(sprintName)
                .createdAt(issue.getCreatedAt())
                .build();
    }
}
