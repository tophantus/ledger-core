package com.example.ledgercore.hold.command.port.inbound;

import com.example.ledgercore.hold.command.dto.ReleaseCreditHoldCommand;

public interface ReleaseCreditHoldUseCase {

    void execute(ReleaseCreditHoldCommand command);
}