package com.example.ledgercore.interest.query.repository;

import com.example.ledgercore.interest.entity.InterestAccrual;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface InterestAccrualQueryRepository
        extends JpaRepository<InterestAccrual, UUID>,
        JpaSpecificationExecutor<InterestAccrual> {

    @Query("""
        SELECT DISTINCT accrual.accountId
        FROM InterestAccrual accrual
        WHERE accrual.businessDate BETWEEN :periodStart AND :periodEnd
          AND (
              :lastProcessedId IS NULL
              OR accrual.accountId > :lastProcessedId
          )
        ORDER BY accrual.accountId
        """)
    List<UUID> findDistinctAccountIds(
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd,
            @Param("lastProcessedId") UUID lastProcessedId,
            Pageable pageable
    );
}