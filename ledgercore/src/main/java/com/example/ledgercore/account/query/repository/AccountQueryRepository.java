package com.example.ledgercore.account.query.repository;

import com.example.ledgercore.account.entity.Account;
import com.example.ledgercore.account.enums.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountQueryRepository
        extends JpaRepository<Account, UUID>,
        JpaSpecificationExecutor<Account> {

    Optional<Account> findByAccountNo(String accountNo);

    @Query("""
            SELECT a.id
            FROM Account a
            WHERE a.userId = :userId
            ORDER BY a.id ASC
            """)
    List<UUID> findIdsByUserId(
            @Param("userId") UUID userId
    );

    List<Account> findAllByUserIdAndStatusNot(
            UUID userId,
            AccountStatus status
    );

    Optional<Account> findByIdAndUserId(
            UUID id,
            UUID userId
    );

    boolean existsByIdAndUserId(
            UUID id,
            UUID userId
    );

    @Query("""
            SELECT a
            FROM Account a
            WHERE a.status = :status
              AND (:lastProcessedId IS NULL
                   OR a.id > :lastProcessedId)
            ORDER BY a.id ASC
            """)
    List<Account> findInterestEligibleBatch(
            @Param("status") AccountStatus status,
            @Param("lastProcessedId") UUID lastProcessedId,
            org.springframework.data.domain.Pageable pageable
    );
}
