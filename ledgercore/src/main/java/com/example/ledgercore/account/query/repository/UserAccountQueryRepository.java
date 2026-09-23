package com.example.ledgercore.account.query.repository;

import com.example.ledgercore.account.entity.UserAccount;
import com.example.ledgercore.account.enums.AccountStatus;
import com.example.ledgercore.account.query.projection.AdminUserAccountProjection;
import com.example.ledgercore.account.query.projection.UserAccountProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserAccountQueryRepository
        extends JpaRepository<UserAccount, UUID> {

    boolean existsByAccountIdAndUserId(
            UUID accountId,
            UUID userId
    );

    @Query("""
        SELECT
            a.id AS accountId,
            ua.userId AS userId,
            a.productId AS productId,
            a.accountNo AS accountNo,
            a.currency AS currency,
            a.balance AS balance,
            a.holdAmount AS holdAmount,
            a.status AS status,
            a.version AS version,
            a.ledgerAccountId AS ledgerAccountId,
            a.createdAt AS createdAt,
            a.updatedAt AS updatedAt
        FROM UserAccount ua
        JOIN Account a
            ON a.id = ua.accountId
        WHERE ua.accountId = :accountId
        """)
    Optional<UserAccountProjection> findUserAccount(
            @Param("accountId") UUID accountId
    );

    Optional<UserAccount> findByAccountId(
            UUID accountId
    );

    @Query("""
        SELECT ua.accountId
        FROM UserAccount ua
        WHERE ua.userId = :userId
        """)
    List<UUID> findAccountIdsByUserId(
            @Param("userId") UUID userId
    );

    @Query("""
        SELECT
            a.id AS accountId,
            ua.userId AS userId,
            a.productId AS productId,
            a.accountNo AS accountNo,
            a.currency AS currency,
            a.balance AS balance,
            a.holdAmount AS holdAmount,
            a.status AS status,
            a.version AS version,
            a.ledgerAccountId AS ledgerAccountId,
            a.createdAt AS createdAt,
            a.updatedAt AS updatedAt
        FROM UserAccount ua
        JOIN Account a
            ON a.id = ua.accountId
        WHERE ua.userId = :userId
          AND a.status <> :status
        """)
    List<UserAccountProjection> findAllByUserIdAndStatusNot(
            @Param("userId") UUID userId,
            @Param("status") AccountStatus status
    );

    @Query(
            value = """
            SELECT
                a.id AS account_id,
                ua.user_id AS user_id,
                a.product_id AS product_id,
                a.account_no AS account_no,
                a.currency AS currency,
                a.balance AS balance,
                a.hold_amount AS hold_amount,
                a.status AS status,
                a.version AS version,
                a.ledger_account_id AS ledger_account_id,
                a.created_at AS created_at,
                a.updated_at AS updated_at
            FROM user_accounts ua
            INNER JOIN accounts a
                ON a.id = ua.account_id
            WHERE (
                :userId IS NULL
                OR ua.user_id = :userId
            )
            AND (
                :accountNo IS NULL
                OR :accountNo = ''
                OR a.account_no ILIKE CONCAT('%', :accountNo, '%')
            )
            AND (
                :status IS NULL
                OR a.status = :status
            )
            AND (
                :currency IS NULL
                OR a.currency = :currency
            )
            ORDER BY a.created_at DESC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM user_accounts ua
            INNER JOIN accounts a
                ON a.id = ua.account_id
            WHERE (
                :userId IS NULL
                OR ua.user_id = :userId
            )
            AND (
                :accountNo IS NULL
                OR :accountNo = ''
                OR a.account_no ILIKE CONCAT('%', :accountNo, '%')
            )
            AND (
                :status IS NULL
                OR a.status = :status
            )
            AND (
                :currency IS NULL
                OR a.currency = :currency
            )
            """,
            nativeQuery = true
    )
    Page<AdminUserAccountProjection> findAdminUserAccounts(
            @Param("userId") UUID userId,
            @Param("accountNo") String accountNo,
            @Param("status") String status,
            @Param("currency") String currency,
            Pageable pageable
    );
}