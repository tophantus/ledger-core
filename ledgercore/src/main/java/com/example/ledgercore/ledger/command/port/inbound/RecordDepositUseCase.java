package com.example.ledgercore.ledger.command.port.inbound;

import com.example.ledgercore.ledger.command.dto.RecordDepositCommand;

public interface RecordDepositUseCase {

    void execute(RecordDepositCommand command);
}