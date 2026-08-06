package com.trackflow.repository;

import com.trackflow.entity.Issue;
import com.trackflow.entity.IssuePriority;
import com.trackflow.entity.IssueStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link Issue} entity.
 *
 * <p>Contains advanced JPQL queries for workload balancing, duplicate detection,
 * and issue tracking.</p>
 */
@Repository
public interface IssueRepository extends JpaRepository<Issue, Long> {

    /**
     * Finds an issue by its unique key (e.g. "TF-42").
     */
    Optional<Issue> findByIssueKeyAndIsDeletedFalse(String issueKey);

    /**
     * Lists issues belonging to a project.
     */
    Page<Issue> findByProjectIdAndIsDeletedFalse(Long projectId, Pageable pageable);

    /**
     * Lists issues assigned to a specific sprint.
     */
    List<Issue> findBySprintIdAndIsDeletedFalse(Long sprintId);

    /**
     * Lists issues in a project that are NOT assigned to any sprint (the backlog).
     */
    List<Issue> findByProjectIdAndSprintIdIsNullAndIsDeletedFalse(Long projectId);

    /**
     * Counts how many issues of a specific priority exist in an active sprint.
     * Used for calculating Sprint Health Scores.
     */
    long countBySprintIdAndPriorityAndIsDeletedFalse(Long sprintId, IssuePriority priority);

    /**
     * Counts how many issues of a specific status exist in an active sprint.
     */
    long countBySprintIdAndStatusAndIsDeletedFalse(Long sprintId, IssueStatus status);

    /**
     * 👥 Developer Workload Balancer Query
     *
     * <p>Finds the total active issues and estimated hours for an assignee.
     * We define "active" as status not being DONE.</p>
     *
     * @param assigneeId User ID
     * @return Object array containing: [0] Long count, [1] Double totalHours
     */
    @Query("SELECT COUNT(i), SUM(i.estimatedHours) FROM Issue i " +
           "WHERE i.assignee.id = :assigneeId " +
           "AND i.status != com.trackflow.entity.IssueStatus.DONE " +
           "AND i.isDeleted = false")
    Object[] getDeveloperWorkload(@Param("assigneeId") Long assigneeId);

    /**
     * 🔍 Text-based Duplicate Issue Detection Query (No AI required)
     *
     * <p>Finds issues in the same project whose titles match a text search.
     * We pass this query to check for potential duplicates before creating a new issue.</p>
     *
     * @param projectId Project ID
     * @param title     Title to check
     * @return List of matching issues
     */
    @Query("SELECT i FROM Issue i WHERE i.project.id = :projectId " +
           "AND i.isDeleted = false " +
           "AND LOWER(i.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<Issue> findPotentialDuplicates(@Param("projectId") Long projectId, @Param("title") String title);

    /**
     * Advanced Issue Search & Filter Query.
     * Allows filtering by status, priority, assignee, type, and search keyword.
     */
    @Query("SELECT i FROM Issue i WHERE i.project.id = :projectId " +
           "AND i.isDeleted = false " +
           "AND (:status IS NULL OR i.status = :status) " +
           "AND (:priority IS NULL OR i.priority = :priority) " +
           "AND (:assigneeId IS NULL OR i.assignee.id = :assigneeId) " +
           "AND (:keyword IS NULL OR LOWER(i.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Issue> filterIssues(@Param("projectId") Long projectId,
                             @Param("status") IssueStatus status,
                             @Param("priority") IssuePriority priority,
                             @Param("assigneeId") Long assigneeId,
                             @Param("keyword") String keyword,
                             Pageable pageable);
}
