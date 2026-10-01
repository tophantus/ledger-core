package com.example.ledgercore.credit.command.port.inbound;

import com.example.ledgercore.credit.command.dto.run.ClaimedCreditStatementRun;

public interface ProcessCreditStatementRunUseCase {

    void process(ClaimedCreditStatementRun run);
}