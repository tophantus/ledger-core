package com.example.ledgercore.ledger.command.port.inbound;

import com.example.ledgercore.ledger.command.dto.RecordInterestAccrualJournalCommand;

import java.util.UUID;

public interface RecordInterestAccrualJournalUseCase {

    UUID execute(
            RecordInterestAccrualJournalCommand command
    );
}