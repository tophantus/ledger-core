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

    @Query(value = """
        SELECT *
        FROM otp_challenges
        WHERE subject_id = :subjectId
          AND (
                (:referenceId IS NULL AND reference_id IS NULL)
                OR reference_id = :referenceId
              )
          AND purpose = :purpose
          AND status = :status
        ORDER BY created_at DESC
        LIMIT 1
        """, nativeQuery = true)
    Optional<OtpChallenge> findLatest(
            UUID subjectId,
            UUID referenceId,
            String purpose,
            String status
    );
}