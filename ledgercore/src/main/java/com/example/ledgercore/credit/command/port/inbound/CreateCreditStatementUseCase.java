package com.example.ledgercore.credit.command.port.inbound;

import com.example.ledgercore.credit.command.dto.CreateCreditStatementCommand;

public interface CreateCreditStatementUseCase {

    void execute(CreateCreditStatementCommand command);
}
