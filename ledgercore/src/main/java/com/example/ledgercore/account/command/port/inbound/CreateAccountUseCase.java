package com.example.ledgercore.account.command.port.inbound;

import com.example.ledgercore.account.command.dto.CreateAccountCommand;
import com.example.ledgercore.account.query.dto.AccountResponse;

public interface CreateAccountUseCase {

    AccountResponse execute(CreateAccountCommand command);
}