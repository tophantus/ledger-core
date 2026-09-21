package com.example.ledgercore.card.command.port.inbound;

import com.example.ledgercore.card.command.dto.AuthorizeCardPaymentCommand;
import com.example.ledgercore.card.command.dto.AuthorizeCardPaymentResult;

public interface AuthorizeCardPaymentUseCase {

    AuthorizeCardPaymentResult execute(
            AuthorizeCardPaymentCommand command
    );
}