package com.example.ledgercore.credit.command.port.inbound;

import com.example.ledgercore.credit.command.dto.RejectCreditOfferCommand;
import com.example.ledgercore.credit.command.dto.RejectCreditOfferResult;

public interface RejectCreditOfferUseCase {

    RejectCreditOfferResult execute(
            RejectCreditOfferCommand command
    );
}