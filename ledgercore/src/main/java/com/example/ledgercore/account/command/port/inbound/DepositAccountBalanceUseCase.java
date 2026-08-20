package com.example.ledgercore.account.command.port.inbound;

import com.example.ledgercore.account.command.dto.DepositAccountCommand;

public interface DepositAccountBalanceUseCase {

    void execute(
            DepositAccountCommand command
    );
}