package com.trackflow.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Comment entity representing a user's comment on an Issue.
 *
 * <p>Comments are the primary communication mechanism on issues.
 * They support @mentions to notify other users.</p>
 *
 * <h3>Design Decisions:</h3>
 * <ul>
 *   <li><strong>Content as TEXT:</strong> Comments can be long (bug descriptions,
 *       code snippets, etc.), so we use TEXT instead of VARCHAR.</li>
 *   <li><strong>@mentions stored in content:</strong> Mentions like "@john" are
 *       parsed from the comment text. We don't create a separate mentions table —
 *       the NotificationService extracts @mentions when a comment is created
 *       and sends notifications.</li>
 *   <li><strong>orphanRemoval on Issue→Comments:</strong> If an issue is deleted,
 *       its comments are automatically removed.</li>
 * </ul>
 */
@Entity
@Table(name = "comments", indexes = {
        @Index(name = "idx_comment_issue", columnList = "issue_id"),
        @Index(name = "idx_comment_author", columnList = "author_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The comment text (supports @mentions) */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** The user who wrote this comment */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    /** The issue this comment belongs to */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id", nullable = false)
    private Issue issue;
}
