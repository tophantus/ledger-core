package com.example.ledgercore.ledger.command.port.inbound;

import com.example.ledgercore.ledger.command.dto.RecordCreditPurchaseCommand;

public interface RecordCreditPurchaseUseCase {

    void execute(RecordCreditPurchaseCommand command);
}