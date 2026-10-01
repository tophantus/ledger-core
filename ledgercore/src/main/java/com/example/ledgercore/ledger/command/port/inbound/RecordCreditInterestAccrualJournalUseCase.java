package com.example.ledgercore.ledger.command.port.inbound;

import com.example.ledgercore.ledger.command.dto.RecordCreditInterestAccrualJournalCommand;

import java.util.UUID;

public interface RecordCreditInterestAccrualJournalUseCase {

    UUID execute(
            RecordCreditInterestAccrualJournalCommand command
    );
}