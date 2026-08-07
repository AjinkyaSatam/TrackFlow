package com.trackflow.service;

import com.trackflow.dto.*;
import com.trackflow.entity.*;
import com.trackflow.exception.BadRequestException;
import com.trackflow.exception.ResourceNotFoundException;
import com.trackflow.mapper.IssueMapper;
import com.trackflow.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation providing Issue & State Workflow Management.
 */
@Service
@RequiredArgsConstructor
public class IssueServiceImpl implements IssueService {

    private final IssueRepository issueRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final SprintRepository sprintRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final IssueMapper issueMapper;

    @Override
    @Transactional
    public IssueDTO createIssue(Long projectId, CreateIssueRequest request, User reporter) {
        Project project = projectRepository.findById(projectId)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        // 1. Resolve and validate assignee (must belong to project organization)
        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = userRepository.findById(request.getAssigneeId())
                    .filter(u -> !u.isDeleted())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getAssigneeId()));

            // Verify they are a member of the project
            if (!projectMemberRepository.existsByProjectIdAndUserIdAndIsDeletedFalse(projectId, assignee.getId())) {
                throw new BadRequestException("Assignee must be a member of the project");
            }
        }

        // 2. Resolve and validate sprint
        Sprint sprint = null;
        if (request.getSprintId() != null) {
            sprint = sprintRepository.findById(request.getSprintId())
                    .filter(s -> !s.isDeleted())
                    .orElseThrow(() -> new ResourceNotFoundException("Sprint", "id", request.getSprintId()));

            if (!sprint.getProject().getId().equals(projectId)) {
                throw new BadRequestException("Sprint does not belong to the selected project");
            }
        }

        // 3. Autogenerate Issue Key (project key + sequence count)
        // For production, a concurrent-safe counter/sequence is better, but this works for local/testing.
        long projectIssuesCount = issueRepository.findByProjectIdAndIsDeletedFalse(projectId, Pageable.unpaged()).getTotalElements();
        String issueKey = String.format("%s-%d", project.getProjectKey(), projectIssuesCount + 1);

        // 4. Build and save the issue
        Issue issue = Issue.builder()
                .issueKey(issueKey)
                .title(request.getTitle())
                .description(request.getDescription())
                .type(request.getType())
                .priority(request.getPriority())
                .status(IssueStatus.OPEN)
                .dueDate(request.getDueDate())
                .estimatedHours(request.getEstimatedHours())
                .labels(request.getLabels())
                .reporter(reporter)
                .assignee(assignee)
                .project(project)
                .sprint(sprint)
                .build();

        Issue savedIssue = issueRepository.save(issue);
        return issueMapper.toDTO(savedIssue);
    }

    @Override
    @Transactional(readOnly = true)
    public IssueDTO getIssueById(Long id) {
        Issue issue = issueRepository.findById(id)
                .filter(i -> !i.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Issue", "id", id));
        return issueMapper.toDTO(issue);
    }

    @Override
    @Transactional(readOnly = true)
    public IssueDTO getIssueByKey(String issueKey) {
        Issue issue = issueRepository.findByIssueKeyAndIsDeletedFalse(issueKey)
                .orElseThrow(() -> new ResourceNotFoundException("Issue", "issueKey", issueKey));
        return issueMapper.toDTO(issue);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<IssueDTO> filterIssues(Long projectId, IssueStatus status, IssuePriority priority,
                                       Long assigneeId, String keyword, Pageable pageable) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", "id", projectId);
        }
        return issueRepository.filterIssues(projectId, status, priority, assigneeId, keyword, pageable)
                .map(issueMapper::toDTO);
    }

    @Override
    @Transactional
    public IssueDTO updateIssueStatus(Long id, UpdateIssueStatusRequest request) {
        Issue issue = issueRepository.findById(id)
                .filter(i -> !i.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Issue", "id", id));

        IssueStatus newStatus = request.getStatus();
        IssueStatus currentStatus = issue.getStatus();

        // Enforce Workflow State Machine Transitions (calls the canTransitionTo method we defined in Step 2)
        if (currentStatus != newStatus && !currentStatus.canTransitionTo(newStatus)) {
            throw new BadRequestException(String.format("Invalid workflow status transition from %s to %s", currentStatus, newStatus));
        }

        issue.setStatus(newStatus);
        Issue updatedIssue = issueRepository.save(issue);
        return issueMapper.toDTO(updatedIssue);
    }

    @Override
    @Transactional
    public IssueDTO updateIssueAssignee(Long id, UpdateIssueAssigneeRequest request) {
        Issue issue = issueRepository.findById(id)
                .filter(i -> !i.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Issue", "id", id));

        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = userRepository.findById(request.getAssigneeId())
                    .filter(u -> !u.isDeleted())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getAssigneeId()));

            // Verify they belong to the project workspace
            if (!projectMemberRepository.existsByProjectIdAndUserIdAndIsDeletedFalse(issue.getProject().getId(), assignee.getId())) {
                throw new BadRequestException("Assignee must be a member of the project");
            }
        }

        issue.setAssignee(assignee);
        Issue updatedIssue = issueRepository.save(issue);
        return issueMapper.toDTO(updatedIssue);
    }
}
