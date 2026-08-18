package com.example.ledgercore.user.command.port.inbound;

import com.example.ledgercore.user.command.dto.ActivateUserCommand;

public interface ActivateUserUseCase {

    void execute(ActivateUserCommand command);
}