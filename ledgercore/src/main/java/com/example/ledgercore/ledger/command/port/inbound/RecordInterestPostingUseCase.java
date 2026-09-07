package com.example.ledgercore.ledger.command.port.inbound;

import com.example.ledgercore.ledger.command.dto.RecordInterestPostingCommand;

public interface RecordInterestPostingUseCase {

    void execute(
            RecordInterestPostingCommand command
    );
}