package com.example.ledgercore.ledger.command.port.inbound;

import com.example.ledgercore.ledger.command.dto.CreateCreditLedgerAccountCommand;

import java.util.UUID;

public interface CreateCreditLedgerAccountUseCase {

    UUID execute(
            CreateCreditLedgerAccountCommand command
    );
}