package com.example.ledgercore.account.command.port.inbound;

import com.example.ledgercore.account.command.dto.BlockAccountCommand;

public interface BlockAccountUseCase {

    void execute(BlockAccountCommand command);
}