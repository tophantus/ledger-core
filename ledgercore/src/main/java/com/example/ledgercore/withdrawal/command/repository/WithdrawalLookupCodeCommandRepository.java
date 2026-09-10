package com.example.ledgercore.withdrawal.command.repository;

import com.example.ledgercore.withdrawal.entity.WithdrawalLookupCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.UUID;

public interface WithdrawalLookupCodeCommandRepository
        extends JpaRepository<WithdrawalLookupCode, UUID> {

    boolean existsByLookupCode(String lookupCode);

    @Modifying
    @Query("""
            DELETE FROM WithdrawalLookupCode w
            WHERE w.expiresAt <= :now
            """)
    int deleteExpired(Instant now);
}