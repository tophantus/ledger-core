package com.example.ledgercore.reconciliation.query.repository;

import com.example.ledgercore.reconciliation.entity.ReconciliationRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ReconciliationRunQueryRepository
        extends JpaRepository<ReconciliationRun, UUID> {

    @Query("""
            SELECT
                r.id AS id,
                r.type AS type,
                r.status AS status,
                r.processedCount AS processedCount
            FROM ReconciliationRun r
            WHERE r.businessDate = :businessDate
            ORDER BY r.type ASC
            """)
    List<ReconciliationRunSummaryProjection> findSummaryByBusinessDate(
            @Param("businessDate") LocalDate businessDate
    );
}