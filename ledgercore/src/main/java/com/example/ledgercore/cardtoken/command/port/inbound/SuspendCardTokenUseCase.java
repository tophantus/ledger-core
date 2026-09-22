package com.example.ledgercore.cardtoken.command.port.inbound;

import com.example.ledgercore.cardtoken.command.dto.SuspendCardTokenCommand;

public interface SuspendCardTokenUseCase {

    void execute(SuspendCardTokenCommand command);
}