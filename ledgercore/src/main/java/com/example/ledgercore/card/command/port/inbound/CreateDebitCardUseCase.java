package com.example.ledgercore.card.command.port.inbound;

import com.example.ledgercore.card.command.dto.CreateDebitCardCommand;
import com.example.ledgercore.card.command.dto.CreateDebitCardResult;

public interface CreateDebitCardUseCase {

    CreateDebitCardResult execute(
            CreateDebitCardCommand command
    );
}