package com.example.ledgercore.account.query.repository;

import com.example.ledgercore.account.entity.AccountDailyBalance;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AccountDailyBalanceQueryRepository
        extends JpaRepository<
        AccountDailyBalance,
        AccountDailyBalance.AccountDailyBalanceId> {

    @Query(
            value = """
                SELECT
                    current_balance.account_id AS accountId,
                    current_balance.business_date AS businessDate,

                    COALESCE(
                        previous_balance.closing_balance,
                        0
                    ) AS openingBalance,

                    current_balance.closing_balance AS closingBalance

                FROM account_daily_balances current_balance

                LEFT JOIN LATERAL (
                    SELECT
                        previous.closing_balance
                    FROM account_daily_balances previous
                    WHERE previous.account_id = current_balance.account_id
                      AND previous.business_date < :businessDate
                    ORDER BY previous.business_date DESC
                    LIMIT 1
                ) previous_balance ON TRUE

                WHERE current_balance.business_date = :businessDate
                  AND (
                        :lastProcessedId IS NULL
                        OR current_balance.account_id > :lastProcessedId
                  )

                ORDER BY current_balance.account_id ASC
                """,
            nativeQuery = true
    )
    List<AccountDailyBalanceReconciliationProjection>
    findForReconciliation(
            @Param("businessDate") LocalDate businessDate,
            @Param("lastProcessedId") UUID lastProcessedId,
            Pageable pageable
    );
}