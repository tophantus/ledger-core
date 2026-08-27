package com.example.ledgercore.transaction.query.repository;

import com.example.ledgercore.transaction.entity.MoneyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface TransactionQueryRepository
        extends JpaRepository<MoneyTransaction, UUID>,
                JpaSpecificationExecutor<MoneyTransaction> {

    Optional<MoneyTransaction> findByReference(
            String reference
    );
}