package com.example.ledgercore.ledger.command.port.inbound;

import com.example.ledgercore.ledger.command.dto.RecordWithdrawCommand;

public interface RecordWithdrawUseCase {

    void execute(RecordWithdrawCommand command);
}