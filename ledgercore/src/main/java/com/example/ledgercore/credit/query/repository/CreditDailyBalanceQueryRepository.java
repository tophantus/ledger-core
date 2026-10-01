package com.example.ledgercore.credit.query.repository;

import com.example.ledgercore.credit.entity.CreditDailyBalance;
import com.example.ledgercore.credit.query.projection.CreditFacilityInterestBalanceProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface CreditDailyBalanceQueryRepository
        extends JpaRepository<CreditDailyBalance, UUID> {

    @Query(
            value = """
                SELECT DISTINCT ON (credit_facility_id)
                    credit_facility_id AS creditFacilityId,
                    closing_balance AS closingBalance
                FROM credit_daily_balances
                WHERE credit_facility_id IN (:creditFacilityIds)
                  AND business_date <= :businessDate
                ORDER BY credit_facility_id, business_date DESC
                """,
            nativeQuery = true
    )
    List<CreditFacilityInterestBalanceProjection>
    findEffectiveClosingBalances(
            @Param("creditFacilityIds") List<UUID> creditFacilityIds,
            @Param("businessDate") LocalDate businessDate
    );

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
