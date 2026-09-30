package com.example.ledgercore.credit.command.service;

import com.example.ledgercore.credit.command.service.dto.CreateCreditStatementCommand;

public interface CreateCreditStatementService {

    void execute(CreateCreditStatementCommand command);
}
