package com.example.ledgercore.cardtoken.command.port.inbound;

import com.example.ledgercore.cardtoken.command.dto.RevokeCardTokenCommand;

public interface RevokeCardTokenUseCase {

    void execute(RevokeCardTokenCommand command);
}