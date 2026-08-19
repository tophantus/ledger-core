package com.example.ledgercore.account.command.port.inbound;

import com.example.ledgercore.account.command.dto.ActivateAccountCommand;

public interface ActivateAccountUseCase {

    void execute(ActivateAccountCommand command);
}