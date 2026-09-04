package com.example.ledgercore.transaction.query.repository;

import com.example.ledgercore.transaction.entity.MoneyTransaction;
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
}