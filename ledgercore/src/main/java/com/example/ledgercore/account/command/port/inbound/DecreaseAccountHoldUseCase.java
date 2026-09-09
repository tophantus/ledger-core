package com.example.ledgercore.account.command.port.inbound;

import com.example.ledgercore.account.command.dto.DecreaseAccountHoldCommand;

public interface DecreaseAccountHoldUseCase {

    void execute(
            DecreaseAccountHoldCommand command
    );
}