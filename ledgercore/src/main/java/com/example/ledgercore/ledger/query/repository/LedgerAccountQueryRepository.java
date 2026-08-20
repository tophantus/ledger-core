package com.example.ledgercore.ledger.query.repository;

import com.example.ledgercore.ledger.entity.LedgerAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LedgerAccountQueryRepository
        extends JpaRepository<LedgerAccount, UUID> {

    Optional<LedgerAccount> findByCodeAndCurrency(
            String code,
            String currency
    );
}