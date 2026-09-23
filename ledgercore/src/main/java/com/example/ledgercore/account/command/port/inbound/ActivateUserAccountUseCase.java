package com.example.ledgercore.account.command.port.inbound;

import com.example.ledgercore.account.command.dto.ActivateUserAccountCommand;

public interface ActivateUserAccountUseCase {

    void execute(ActivateUserAccountCommand command);
}