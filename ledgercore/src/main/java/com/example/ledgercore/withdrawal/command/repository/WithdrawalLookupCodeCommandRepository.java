package com.example.ledgercore.withdrawal.command.repository;

import com.example.ledgercore.withdrawal.entity.WithdrawalLookupCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WithdrawalLookupCodeCommandRepository
        extends JpaRepository<WithdrawalLookupCode, UUID> {

    boolean existsByLookupCode(String lookupCode);
}