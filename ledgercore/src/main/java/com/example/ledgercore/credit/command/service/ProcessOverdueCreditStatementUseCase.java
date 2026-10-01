package com.example.ledgercore.credit.command.service;

import java.util.UUID;

public interface ProcessOverdueCreditStatementUseCase {

    void execute(UUID statementId);
}