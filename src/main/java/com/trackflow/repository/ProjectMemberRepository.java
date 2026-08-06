package com.trackflow.repository;

import com.trackflow.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link ProjectMember} join entity.
 */
@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    /**
     * Lists all members assigned to a specific project.
     * Uses JOIN FETCH to load the actual User entity to prevent N+1.
     */
    @Query("SELECT pm FROM ProjectMember pm JOIN FETCH pm.user " +
           "WHERE pm.project.id = :projectId AND pm.isDeleted = false")
    List<ProjectMember> findByProjectIdWithUsers(@Param("projectId") Long projectId);

    /**
     * Finds a membership by Project and User.
     */
    Optional<ProjectMember> findByProjectIdAndUserIdAndIsDeletedFalse(Long projectId, Long userId);

    /**
     * Checks if a user is a member of a project.
     */
    boolean existsByProjectIdAndUserIdAndIsDeletedFalse(Long projectId, Long userId);

    /**
     * Lists all projects a user belongs to.
     */
    @Query("SELECT pm FROM ProjectMember pm JOIN FETCH pm.project " +
           "WHERE pm.user.id = :userId AND pm.isDeleted = false")
    List<ProjectMember> findByUserIdWithProjects(@Param("userId") Long userId);
}
