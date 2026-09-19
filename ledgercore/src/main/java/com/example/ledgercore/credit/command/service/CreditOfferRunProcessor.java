package com.example.ledgercore.credit.command.service;

import com.example.ledgercore.credit.command.dto.ClaimedCreditOfferRun;

public interface CreditOfferRunProcessor {

    void process(ClaimedCreditOfferRun run);
}