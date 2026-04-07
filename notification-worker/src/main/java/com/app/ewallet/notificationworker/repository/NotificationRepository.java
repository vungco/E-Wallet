package com.app.ewallet.notificationworker.repository;

import com.app.ewallet.notificationworker.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    boolean existsByIdempotencyKey(String idempotencyKey);

    long countByUserIdAndReadFlagIsFalse(long userId);

    @Query("""
            SELECT n FROM Notification n
            WHERE n.userId = :userId AND n.readFlag = :read
            AND (:beforeId IS NULL OR n.id < :beforeId)
            ORDER BY n.id DESC
            """)
    List<Notification> findPageForUser(
            @Param("userId") long userId,
            @Param("read") boolean read,
            @Param("beforeId") Long beforeId,
            org.springframework.data.domain.Pageable pageable
    );

    Optional<Notification> findByIdAndUserId(long id, long userId);
}
