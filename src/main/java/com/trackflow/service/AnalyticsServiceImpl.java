package com.trackflow.service;

import com.trackflow.dto.SprintHealthDTO;
import com.trackflow.dto.WorkloadSuggestionDTO;
import com.trackflow.entity.*;
import com.trackflow.exception.ResourceNotFoundException;
import com.trackflow.repository.IssueRepository;
import com.trackflow.repository.ProjectMemberRepository;
import com.trackflow.repository.SprintRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service implementation providing Intelligent Analytics.
 */
@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final ProjectMemberRepository projectMemberRepository;
    private final IssueRepository issueRepository;
    private final SprintRepository sprintRepository;

    @Override
    @Transactional(readOnly = true)
    public List<WorkloadSuggestionDTO> getWorkloadSuggestions(Long projectId) {
        List<ProjectMember> members = projectMemberRepository.findByProjectIdWithUsers(projectId);
        List<WorkloadSuggestionDTO> suggestions = new ArrayList<>();

        for (ProjectMember member : members) {
            User user = member.getUser();
            Object[] workload = issueRepository.getDeveloperWorkload(user.getId());
            
            long activeIssues = (long) workload[0];
            double totalHours = workload[1] != null ? (double) workload[1] : 0.0;

            String status = "LIGHT";
            if (totalHours > 40.0 || activeIssues > 8) {
                status = "HEAVY";
            } else if (totalHours > 20.0 || activeIssues > 4) {
                status = "MODERATE";
            }

            suggestions.add(WorkloadSuggestionDTO.builder()
                    .developerId(user.getId())
                    .developerName(user.getFullName())
                    .activeIssuesCount(activeIssues)
                    .totalEstimatedHours(totalHours)
                    .workloadStatus(status)
                    .build());
        }

        // Sort developers: lowest hours first (easiest to assign tasks to)
        suggestions.sort((a, b) -> Double.compare(a.getTotalEstimatedHours(), b.getTotalEstimatedHours()));
        return suggestions;
    }

    @Override
    @Transactional(readOnly = true)
    public SprintHealthDTO getSprintHealth(Long sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .filter(s -> !s.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Sprint", "id", sprintId));

        List<Issue> issues = issueRepository.findBySprintIdAndIsDeletedFalse(sprintId);

        long total = issues.size();
        if (total == 0) {
            return SprintHealthDTO.builder()
                    .sprintId(sprintId)
                    .sprintName(sprint.getName())
                    .healthScore(100)
                    .totalIssues(0)
                    .completedIssues(0)
                    .openIssues(0)
                    .criticalBugs(0)
                    .feedbackWarnings(List.of("No issues assigned to this sprint yet."))
                    .build();
        }

        long completed = issues.stream().filter(i -> i.getStatus() == IssueStatus.DONE).count();
        long criticalBugs = issues.stream().filter(i -> i.getType() == IssueType.BUG && i.getPriority() == IssuePriority.CRITICAL).count();
        long open = total - completed;

        List<String> warnings = new ArrayList<>();

        // 1. Base Score calculation
        double completionRate = (double) completed / total;
        int score = (int) (completionRate * 100);

        // 2. Penalty rules
        // Critical bugs reduce the health score significantly
        if (criticalBugs > 0) {
            score -= (criticalBugs * 15);
            warnings.add(String.format("Sprint has %d unresolved CRITICAL bugs blocker.", criticalBugs));
        }

        // Check if any developer is overloaded
        List<WorkloadSuggestionDTO> workloads = getWorkloadSuggestions(sprint.getProject().getId());
        long overloadedDevs = workloads.stream().filter(w -> w.getWorkloadStatus().equals("HEAVY")).count();
        if (overloadedDevs > 0) {
            score -= 10;
            warnings.add(String.format("%d developers have HEAVY workload warnings.", overloadedDevs));
        }

        // Ensure score bounds [0, 100]
        score = Math.max(0, Math.min(100, score));

        return SprintHealthDTO.builder()
                .sprintId(sprintId)
                .sprintName(sprint.getName())
                .healthScore(score)
                .totalIssues(total)
                .completedIssues(completed)
                .openIssues(open)
                .criticalBugs(criticalBugs)
                .feedbackWarnings(warnings)
                .build();
    }
}
