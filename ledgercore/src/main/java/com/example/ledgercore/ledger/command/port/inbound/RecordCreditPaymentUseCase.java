package com.example.ledgercore.ledger.command.port.inbound;

import com.example.ledgercore.ledger.command.dto.RecordCreditPaymentCommand;

public interface RecordCreditPaymentUseCase {

    void execute(RecordCreditPaymentCommand command);
}