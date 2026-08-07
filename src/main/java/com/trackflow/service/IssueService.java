package com.trackflow.service;

import com.trackflow.dto.*;
import com.trackflow.entity.IssuePriority;
import com.trackflow.entity.IssueStatus;
import com.trackflow.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface defining issue management and workflow operations.
 */
public interface IssueService {

    IssueDTO createIssue(Long projectId, CreateIssueRequest request, User reporter);

    IssueDTO getIssueById(Long id);

    IssueDTO getIssueByKey(String issueKey);

    Page<IssueDTO> filterIssues(Long projectId, IssueStatus status, IssuePriority priority,
                                Long assigneeId, String keyword, Pageable pageable);

    IssueDTO updateIssueStatus(Long id, UpdateIssueStatusRequest request);

    IssueDTO updateIssueAssignee(Long id, UpdateIssueAssigneeRequest request);
}
