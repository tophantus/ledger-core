package com.example.ledgercore.card.command.handler;

import com.example.ledgercore.card.command.dto.CaptureCardPaymentCommand;
import com.example.ledgercore.card.command.dto.CaptureCardPaymentResult;
import com.example.ledgercore.card.command.port.inbound.CaptureCardPaymentUseCase;
import com.example.ledgercore.card.command.port.outbound.CardProviderAccountPort;
import com.example.ledgercore.card.command.port.outbound.CardProviderTransferPort;
import com.example.ledgercore.card.command.port.outbound.CardReleaseHoldPort;
import com.example.ledgercore.card.command.port.outbound.ProviderAuthenticationPort;
import com.example.ledgercore.card.command.repository.CardAuthorizationCommandRepository;
import com.example.ledgercore.card.command.repository.CardCaptureCommandRepository;
import com.example.ledgercore.card.command.repository.CardCommandRepository;
import com.example.ledgercore.card.entity.Card;
import com.example.ledgercore.card.entity.CardAuthorization;
import com.example.ledgercore.card.entity.CardCapture;
import com.example.ledgercore.card.enums.CardAuthorizationStatus;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CaptureCardPaymentHandler
        implements CaptureCardPaymentUseCase {

    private final CardAuthorizationCommandRepository
            cardAuthorizationCommandRepository;
    private final CardCommandRepository
            cardCommandRepository;
    private final CardCaptureCommandRepository
            cardCaptureCommandRepository;

    private final ProviderAuthenticationPort
            providerAuthenticationPort;
    private final CardProviderAccountPort
            cardProviderAccountPort;
    private final CardProviderTransferPort
            cardProviderTransferPort;
    private final CardReleaseHoldPort
            cardReleaseHoldPort;

    private final Clock clock;

    @Override
    @Transactional
    public CaptureCardPaymentResult execute(
            CaptureCardPaymentCommand command
    ) {
        validateCommand(command);

        ProviderAuthenticationPort.ProviderAuthenticationResult provider =
                providerAuthenticationPort.authenticate(
                        command.providerClientId(),
                        command.providerCredential()
                );

        CardAuthorization authorization =
                cardAuthorizationCommandRepository
                        .findByIdForUpdate(command.authorizationId())
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.CARD_NOT_FOUND
                                )
                        );

        Card card = cardCommandRepository
                .findById(authorization.getCardId())
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.CARD_NOT_FOUND
                        )
                );

        if (!provider.providerId().equals(
                authorization.getProviderId()
        )) {
            throw new BusinessException(
                    ErrorCode.CARD_CAPTURE_PROVIDER_MISMATCH
            );
        }

        Instant now = Instant.now(clock);
        if (authorization.getStatus() != CardAuthorizationStatus.AUTHORIZED
                || authorization.isExpired(now)) {
            throw new BusinessException(
                    ErrorCode.CARD_CAPTURE_NOT_ALLOWED
            );
        }

        UUID providerAccountId =
                cardProviderAccountPort.getProviderAccountId(
                        provider.providerId(),
                        authorization.getCurrency()
                );

        cardReleaseHoldPort.releaseHold(
                authorization.getHoldType(),
                authorization.getHoldId()
        );

        UUID transactionId =
                createProviderTransfer(
                        command,
                        authorization,
                        card,
                        providerAccountId
                );

        CardCapture capture =
                CardCapture.builder()
                        .id(UUID.randomUUID())
                        .authorizationId(authorization.getId())
                        .transactionId(transactionId)
                        .amount(authorization.getAmount())
                        .capturedAt(now)
                        .createdAt(now)
                        .build();

        cardCaptureCommandRepository.save(capture);
        authorization.capture(now);

        return new CaptureCardPaymentResult(
                capture.getId(),
                authorization.getId(),
                transactionId,
                authorization.getStatus(),
                authorization.getAmount(),
                now
        );
    }

    private void validateCommand(
            CaptureCardPaymentCommand command
    ) {
        if (command == null
                || command.providerClientId() == null
                || command.providerClientId().isBlank()
                || command.providerCredential() == null
                || command.providerCredential().isBlank()
                || command.authorizationId() == null
                || command.reference() == null
                || command.reference().isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    private UUID createProviderTransfer(
            CaptureCardPaymentCommand command,
            CardAuthorization authorization,
            Card card,
            UUID providerAccountId
    ) {
        boolean hasAccount = card.getAccountId() != null;
        boolean hasCredit = card.getCreditFacilityId() != null;

        if (hasAccount == hasCredit) {
            throw new BusinessException(
                    ErrorCode.CARD_CAPTURE_INVALID_FUNDING_SOURCE
            );
        }

        if (hasAccount) {
            return cardProviderTransferPort.transferFromDebitCard(
                    card.getAccountId(),
                    providerAccountId,
                    authorization.getAmount(),
                    authorization.getCurrency(),
                    command.reference(),
                    command.description()
            );
        }

        return cardProviderTransferPort.transferFromCreditCard(
                card.getCreditFacilityId(),
                providerAccountId,
                authorization.getAmount(),
                authorization.getCurrency(),
                command.reference(),
                command.description()
        );
    }
}
