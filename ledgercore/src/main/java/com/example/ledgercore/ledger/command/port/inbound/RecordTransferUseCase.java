package com.example.ledgercore.ledger.command.port.inbound;

import com.example.ledgercore.ledger.command.dto.RecordTransferCommand;

public interface RecordTransferUseCase {

    void execute(RecordTransferCommand command);
}