package com.example.ledgercore.provider.command.port.inbound;

import com.example.ledgercore.provider.command.dto.RegisterPaymentProviderCommand;
import com.example.ledgercore.provider.command.dto.RegisterPaymentProviderResult;

public interface RegisterPaymentProviderUseCase {

    RegisterPaymentProviderResult execute(
            RegisterPaymentProviderCommand command
    );
}