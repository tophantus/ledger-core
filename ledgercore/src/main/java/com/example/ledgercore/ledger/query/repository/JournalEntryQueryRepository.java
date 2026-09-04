package com.example.ledgercore.ledger.query.repository;

import com.example.ledgercore.ledger.entity.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface JournalEntryQueryRepository
        extends JpaRepository<JournalEntry, UUID> {

    @Query("""
            SELECT
                j.id AS id,
                j.transactionId AS transactionId,
                COALESCE(
                    SUM(
                        CASE
                            WHEN l.entryType = com.example.ledgercore.ledger.enums.EntryType.DEBIT
                            THEN l.amount
                            ELSE 0
                        END
                    ),
                    0
                ) AS debitTotal,
                COALESCE(
                    SUM(
                        CASE
                            WHEN l.entryType = com.example.ledgercore.ledger.enums.EntryType.CREDIT
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
            WHERE j.transactionId IN :transactionIds
            GROUP BY
                j.id,
                j.transactionId,
                j.businessDate
            """)
    List<JournalReconciliationProjection> findForReconciliation(
            @Param("transactionIds") List<UUID> transactionIds
    );
}