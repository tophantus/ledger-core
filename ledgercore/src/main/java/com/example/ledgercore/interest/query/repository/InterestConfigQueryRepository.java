package com.example.ledgercore.interest.query.repository;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.interest.entity.InterestConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface InterestConfigQueryRepository
        extends JpaRepository<InterestConfig, UUID> {

    @Query("""
        SELECT c
        FROM InterestConfig c
        WHERE c.productId = :productId
          AND c.currency = :currency
          AND c.effectiveFrom <= :businessDate
          AND (
                c.effectiveTo IS NULL
                OR :businessDate < c.effectiveTo
              )
        ORDER BY c.effectiveFrom DESC
        """)
    Optional<InterestConfig> findApplicableConfig(
            @Param("productId") UUID productId,
            @Param("currency") Currency currency,
            @Param("businessDate") LocalDate businessDate
    );
}