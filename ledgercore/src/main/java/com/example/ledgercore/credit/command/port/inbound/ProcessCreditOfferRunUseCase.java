package com.example.ledgercore.credit.command.port.inbound;

import com.example.ledgercore.credit.command.dto.run.ClaimedCreditOfferRun;

public interface ProcessCreditOfferRunUseCase {

    void process(ClaimedCreditOfferRun run);
}