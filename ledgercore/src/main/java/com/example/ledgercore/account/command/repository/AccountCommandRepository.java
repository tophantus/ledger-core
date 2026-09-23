package com.example.ledgercore.account.command.repository;

import com.example.ledgercore.account.entity.Account;
import com.example.ledgercore.account.enums.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface AccountCommandRepository
        extends JpaRepository<Account, UUID> {

    @Modifying
    @Query("""
        UPDATE Account a
        SET a.status = :status,
            a.updatedAt = CURRENT_TIMESTAMP
        WHERE a.id = :accountId
        """)
    int updateStatus(
            @Param("accountId") UUID accountId,
            @Param("status") AccountStatus status
    );
}
