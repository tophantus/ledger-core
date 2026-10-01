package com.example.ledgercore.ledger.command.port.inbound;

import com.example.ledgercore.ledger.command.dto.RecordCreditFeeCommand;

public interface RecordCreditFeeUseCase {

    void execute(RecordCreditFeeCommand command);
}