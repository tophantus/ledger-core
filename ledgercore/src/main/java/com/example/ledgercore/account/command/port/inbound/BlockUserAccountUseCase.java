package com.example.ledgercore.account.command.port.inbound;

import com.example.ledgercore.account.command.dto.BlockUserAccountCommand;

public interface BlockUserAccountUseCase {

    void execute(BlockUserAccountCommand command);
}