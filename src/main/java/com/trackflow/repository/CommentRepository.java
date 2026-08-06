package com.trackflow.repository;

import com.trackflow.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for {@link Comment} entity.
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * Fetches all comments for an issue, ordered by creation date.
     * Uses JOIN FETCH to load comment authors immediately.
     */
    @Query(value = "SELECT c FROM Comment c JOIN FETCH c.author " +
                   "WHERE c.issue.id = :issueId AND c.isDeleted = false",
           countQuery = "SELECT COUNT(c) FROM Comment c WHERE c.issue.id = :issueId AND c.isDeleted = false")
    Page<Comment> findByIssueIdWithAuthors(@Param("issueId") Long issueId, Pageable pageable);
}
