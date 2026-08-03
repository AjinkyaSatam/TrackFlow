package com.trackflow.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Abstract base entity providing common audit fields for all entities.
 *
 * <p>Every entity in TrackFlow extends this class to automatically get:</p>
 * <ul>
 *   <li>{@code createdAt} — Timestamp of record creation (set once, never changes)</li>
 *   <li>{@code updatedAt} — Timestamp of last modification (auto-updated)</li>
 *   <li>{@code isDeleted} — Soft delete flag (data is hidden, not destroyed)</li>
 * </ul>
 *
 * <h3>Why use @MappedSuperclass instead of @Entity?</h3>
 * <p>{@code @MappedSuperclass} means this class is NOT a table itself.
 * Its fields are inherited by child entities and stored in THEIR tables.
 * If we used {@code @Entity}, JPA would create a separate "base_entity" table
 * and use inheritance strategies (JOINED, TABLE_PER_CLASS), which adds
 * unnecessary complexity.</p>
 *
 * <h3>Why not use @PrePersist / @PreUpdate?</h3>
 * <p>We could, but Hibernate's {@code @CreationTimestamp} and {@code @UpdateTimestamp}
 * handle this automatically with less boilerplate. They work at the Hibernate level
 * and set timestamps before the SQL is generated.</p>
 *
 * <h3>Interview Question:</h3>
 * <p>"How do you implement audit fields in JPA?"</p>
 * <p>Answer: Use {@code @MappedSuperclass} with {@code @CreationTimestamp}
 * and {@code @UpdateTimestamp}. For more advanced auditing (who changed it),
 * use Spring Data JPA's {@code @EnableJpaAuditing} with {@code @CreatedBy}
 * and {@code @LastModifiedBy}.</p>
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {

    /**
     * Timestamp when the record was created.
     *
     * <p>{@code updatable = false} ensures this value is set ONCE at creation
     * and never changes, even if the entity is updated.</p>
     *
     * <p>{@code @CreationTimestamp} tells Hibernate to automatically set
     * this field to the current timestamp when the entity is first persisted.</p>
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp of the last update.
     *
     * <p>{@code @UpdateTimestamp} tells Hibernate to automatically update
     * this field every time the entity is modified and saved.</p>
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Soft delete flag.
     *
     * <p>When true, the record is "deleted" but still exists in the database.
     * All queries should filter by {@code isDeleted = false} to exclude
     * soft-deleted records.</p>
     *
     * <p>We use {@code columnDefinition} to set the default at the DATABASE level,
     * not just the Java level. This ensures consistency even if records are
     * inserted directly via SQL.</p>
     */
    @Column(name = "is_deleted", nullable = false, columnDefinition = "boolean default false")
    private boolean isDeleted = false;
}
