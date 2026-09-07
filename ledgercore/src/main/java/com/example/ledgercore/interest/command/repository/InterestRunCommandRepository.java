package com.example.ledgercore.interest.command.repository;

import com.example.ledgercore.interest.entity.InterestRun;
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
                    status = 'PENDING'
                    OR (
                        status = 'RUNNING'
                        AND heartbeat_at < :staleBefore
                    )
                ORDER BY created_at ASC
                FOR UPDATE SKIP LOCKED
                LIMIT 1
                """,
            nativeQuery = true
    )
    Optional<InterestRun> findClaimableRun(
            @Param("staleBefore") Instant staleBefore
    );

    boolean existsByBusinessDate(
            LocalDate businessDate
    );
}