package com.example.ledgercore.account.command.port.inbound;

import com.example.ledgercore.account.command.dto.IncreaseAccountBalanceCommand;

public interface IncreaseAccountBalanceUseCase {

    void execute(IncreaseAccountBalanceCommand command);
}