package com.example.ledgercore.hold.command.port.inbound;

import com.example.ledgercore.hold.command.dto.CreateCreditHoldCommand;
import com.example.ledgercore.hold.command.dto.CreateCreditHoldResponse;

public interface CreateCreditHoldUseCase {

    CreateCreditHoldResponse execute(
            CreateCreditHoldCommand command
    );
}