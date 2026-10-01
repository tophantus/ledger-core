package com.example.ledgercore.ledger.command.port.inbound;

import com.example.ledgercore.ledger.command.dto.RecordCreditInterestPostingCommand;

public interface RecordCreditInterestPostingUseCase {

    void execute(RecordCreditInterestPostingCommand command);
}