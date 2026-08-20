package com.example.ledgercore.account.command.port.inbound;

import com.example.ledgercore.account.command.dto.TransferAccountCommand;

public interface TransferAccountBalanceUseCase {

    void execute(TransferAccountCommand command);
}