package com.example.ledgercore.ledger.command.repository;

import com.example.ledgercore.ledger.entity.JournalEntryLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JournalEntryLineCommandRepository
        extends JpaRepository<JournalEntryLine, UUID> {
}