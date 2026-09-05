package com.example.ledgercore.reconciliation.command.repository;

import com.example.ledgercore.reconciliation.entity.ReconciliationRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface ReconciliationRunCommandRepository
        extends JpaRepository<ReconciliationRun, UUID> {

    @Query(
            value = """
                    SELECT *
                    FROM reconciliation_runs
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
    Optional<ReconciliationRun> findClaimableRun(
            @Param("staleBefore") Instant staleBefore
    );

    @Modifying
    @Query(
            value = """
                    INSERT INTO reconciliation_runs (
                        id,
                        business_date,
                        type,
                        status,
                        processed_count,
                        created_at,
                        updated_at
                    )
                    VALUES (
                        gen_random_uuid(),
                        :businessDate,
                        :type,
                        'PENDING',
                        0,
                        CURRENT_TIMESTAMP,
                        CURRENT_TIMESTAMP
                    )
                    ON CONFLICT (business_date, type)
                    DO NOTHING
                    """,
            nativeQuery = true
    )
    int insertIfNotExists(
            @Param("businessDate") LocalDate businessDate,
            @Param("type") String type
    );
}