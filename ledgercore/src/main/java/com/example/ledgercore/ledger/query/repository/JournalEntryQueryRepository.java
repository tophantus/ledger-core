package com.example.ledgercore.ledger.query.repository;

import com.example.ledgercore.ledger.entity.JournalEntry;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface JournalEntryQueryRepository
        extends JpaRepository<JournalEntry, UUID> {

    @Query("""
            SELECT
                j.id AS id,
                j.sourceId AS transactionId,
                COALESCE(
                    SUM(
                        CASE
                            WHEN l.entryType =
                                com.example.ledgercore.ledger.enums.EntryType.DEBIT
                            THEN l.amount
                            ELSE 0
                        END
                    ),
                    0
                ) AS debitTotal,
                COALESCE(
                    SUM(
                        CASE
                            WHEN l.entryType =
                                com.example.ledgercore.ledger.enums.EntryType.CREDIT
                            THEN l.amount
                            ELSE 0
                        END
                    ),
                    0
                ) AS creditTotal,
                j.businessDate AS businessDate
            FROM JournalEntry j
            LEFT JOIN JournalEntryLine l
                ON l.journalEntryId = j.id
            WHERE j.sourceType =
                com.example.ledgercore.ledger.enums.JournalSourceType.TRANSACTION
              AND j.sourceId IN :transactionIds
            GROUP BY
                j.id,
                j.sourceId,
                j.businessDate
            """)
    List<JournalReconciliationProjection> findForBalanceReconciliation(
            @Param("transactionIds") List<UUID> transactionIds
    );

    @Query("""
        SELECT
            j.id AS id,
            COALESCE(
                SUM(
                    CASE
                        WHEN l.entryType =
                            com.example.ledgercore.ledger.enums.EntryType.DEBIT
                        THEN l.amount
                        ELSE 0
                    END
                ),
                0
            ) AS debitTotal,
            COALESCE(
                SUM(
                    CASE
                        WHEN l.entryType =
                            com.example.ledgercore.ledger.enums.EntryType.CREDIT
                        THEN l.amount
                        ELSE 0
                    END
                ),
                0
            ) AS creditTotal,
            j.businessDate AS businessDate
        FROM JournalEntry j
        LEFT JOIN JournalEntryLine l
            ON l.journalEntryId = j.id
        WHERE j.businessDate = :businessDate
          AND (
                :lastProcessedId IS NULL
                OR j.id > :lastProcessedId
          )
        GROUP BY
            j.id,
            j.businessDate
        ORDER BY j.id ASC
        """)
    List<JournalBalanceReconciliationProjection>
    findForBalanceReconciliation(
            @Param("businessDate") LocalDate businessDate,
            @Param("lastProcessedId") UUID lastProcessedId,
            Pageable pageable
    );
}