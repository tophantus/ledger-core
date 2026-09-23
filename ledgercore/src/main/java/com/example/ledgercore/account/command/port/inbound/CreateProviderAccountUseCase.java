package com.example.ledgercore.account.command.port.inbound;

import com.example.ledgercore.account.command.dto.CreateProviderAccountCommand;
import com.example.ledgercore.account.query.dto.AccountResponse;

public interface CreateProviderAccountUseCase {

    AccountResponse execute(
            CreateProviderAccountCommand command
    );
}