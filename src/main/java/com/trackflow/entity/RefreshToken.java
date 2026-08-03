package com.trackflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * RefreshToken entity for managing JWT refresh tokens.
 *
 * <p>When a user logs in, they receive two tokens:</p>
 * <ol>
 *   <li><strong>Access Token (short-lived, ~15 min):</strong> Used for API requests.
 *       NOT stored in the database — validated by signature only.</li>
 *   <li><strong>Refresh Token (long-lived, ~7 days):</strong> Used to get a new
 *       access token when the old one expires. Stored in the database so it
 *       can be revoked (logout, security breach).</li>
 * </ol>
 *
 * <h3>Why store refresh tokens in the DB?</h3>
 * <ul>
 *   <li><strong>Revocation:</strong> When a user logs out, we delete their refresh token.
 *       They can't get a new access token anymore.</li>
 *   <li><strong>Security:</strong> If a token is compromised, we can invalidate it
 *       without affecting other users.</li>
 *   <li><strong>Single device or multi-device:</strong> You can allow multiple refresh
 *       tokens per user (multi-device) or only one (single session).</li>
 * </ul>
 *
 * <h3>Interview Question:</h3>
 * <p>"Why use both access tokens and refresh tokens?"</p>
 * <p>Answer: Access tokens are short-lived and stateless (no DB lookup needed),
 * making API calls fast. Refresh tokens are long-lived and stored in the DB,
 * providing a way to revoke access. This balances performance with security.</p>
 */
@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_refresh_token", columnList = "token", unique = true),
        @Index(name = "idx_refresh_token_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The refresh token string (UUID-based, opaque token) */
    @Column(nullable = false, unique = true, length = 255)
    private String token;

    /** When this token expires */
    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    /** Whether this token has been revoked (logout, security action) */
    @Column(nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private boolean revoked = false;

    /** The user this refresh token belongs to */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Checks if this token has expired.
     *
     * @return true if the token's expiry date is in the past
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryDate);
    }

    /**
     * Checks if this token is valid (not expired AND not revoked).
     *
     * @return true if the token can still be used
     */
    public boolean isValid() {
        return !isExpired() && !isRevoked();
    }
}
