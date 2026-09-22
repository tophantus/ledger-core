package com.example.ledgercore.cardtoken.command.handler;

import com.example.ledgercore.cardtoken.command.dto.SuspendCardTokenCommand;
import com.example.ledgercore.cardtoken.command.port.inbound.SuspendCardTokenUseCase;
import com.example.ledgercore.cardtoken.command.port.outbound.ProviderAuthenticationPort;
import com.example.ledgercore.cardtoken.command.repository.CardTokenCommandRepository;
import com.example.ledgercore.cardtoken.entity.CardToken;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SuspendCardTokenHandler
        implements SuspendCardTokenUseCase {

    private final CardTokenCommandRepository
            cardTokenCommandRepository;

    private final ProviderAuthenticationPort
            providerAuthenticationPort;

    private final Clock clock;

    @Override
    @Transactional
    public void execute(
            SuspendCardTokenCommand command
    ) {
        validateCommand(command);

        String clientId =
                command.clientId().trim();

        String tokenValue =
                command.token().trim();

        ProviderAuthenticationPort.ProviderAuthenticationResult provider =
                providerAuthenticationPort.authenticate(
                        clientId,
                        command.credential()
                );

        CardToken cardToken =
                cardTokenCommandRepository
                        .findByTokenAndProviderId(
                                tokenValue,
                                provider.providerId()
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.CARD_TOKEN_NOT_FOUND
                                )
                        );

        Instant now = Instant.now(clock);

        cardToken.suspend(now);

        cardTokenCommandRepository.save(cardToken);
    }

    private void validateCommand(
            SuspendCardTokenCommand command
    ) {
        if (command == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (command.clientId() == null
                || command.clientId().isBlank()) {
            throw new BusinessException(
                    ErrorCode.PROVIDER_CLIENT_ID_REQUIRED
            );
        }

        if (command.credential() == null
                || command.credential().isBlank()) {
            throw new BusinessException(
                    ErrorCode.PROVIDER_CREDENTIAL_REQUIRED
            );
        }

        if (command.token() == null
                || command.token().isBlank()) {
            throw new BusinessException(
                    ErrorCode.CARD_TOKEN_REQUIRED
            );
        }
    }
}