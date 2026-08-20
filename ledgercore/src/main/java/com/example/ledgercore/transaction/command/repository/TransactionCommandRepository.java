package com.example.ledgercore.transaction.command.repository;

import com.example.ledgercore.transaction.entity.MoneyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TransactionCommandRepository
        extends JpaRepository<MoneyTransaction, UUID> {

    Optional<MoneyTransaction> findByReference(String reference);
}