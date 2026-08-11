package com.sewagealert.auth.repository;

import com.sewagealert.auth.model.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    // findByTokenHash: Tokens are looked up by their SHA-256 digest — the raw token is
    // never stored or queryable.
    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);

    // invalidateByUser: Marks every outstanding token of a user as used so a new
    // verification request supersedes all older links (single-use guarantee).
    @Modifying
    @Query("UPDATE EmailVerificationToken t SET t.used = true WHERE t.userId = :userId AND t.used = false")
    int invalidateByUser(@Param("userId") Long userId);
}
