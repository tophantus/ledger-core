package com.example.ledgercore.account.command.port.inbound;

import com.example.ledgercore.account.command.dto.IncreaseAccountHoldCommand;

public interface IncreaseAccountHoldUseCase {

    void execute(
            IncreaseAccountHoldCommand command
    );
}