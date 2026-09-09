package com.example.ledgercore.hold.command.port.inbound;

import com.example.ledgercore.hold.command.dto.CreateAccountHoldCommand;
import com.example.ledgercore.hold.command.dto.CreateAccountHoldResponse;

public interface CreateAccountHoldUseCase {

    CreateAccountHoldResponse execute(
            CreateAccountHoldCommand command
    );
}