package com.example.ledgercore.account.command.port.inbound;

import com.example.ledgercore.account.command.dto.CloseAccountCommand;

public interface CloseAccountUseCase {

    void execute(CloseAccountCommand command);
}