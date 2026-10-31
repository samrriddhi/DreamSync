package com.dreamsync.repository;

import com.dreamsync.entity.RefreshToken;
import com.dreamsync.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByUser(User user);

    @Modifying
    @Transactional
    @Query("""
            UPDATE RefreshToken rt
            SET rt.token = :newToken, rt.expiryDate = :newExpiry
            WHERE rt.id = :id
              AND rt.token = :currentToken
              AND rt.expiryDate > :now
            """)
    int rotateIfCurrent(
            @Param("id") Long id,
            @Param("currentToken") String currentToken,
            @Param("now") LocalDateTime now,
            @Param("newToken") String newToken,
            @Param("newExpiry") LocalDateTime newExpiry
    );

    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.user = :user")
    void deleteByUser(@Param("user") User user);
}