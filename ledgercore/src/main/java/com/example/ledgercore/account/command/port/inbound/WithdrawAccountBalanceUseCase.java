package com.example.ledgercore.account.command.port.inbound;

import com.example.ledgercore.account.command.dto.WithdrawAccountCommand;

public interface WithdrawAccountBalanceUseCase {

    void execute(WithdrawAccountCommand command);
}