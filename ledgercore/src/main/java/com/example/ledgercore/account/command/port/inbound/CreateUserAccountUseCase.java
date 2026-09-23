package com.example.ledgercore.account.command.port.inbound;

import com.example.ledgercore.account.command.dto.CreatUserAccountCommand;
import com.example.ledgercore.account.query.dto.AccountResponse;

public interface CreateUserAccountUseCase {

    AccountResponse execute(CreatUserAccountCommand command);
}