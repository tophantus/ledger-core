package com.example.ledgercore.card.command.port.inbound;

import com.example.ledgercore.card.command.dto.CreateCreditCardCommand;
import com.example.ledgercore.card.command.dto.CreateCreditCardResult;

public interface CreateCreditCardUseCase {

    CreateCreditCardResult execute(
            CreateCreditCardCommand command
    );
}