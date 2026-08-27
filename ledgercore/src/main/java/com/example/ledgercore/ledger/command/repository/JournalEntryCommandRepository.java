package com.example.ledgercore.ledger.command.repository;

import com.example.ledgercore.ledger.entity.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JournalEntryCommandRepository
        extends JpaRepository<JournalEntry, UUID> {
}