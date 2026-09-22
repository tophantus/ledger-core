package com.example.ledgercore.card.adapter.outbound.provider;

import com.example.ledgercore.card.command.port.outbound.ProviderAuthenticationPort;
import com.example.ledgercore.provider.command.dto.AuthenticatePaymentProviderCommand;
import com.example.ledgercore.provider.command.dto.AuthenticatePaymentProviderResult;
import com.example.ledgercore.provider.command.port.inbound.AuthenticatePaymentProviderUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CardToProviderAuthenticationAdapter
        implements ProviderAuthenticationPort {

    private final AuthenticatePaymentProviderUseCase
            authenticatePaymentProviderUseCase;

    @Override
    public ProviderAuthenticationResult authenticate(
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

        return new ProviderAuthenticationResult(
                result.providerId(),
                result.code(),
                result.name(),
                result.type()
        );
    }
}
