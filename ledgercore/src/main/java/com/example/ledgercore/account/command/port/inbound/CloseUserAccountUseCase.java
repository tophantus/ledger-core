package com.example.ledgercore.account.command.port.inbound;

import com.example.ledgercore.account.command.dto.CloseUserAccountCommand;

public interface CloseUserAccountUseCase {

    void execute(CloseUserAccountCommand command);
}