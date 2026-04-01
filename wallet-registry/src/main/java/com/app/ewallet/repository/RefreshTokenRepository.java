package com.app.ewallet.repository;

import com.app.ewallet.model.RefreshToken;
import com.app.ewallet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    @Query("""
            SELECT rt FROM RefreshToken rt
            JOIN FETCH rt.user
            WHERE rt.tokenHash = :hash
              AND rt.revokedAt IS NULL
              AND rt.expiresAt > :now
            """)
    Optional<RefreshToken> findActiveByTokenHash(@Param("hash") String hash, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revokedAt = :now WHERE rt.user = :user AND rt.revokedAt IS NULL")
    int revokeAllActiveForUser(@Param("user") User user, @Param("now") LocalDateTime now);
}
