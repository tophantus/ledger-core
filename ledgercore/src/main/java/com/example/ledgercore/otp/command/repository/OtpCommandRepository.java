package com.example.ledgercore.otp.command.repository;

import com.example.ledgercore.otp.entity.OtpChallenge;
import com.example.ledgercore.otp.enums.OtpPurpose;
import com.example.ledgercore.otp.enums.OtpStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface OtpCommandRepository
        extends JpaRepository<OtpChallenge, UUID> {

    @Query("""
        SELECT o
        FROM OtpChallenge o
        WHERE o.subjectId = :subjectId
          AND (
                (:referenceId IS NULL AND o.referenceId IS NULL)
                OR o.referenceId = :referenceId
              )
          AND o.purpose = :purpose
          AND o.status = :status
        ORDER BY o.createdAt DESC
        """)
    Optional<OtpChallenge> findLatest(
            UUID subjectId,
            UUID referenceId,
            OtpPurpose purpose,
            OtpStatus status
    );
}