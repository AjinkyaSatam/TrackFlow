package com.trackflow.service;

import com.trackflow.dto.CommentDTO;
import com.trackflow.dto.CreateCommentRequest;
import com.trackflow.entity.Comment;
import com.trackflow.entity.Issue;
import com.trackflow.entity.Role;
import com.trackflow.entity.User;
import com.trackflow.exception.AccessDeniedException;
import com.trackflow.exception.ResourceNotFoundException;
import com.trackflow.mapper.CommentMapper;
import com.trackflow.repository.CommentRepository;
import com.trackflow.repository.IssueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation providing Comment Threading Management.
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final IssueRepository issueRepository;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public CommentDTO addComment(Long issueId, CreateCommentRequest request, User author) {
        Issue issue = issueRepository.findById(issueId)
                .filter(i -> !i.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Issue", "id", issueId));

        Comment comment = Comment.builder()
                .content(request.getContent())
                .author(author)
                .issue(issue)
                .build();

        Comment savedComment = commentRepository.save(comment);
        return commentMapper.toDTO(savedComment);
    }

    @Override
    @Transactional
    public CommentDTO updateComment(Long commentId, CreateCommentRequest request, User author) {
        Comment comment = findCommentOrThrow(commentId);

        // Access Control: Only the original author can edit their own comment
        if (!comment.getAuthor().getId().equals(author.getId())) {
            throw new AccessDeniedException("You do not have permission to edit this comment");
        }

        comment.setContent(request.getContent());
        Comment updatedComment = commentRepository.save(comment);
        return commentMapper.toDTO(updatedComment);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, User author) {
        Comment comment = findCommentOrThrow(commentId);

        // Access Control: Only the author, an Org Admin, or a Super Admin can delete a comment
        boolean isAuthor = comment.getAuthor().getId().equals(author.getId());
        boolean isAdmin = author.getRole() == Role.ORG_ADMIN || author.getRole() == Role.SUPER_ADMIN;

        if (!isAuthor && !isAdmin) {
            throw new AccessDeniedException("You do not have permission to delete this comment");
        }

        comment.setDeleted(true);
        commentRepository.save(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentDTO> getCommentsByIssue(Long issueId, Pageable pageable) {
        if (!issueRepository.existsById(issueId)) {
            throw new ResourceNotFoundException("Issue", "id", issueId);
        }
        return commentRepository.findByIssueIdWithAuthors(issueId, pageable)
                .map(commentMapper::toDTO);
    }

    private Comment findCommentOrThrow(Long id) {
        return commentRepository.findById(id)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", id));
    }
}
