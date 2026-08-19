package com.example.ledgercore.account.command.port.inbound;

import com.example.ledgercore.account.command.dto.SuspendAccountCommand;

public interface SuspendAccountUseCase {

    void execute(SuspendAccountCommand command);
}