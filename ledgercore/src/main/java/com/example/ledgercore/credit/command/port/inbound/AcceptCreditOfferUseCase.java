package com.example.ledgercore.credit.command.port.inbound;

import com.example.ledgercore.credit.command.dto.AcceptCreditOfferCommand;
import com.example.ledgercore.credit.command.dto.AcceptCreditOfferResult;

public interface AcceptCreditOfferUseCase {

    AcceptCreditOfferResult execute(
            AcceptCreditOfferCommand command
    );
}