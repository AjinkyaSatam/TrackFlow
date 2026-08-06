package com.trackflow.repository;

import com.trackflow.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for {@link Project} entity.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /**
     * Fetch a project by ID with its organization and creator pre-loaded.
     *
     * <p>We use {@code JOIN FETCH} to instruct Hibernate to pull the related
     * entities in the initial SQL JOIN, resolving potential N+1 query overheads.</p>
     *
     * @param id Project ID
     * @return Optional project
     */
    @Query("SELECT p FROM Project p " +
           "JOIN FETCH p.organization " +
           "JOIN FETCH p.createdBy " +
           "WHERE p.id = :id AND p.isDeleted = false")
    Optional<Project> findByIdWithDetails(@Param("id") Long id);

    /**
     * Lists all projects belonging to an organization.
     *
     * @param orgId    Organization ID
     * @param pageable Pagination settings
     * @return Page of projects
     */
    Page<Project> findByOrganizationIdAndIsDeletedFalse(Long orgId, Pageable pageable);

    /**
     * Checks if a project key already exists in the system.
     * Project keys must be globally unique (e.g. "TF", "JIRA").
     *
     * @param projectKey The key to check (e.g. "TF")
     * @return true if the key is already in use
     */
    boolean existsByProjectKeyIgnoreCaseAndIsDeletedFalse(String projectKey);
}
