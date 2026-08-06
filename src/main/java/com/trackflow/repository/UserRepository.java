package com.trackflow.repository;

import com.trackflow.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for {@link User} entity.
 *
 * <p>Extends {@link JpaRepository} to inherit standard CRUD operations.
 * Spring Data JPA auto-generates the database queries from method signatures
 * or custom @Query annotations.</p>
 *
 * <h3>Design Decisions:</h3>
 * <ul>
 *   <li>Only select users who are not soft-deleted ({@code isDeleted = false}).</li>
 *   <li>Use {@link Optional} for single-record lookups to avoid NullPointerExceptions.</li>
 * </ul>
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds an active user by their email address.
     *
     * <p>Spring Data JPA parses this method name to generate:
     * {@code SELECT * FROM users WHERE email = ? AND is_deleted = false}</p>
     *
     * @param email User's email
     * @return Optional containing the User if found and not deleted
     */
    Optional<User> findByEmailAndIsDeletedFalse(String email);

    /**
     * Checks if a user already exists with the given email.
     *
     * <p>Generated SQL:
     * {@code SELECT COUNT(*) FROM users WHERE email = ? AND is_deleted = false}</p>
     *
     * @param email Email to check
     * @return true if an active user exists with this email
     */
    boolean existsByEmailAndIsDeletedFalse(String email);

    /**
     * Performs a paginated search for users within an organization.
     *
     * <p>This uses a custom JPQL query. JPQL queries operate on Java objects
     * and their fields rather than database tables and columns.</p>
     *
     * <h3>N+1 Query Prevention:</h3>
     * <p>We don't need JOIN FETCH here because we only need the user fields.
     * However, if the client needs organization info, we would use join fetch.</p>
     *
     * @param orgId    Organization ID
     * @param query    Search string matched against name or email
     * @param pageable Pagination and sorting options
     * @return A page of matching users
     */
    @Query("SELECT u FROM User u WHERE u.organization.id = :orgId " +
           "AND u.isDeleted = false " +
           "AND (LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<User> searchUsersInOrganization(@Param("orgId") Long orgId,
                                         @Param("query") String query,
                                         Pageable pageable);
}
