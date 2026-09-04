package com.example.ledgercore.reconciliation.command.repository;

import com.example.ledgercore.reconciliation.entity.ReconciliationException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReconciliationExceptionCommandRepository extends JpaRepository<ReconciliationException, UUID> {
}
