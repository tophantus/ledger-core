package com.example.ledgercore.account.query.repository;

import com.example.ledgercore.account.entity.ProviderAccount;
import com.example.ledgercore.account.query.projection.ProviderAccountProjection;
import com.example.ledgercore.common.currency.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProviderAccountQueryRepository
        extends JpaRepository<ProviderAccount, UUID> {

    @Query(
            value = """
                    SELECT
                        pa.provider_id AS providerId,

                        a.id AS accountId,
                        a.product_id AS productId,
                        a.account_no AS accountNo,
                        a.currency AS currency,
                        a.balance AS balance,
                        a.hold_amount AS holdAmount,
                        a.status AS status,
                        a.created_at AS createdAt,
                        a.updated_at AS updatedAt,
                        a.ledger_account_id AS ledgerAccountId

                    FROM provider_accounts pa

                    JOIN accounts a
                        ON a.id = pa.account_id

                    WHERE pa.provider_id = :providerId
                      AND a.currency = :currency
                    """,
            nativeQuery = true
    )
    Optional<ProviderAccountProjection> findByProviderIdAndCurrency(
            @Param("providerId") UUID providerId,
            @Param("currency") Currency currency
    );
}