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

    // findFirstByUserIdAndUsedFalseOrderByCreatedAtDesc: The user's single active code
    // (latest issued) — used by the 6-digit-code verification path.
    Optional<EmailVerificationToken> findFirstByUserIdAndUsedFalseOrderByCreatedAtDesc(Long userId);

    // invalidateByUser: Marks every outstanding code of a user as used so a new
    // verification request supersedes all older ones (single-use guarantee).
    @Modifying
    @Query("UPDATE EmailVerificationToken t SET t.used = true WHERE t.userId = :userId AND t.used = false")
    int invalidateByUser(@Param("userId") Long userId);
}
