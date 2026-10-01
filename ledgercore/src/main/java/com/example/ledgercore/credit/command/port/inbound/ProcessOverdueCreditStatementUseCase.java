package com.example.ledgercore.credit.command.port.inbound;

import java.util.UUID;

public interface ProcessOverdueCreditStatementUseCase {

    void execute(UUID statementId);
}