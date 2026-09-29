package com.example.ledgercore.interest.query.repository;

import com.example.ledgercore.interest.entity.CreditInterestAccrual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface CreditInterestAccrualQueryRepository
        extends JpaRepository<CreditInterestAccrual, UUID> {

    @Query("""
            SELECT SUM(accrual.interestAmount)
            FROM CreditInterestAccrual accrual
            WHERE accrual.creditFacilityId = :creditFacilityId
              AND accrual.businessDate BETWEEN :fromDate AND :toDate
            """)
    BigDecimal sumInterestAmountByCreditFacilityIdAndBusinessDateBetween(
            @Param("creditFacilityId") UUID creditFacilityId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );
}
