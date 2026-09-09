package com.example.ledgercore.hold.command.port.inbound;

import com.example.ledgercore.hold.command.dto.ReleaseAccountHoldCommand;

public interface ReleaseAccountHoldUseCase {

    void execute(
            ReleaseAccountHoldCommand command
    );
}