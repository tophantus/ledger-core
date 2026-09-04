package com.example.ledgercore.reconciliation.command.repository;

import com.example.ledgercore.reconciliation.entity.ReconciliationRun;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.UUID;

public interface ReconciliationRunCommandRepository
        extends JpaRepository<ReconciliationRun, UUID> {

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
    void insertIfNotExists(
            @Param("businessDate") LocalDate businessDate,
            @Param("type") String type
    );

}