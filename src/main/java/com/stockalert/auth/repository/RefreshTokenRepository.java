package com.stockalert.auth.repository;

import com.stockalert.auth.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUserIdAndRevokedFalseAndExpiresAtAfter(Long userId, LocalDateTime now);

    @Modifying
    @Query("update RefreshToken rt set rt.revoked = true, rt.revokedAt = :revokedAt where rt.user.id = :userId and rt.revoked = false and rt.expiresAt > :now")
    int revokeActiveTokensByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now, @Param("revokedAt") LocalDateTime revokedAt);
}
