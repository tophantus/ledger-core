package com.example.ledgercore.transaction.query.repository;

import com.example.ledgercore.transaction.entity.MoneyTransaction;
import com.example.ledgercore.transaction.enums.TransactionStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionQueryRepository
        extends JpaRepository<MoneyTransaction, UUID>,
                JpaSpecificationExecutor<MoneyTransaction> {

    Optional<MoneyTransaction> findByReference(
            String reference
    );

    @Query("""
        SELECT t
        FROM MoneyTransaction t
        WHERE t.businessDate = :businessDate
          AND (
                :lastProcessedId IS NULL
                OR t.id > :lastProcessedId
          )
        ORDER BY t.id ASC
        """)
    List<MoneyTransaction> findForReconciliation(
            @Param("businessDate") LocalDate businessDate,
            @Param("lastProcessedId") UUID lastProcessedId,
            Pageable pageable
    );

    @Query("""
            SELECT
                accountId AS accountId,
                COALESCE(SUM(totalCredit), 0) AS totalCredit,
                COALESCE(SUM(totalDebit), 0) AS totalDebit
            FROM (
                SELECT
                    t.destinationAccountId AS accountId,
                    t.amount AS totalCredit,
                    0 AS totalDebit
                FROM MoneyTransaction t
                WHERE t.businessDate = :businessDate
                  AND t.status = :status
                  AND t.destinationAccountId IN :accountIds

                UNION ALL

                SELECT
                    t.sourceAccountId AS accountId,
                    0 AS totalCredit,
                    t.amount AS totalDebit
                FROM MoneyTransaction t
                WHERE t.businessDate = :businessDate
                  AND t.status = :status
                  AND t.sourceAccountId IN :accountIds
            )
            GROUP BY accountId
            """)
    List<AccountTransactionMovementProjection>
    findAccountMovementsForReconciliation(
            @Param("businessDate") LocalDate businessDate,
            @Param("accountIds") List<UUID> accountIds,
            @Param("status") TransactionStatus status
    );
}