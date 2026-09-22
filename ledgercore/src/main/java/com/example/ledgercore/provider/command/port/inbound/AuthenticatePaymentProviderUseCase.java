package com.example.ledgercore.provider.command.port.inbound;

import com.example.ledgercore.provider.command.dto.AuthenticatePaymentProviderCommand;
import com.example.ledgercore.provider.command.dto.AuthenticatePaymentProviderResult;

public interface AuthenticatePaymentProviderUseCase {

    AuthenticatePaymentProviderResult execute(
            AuthenticatePaymentProviderCommand command
    );
}