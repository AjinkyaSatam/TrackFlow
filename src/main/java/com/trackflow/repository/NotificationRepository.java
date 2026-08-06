package com.trackflow.repository;

import com.trackflow.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for {@link Notification} entity.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Lists notifications for a recipient, paginated.
     */
    Page<Notification> findByRecipientIdAndIsDeletedFalse(Long recipientId, Pageable pageable);

    /**
     * Counts unread notifications for a user.
     */
    long countByRecipientIdAndReadFalseAndIsDeletedFalse(Long recipientId);

    /**
     * Bulk updates all unread notifications of a user to read.
     *
     * <p>{@code @Modifying} tells Spring Data JPA that this query executes an
     * UPDATE/DELETE statement, which modifies database state.
     * Requires Transactional context from the service layer calling it.</p>
     */
    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.recipient.id = :recipientId " +
           "AND n.read = false AND n.isDeleted = false")
    void markAllAsRead(@Param("recipientId") Long recipientId);
}
