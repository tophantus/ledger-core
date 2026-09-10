package com.example.ledgercore.withdrawal.query.repository;

import com.example.ledgercore.withdrawal.entity.WithdrawalLookupCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WithdrawalLookupCodeQueryRepository
        extends JpaRepository<WithdrawalLookupCode, UUID> {

    Optional<WithdrawalLookupCode> findByLookupCode(
            String lookupCode
    );
}