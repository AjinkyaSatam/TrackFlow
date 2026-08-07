package com.trackflow.service;

import com.trackflow.dto.CommentDTO;
import com.trackflow.dto.CreateCommentRequest;
import com.trackflow.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface defining comment thread management operations.
 */
public interface CommentService {

    CommentDTO addComment(Long issueId, CreateCommentRequest request, User author);

    CommentDTO updateComment(Long commentId, CreateCommentRequest request, User author);

    void deleteComment(Long commentId, User author);

    Page<CommentDTO> getCommentsByIssue(Long issueId, Pageable pageable);
}
