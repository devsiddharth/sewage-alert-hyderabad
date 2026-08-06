package com.sewagealert.notification.repository;

import com.sewagealert.notification.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Paginated, newest-first feed for a single user (soft-deleted records are hidden)
    Page<Notification> findByUserIdAndDeletedFalse(Long userId, Pageable pageable);

    // Counts total (non-deleted) notifications for a user
    long countByUserIdAndDeletedFalse(Long userId);

    // Counts unread notifications for a user — powers the badge in the UI
    long countByUserIdAndReadFalseAndDeletedFalse(Long userId);

    // Finds a single notification belonging to a specific user (ownership enforced at query level)
    Optional<Notification> findByIdAndUserIdAndDeletedFalse(Long id, Long userId);

    // Bulk "mark all as read" — single UPDATE instead of N+1 individual saves.
    // clearAutomatically avoids stale first-level cache after the bulk write.
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notification n SET n.read = true, n.readAt = :now " +
           "WHERE n.userId = :userId AND n.read = false AND n.deleted = false")
    int markAllAsRead(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    // Soft delete: flags the record instead of removing the row
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notification n SET n.deleted = true, n.updatedAt = :now " +
           "WHERE n.id = :id AND n.userId = :userId AND n.deleted = false")
    int softDelete(@Param("id") Long id, @Param("userId") Long userId, @Param("now") LocalDateTime now);
}
