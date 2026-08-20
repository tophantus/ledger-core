package com.example.ledgercore.ledger.command.port.inbound;

import com.example.ledgercore.ledger.command.dto.CreateLedgerAccountCommand;

import java.util.UUID;

public interface CreateLedgerAccountUseCase {

    UUID execute(CreateLedgerAccountCommand command);
}