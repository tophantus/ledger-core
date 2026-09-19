package com.example.ledgercore.credit.command.port.inbound;

import java.util.UUID;

public interface EvaluateAndCreateCreditOfferUseCase {

    void execute(
            UUID runId,
            UUID customerId
    );
}