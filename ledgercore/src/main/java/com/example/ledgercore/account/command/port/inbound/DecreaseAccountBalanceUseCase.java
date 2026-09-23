package com.example.ledgercore.account.command.port.inbound;

import com.example.ledgercore.account.command.dto.DecreaseAccountBalanceCommand;

public interface DecreaseAccountBalanceUseCase {

    void execute(DecreaseAccountBalanceCommand command);
}