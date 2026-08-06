package com.trackflow.repository;

import com.trackflow.entity.Sprint;
import com.trackflow.entity.SprintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link Sprint} entity.
 */
@Repository
public interface SprintRepository extends JpaRepository<Sprint, Long> {

    /**
     * Lists all sprints for a specific project.
     *
     * @param projectId Project ID
     * @return List of sprints
     */
    List<Sprint> findByProjectIdAndIsDeletedFalse(Long projectId);

    /**
     * Finds the currently active sprint of a project, if any.
     *
     * <p>Used to validate that only one sprint is active per project
     * when starting a new sprint.</p>
     *
     * @param projectId Project ID
     * @param status    Should pass {@link SprintStatus#ACTIVE}
     * @return Optional containing the active sprint
     */
    Optional<Sprint> findByProjectIdAndStatusAndIsDeletedFalse(Long projectId, SprintStatus status);

    /**
     * Checks if any sprint in the project is currently active.
     */
    boolean existsByProjectIdAndStatusAndIsDeletedFalse(Long projectId, SprintStatus status);
}
