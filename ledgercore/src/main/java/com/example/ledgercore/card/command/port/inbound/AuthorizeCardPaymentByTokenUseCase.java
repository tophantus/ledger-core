package com.example.ledgercore.card.command.port.inbound;

import com.example.ledgercore.card.command.dto.AuthorizeCardPaymentByTokenCommand;
import com.example.ledgercore.card.command.dto.AuthorizeCardPaymentByTokenResult;

public interface AuthorizeCardPaymentByTokenUseCase {

    AuthorizeCardPaymentByTokenResult execute(
            AuthorizeCardPaymentByTokenCommand command
    );
}
