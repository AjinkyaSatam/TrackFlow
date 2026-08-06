package com.trackflow.repository;

import com.trackflow.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for {@link ActivityLog} entity.
 */
@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    /**
     * Fetches timeline logs for a specific issue.
     */
    Page<ActivityLog> findByIssueIdAndIsDeletedFalse(Long issueId, Pageable pageable);

    /**
     * Fetches timeline logs for a specific project.
     */
    Page<ActivityLog> findByProjectIdAndIsDeletedFalse(Long projectId, Pageable pageable);

    /**
     * Fetches timeline logs related to a user.
     */
    Page<ActivityLog> findByUserIdAndIsDeletedFalse(Long userId, Pageable pageable);
}
