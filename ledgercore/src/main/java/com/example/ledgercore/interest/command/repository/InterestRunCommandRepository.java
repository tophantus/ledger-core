package com.example.ledgercore.interest.command.repository;

import com.example.ledgercore.interest.entity.InterestRun;
import com.example.ledgercore.interest.enums.InterestRunType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface InterestRunCommandRepository
        extends JpaRepository<InterestRun, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT r
        FROM InterestRun r
        WHERE r.id = :runId
        """)
    Optional<InterestRun> findByIdForUpdate(
            @Param("runId") UUID runId
    );

    @Query(
            value = """
            SELECT *
            FROM interest_runs
            WHERE
                status = :pendingStatus
                OR (
                    status = :runningStatus
                    AND heartbeat_at < :staleBefore
                )
            ORDER BY created_at ASC
            FOR UPDATE SKIP LOCKED
            LIMIT 1
            """,
            nativeQuery = true
    )
    Optional<InterestRun> findClaimableRun(
            @Param("pendingStatus") String pendingStatus,
            @Param("runningStatus") String runningStatus,
            @Param("staleBefore") Instant staleBefore
    );

    boolean existsByBusinessDateAndRunType(
            LocalDate businessDate,
            InterestRunType runType
    );
}