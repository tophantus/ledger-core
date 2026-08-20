package com.example.ledgercore.ledger.command.repository;

import com.example.ledgercore.ledger.entity.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LedgerEntryCommandRepository extends JpaRepository<LedgerEntry, UUID> {
}
