package com.example.ledgercore.cardtoken.command.handler;

import com.example.ledgercore.cardtoken.command.dto.ProvisionCardTokenCommand;
import com.example.ledgercore.cardtoken.command.dto.ProvisionCardTokenResult;
import com.example.ledgercore.cardtoken.command.port.inbound.ProvisionCardTokenUseCase;
import com.example.ledgercore.cardtoken.command.port.outbound.CardVerificationPort;
import com.example.ledgercore.cardtoken.command.port.outbound.ProviderAuthenticationPort;
import com.example.ledgercore.cardtoken.command.repository.CardTokenCommandRepository;
import com.example.ledgercore.cardtoken.command.service.CardTokenGenerator;
import com.example.ledgercore.cardtoken.entity.CardToken;
import com.example.ledgercore.cardtoken.enums.CardTokenStatus;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ProvisionCardTokenHandler
        implements ProvisionCardTokenUseCase {

    private final CardTokenCommandRepository cardTokenCommandRepository;
    private final ProviderAuthenticationPort providerAuthenticationPort;
    private final CardVerificationPort cardVerificationPort;
    private final CardTokenGenerator cardTokenGenerator;
    private final Clock clock;

    @Override
    @Transactional
    public ProvisionCardTokenResult execute(
            ProvisionCardTokenCommand command
    ) {
        validateCommand(command);

        String clientId = command.clientId().trim();
        String providerCustomerReference =
                command.providerCustomerReference().trim();

        ProviderAuthenticationPort.ProviderAuthenticationResult provider =
                providerAuthenticationPort.authenticate(
                        clientId,
                        command.credential()
                );

        CardVerificationPort.CardVerificationResult card =
                cardVerificationPort.verify(
                        command.pan(),
                        command.expiryMonth(),
                        command.expiryYear(),
                        command.cvv()
                );

        validateExistingToken(
                card.cardId(),
                provider.providerId(),
                providerCustomerReference
        );

        String token = generateUniqueToken();

        Instant now = Instant.now(clock);

        CardToken cardToken = CardToken.builder()
                .token(token)
                .cardId(card.cardId())
                .providerId(provider.providerId())
                .providerCustomerReference(
                        providerCustomerReference
                )
                .status(CardTokenStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();

        cardToken.activate(now);

        CardToken savedToken =
                cardTokenCommandRepository.save(cardToken);

        return new ProvisionCardTokenResult(
                savedToken.getId(),
                savedToken.getCardId(),
                savedToken.getToken(),
                savedToken.getProviderId(),
                savedToken.getProviderCustomerReference()
        );
    }

    private void validateCommand(
            ProvisionCardTokenCommand command
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

        if (command.pan() == null
                || command.pan().isBlank()) {
            throw new BusinessException(
                    ErrorCode.PAN_REQUIRED
            );
        }

        if (command.expiryMonth() == null) {
            throw new BusinessException(
                    ErrorCode.EXPIRY_MONTH_REQUIRED
            );
        }

        if (command.expiryYear() == null) {
            throw new BusinessException(
                    ErrorCode.EXPIRY_YEAR_REQUIRED
            );
        }

        if (command.cvv() == null
                || command.cvv().isBlank()) {
            throw new BusinessException(
                    ErrorCode.CVV_REQUIRED
            );
        }

        if (command.providerCustomerReference() == null
                || command.providerCustomerReference().isBlank()) {
            throw new BusinessException(
                    ErrorCode.CARD_TOKEN_PROVIDER_CUSTOMER_REFERENCE_REQUIRED
            );
        }
    }

    private void validateExistingToken(
            java.util.UUID cardId,
            java.util.UUID providerId,
            String providerCustomerReference
    ) {
        boolean exists =
                cardTokenCommandRepository
                        .existsByCardIdAndProviderIdAndProviderCustomerReferenceAndStatus(
                                cardId,
                                providerId,
                                providerCustomerReference,
                                CardTokenStatus.ACTIVE
                        );

        if (exists) {
            throw new BusinessException(
                    ErrorCode.CARD_TOKEN_ALREADY_EXISTS
            );
        }
    }

    private String generateUniqueToken() {
        String token;

        do {
            token = cardTokenGenerator.generate();
        } while (
                cardTokenCommandRepository.existsByToken(token)
        );

        return token;
    }
}