package com.example.ledgercore.credit.query.repository;

import com.example.ledgercore.credit.entity.CreditDailyBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface CreditDailyBalanceQueryRepository
        extends JpaRepository<CreditDailyBalance, UUID> {

    @Query(
            value = """
                    SELECT COALESCE(
                        (
                            SELECT closing_balance
                            FROM credit_daily_balances
                            WHERE credit_facility_id = :creditFacilityId
                              AND business_date <= :businessDate
                            ORDER BY business_date DESC
                            LIMIT 1
                        ),
                        0
                    )
                    """,
            nativeQuery = true
    )
    BigDecimal findEffectiveClosingBalance(
            @Param("creditFacilityId") UUID creditFacilityId,
            @Param("businessDate") LocalDate businessDate
    );
}
