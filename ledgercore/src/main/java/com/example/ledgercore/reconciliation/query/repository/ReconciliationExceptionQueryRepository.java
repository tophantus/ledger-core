package com.example.ledgercore.reconciliation.query.repository;

import com.example.ledgercore.reconciliation.entity.ReconciliationException;
import com.example.ledgercore.reconciliation.enums.ReconciliationErrorCode;
import com.example.ledgercore.reconciliation.enums.ReconciliationTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.UUID;

public interface ReconciliationExceptionQueryRepository
        extends JpaRepository<ReconciliationException, UUID> {

    @Query("""
            SELECT
                e.id AS id,
                r.id AS reconciliationRunId,
                r.businessDate AS businessDate,
                e.targetType AS targetType,
                e.targetId AS targetId,
                e.errorCode AS errorCode,
                e.expectedValue AS expectedValue,
                e.actualValue AS actualValue,
                e.message AS message,
                e.createdAt AS createdAt
            FROM ReconciliationException e
            JOIN e.reconciliationRun r
            WHERE (:businessDate IS NULL
                   OR r.businessDate = :businessDate)
              AND (:targetType IS NULL
                   OR e.targetType = :targetType)
              AND (:errorCode IS NULL
                   OR e.errorCode = :errorCode)
            ORDER BY e.createdAt DESC
            """)
    Page<ReconciliationExceptionProjection> findAllForAdmin(
            @Param("businessDate") LocalDate businessDate,
            @Param("targetType") ReconciliationTargetType targetType,
            @Param("errorCode") ReconciliationErrorCode errorCode,
            Pageable pageable
    );
}