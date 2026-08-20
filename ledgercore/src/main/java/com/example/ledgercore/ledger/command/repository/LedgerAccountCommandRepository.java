package com.example.ledgercore.ledger.command.repository;

import com.example.ledgercore.ledger.entity.LedgerAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LedgerAccountCommandRepository extends JpaRepository<LedgerAccount, UUID> {
}
