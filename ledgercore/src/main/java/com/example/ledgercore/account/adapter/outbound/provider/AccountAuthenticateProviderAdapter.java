package com.example.ledgercore.account.adapter.outbound.provider;

import com.example.ledgercore.account.query.port.outbound.AccountAuthenticateProviderPort;
import com.example.ledgercore.provider.command.dto.AuthenticatePaymentProviderCommand;
import com.example.ledgercore.provider.command.dto.AuthenticatePaymentProviderResult;
import com.example.ledgercore.provider.command.port.inbound.AuthenticatePaymentProviderUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountAuthenticateProviderAdapter
        implements AccountAuthenticateProviderPort {

    private final AuthenticatePaymentProviderUseCase
            authenticatePaymentProviderUseCase;

    @Override
    public UUID authenticate(
            String clientId,
            String credential
    ) {
        AuthenticatePaymentProviderResult result =
                authenticatePaymentProviderUseCase.execute(
                        new AuthenticatePaymentProviderCommand(
                                clientId,
                                credential
                        )
                );

        return result.providerId();
    }
}