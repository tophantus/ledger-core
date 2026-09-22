package com.example.ledgercore.card.command.handler;

import com.example.ledgercore.card.command.dto.AuthorizeCardPaymentByTokenCommand;
import com.example.ledgercore.card.command.dto.AuthorizeCardPaymentByTokenResult;
import com.example.ledgercore.card.command.port.inbound.AuthorizeCardPaymentByTokenUseCase;
import com.example.ledgercore.card.command.port.outbound.*;
import com.example.ledgercore.card.command.port.outbound.dto.CardAccountInfo;
import com.example.ledgercore.card.command.port.outbound.dto.CardCreditFacilityInfo;
import com.example.ledgercore.card.command.repository.CardAuthorizationCommandRepository;
import com.example.ledgercore.card.command.repository.CardCommandRepository;
import com.example.ledgercore.card.entity.Card;
import com.example.ledgercore.card.entity.CardAuthorization;
import com.example.ledgercore.card.enums.*;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.credit.enums.CreditFacilityStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthorizeCardPaymentByTokenHandler
        implements AuthorizeCardPaymentByTokenUseCase {

    private static final int AUTHORIZATION_EXPIRY_MINUTES = 7;

    private final CardCommandRepository cardCommandRepository;
    private final CardAuthorizationCommandRepository
            cardAuthorizationCommandRepository;

    private final ProviderAuthenticationPort providerAuthenticationPort;
    private final CardTokenPort cardTokenPort;
    private final CardAccountPort cardAccountPort;
    private final CardCreditFacilityPort cardCreditFacilityPort;
    private final AccountAuthorizationHoldPort accountAuthorizationHoldPort;
    private final CreditAuthorizationHoldPort creditAuthorizationHoldPort;

    @Override
    @Transactional
    public AuthorizeCardPaymentByTokenResult execute(
            AuthorizeCardPaymentByTokenCommand command
    ) {
        validateCommand(command);
        validateReference(command.reference());

        ProviderAuthenticationPort.ProviderAuthenticationResult provider =
                providerAuthenticationPort.authenticate(
                        command.providerClientId(),
                        command.providerCredential()
                );

        CardTokenPort.CardTokenInfo cardToken =
                cardTokenPort.resolve(
                        provider.providerId(),
                        command.token()
                );

        Card card = cardCommandRepository
                .findById(cardToken.cardId())
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.CARD_NOT_FOUND
                        )
                );

        validateCard(card);

        Instant now = Instant.now();
        UUID authorizationId = UUID.randomUUID();
        Instant expiresAt =
                now.plusSeconds(
                        AUTHORIZATION_EXPIRY_MINUTES * 60L
                );

        UUID holdId =
                createHold(
                        card,
                        authorizationId,
                        command
                );

        CardAuthorizationHoldType holdType =
                card.getType() == CardType.DEBIT
                        ? CardAuthorizationHoldType.ACCOUNT
                        : CardAuthorizationHoldType.CREDIT;

        CardAuthorization authorization =
                CardAuthorization.builder()
                        .id(authorizationId)
                        .cardId(card.getId())
                        .reference(command.reference())
                        .providerId(provider.providerId())
                        .merchantReference(
                                command.merchantReference()
                        )
                        .authorizationMethod(
                                CardAuthorizationMethod.TOKEN
                        )
                        .amount(command.amount())
                        .currency(command.currency())
                        .status(
                                CardAuthorizationStatus.AUTHORIZED
                        )
                        .holdId(holdId)
                        .holdType(holdType)
                        .authorizedAt(now)
                        .expiresAt(expiresAt)
                        .capturedAt(null)
                        .createdAt(now)
                        .updatedAt(now)
                        .build();

        CardAuthorization savedAuthorization =
                cardAuthorizationCommandRepository.save(
                        authorization
                );

        return toResult(savedAuthorization);
    }

    private void validateCommand(
            AuthorizeCardPaymentByTokenCommand command
    ) {
        if (command == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (command.reference() == null
                || command.reference().isBlank()) {
            throw new BusinessException(
                    ErrorCode.CARD_AUTHORIZATION_REFERENCE_REQUIRED
            );
        }

        if (command.providerClientId() == null
                || command.providerClientId().isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (command.providerCredential() == null
                || command.providerCredential().isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (command.token() == null
                || command.token().isBlank()) {
            throw new BusinessException(
                    ErrorCode.CARD_TOKEN_REQUIRED
            );
        }

        if (command.amount() == null
                || command.amount().signum() <= 0) {
            throw new BusinessException(
                    ErrorCode.CARD_AUTHORIZATION_AMOUNT_INVALID
            );
        }

        if (command.currency() == null) {
            throw new BusinessException(
                    ErrorCode.CARD_AUTHORIZATION_CURRENCY_REQUIRED
            );
        }
    }

    private void validateReference(
            String reference
    ) {
        if (cardAuthorizationCommandRepository
                .existsByReference(reference)) {
            throw new BusinessException(
                    ErrorCode.CARD_AUTHORIZATION_REFERENCE_ALREADY_EXISTS
            );
        }
    }

    private void validateCard(Card card) {
        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new BusinessException(
                    ErrorCode.CARD_NOT_ACTIVE
            );
        }

        if (card.getType() == CardType.DEBIT) {
            if (card.getAccountId() == null) {
                throw new BusinessException(
                        ErrorCode.CARD_ACCOUNT_ID_REQUIRED
                );
            }
            return;
        }

        if (card.getType() == CardType.CREDIT) {
            if (card.getCreditFacilityId() == null) {
                throw new BusinessException(
                        ErrorCode.CARD_CREDIT_FACILITY_ID_REQUIRED
                );
            }
            return;
        }

        throw new BusinessException(
                ErrorCode.INVALID_REQUEST
        );
    }

    private UUID createHold(
            Card card,
            UUID authorizationId,
            AuthorizeCardPaymentByTokenCommand command
    ) {
        if (card.getType() == CardType.DEBIT) {
            return createDebitHold(
                    card,
                    authorizationId,
                    command
            );
        }

        if (card.getType() == CardType.CREDIT) {
            return createCreditHold(
                    card,
                    authorizationId,
                    command
            );
        }

        throw new BusinessException(
                ErrorCode.INVALID_REQUEST
        );
    }

    private UUID createDebitHold(
            Card card,
            UUID authorizationId,
            AuthorizeCardPaymentByTokenCommand command
    ) {
        CardAccountInfo account =
                cardAccountPort.getOwnedAccount(
                        card.getCustomerId(),
                        card.getAccountId()
                );

        if (account == null) {
            throw new BusinessException(
                    ErrorCode.ACCOUNT_NOT_FOUND
            );
        }

        if (!account.currency().equals(
                command.currency()
        )) {
            throw new BusinessException(
                    ErrorCode.CARD_AUTHORIZATION_CURRENCY_MISMATCH
            );
        }

        if (command.amount().compareTo(
                account.availableBalance()
        ) > 0) {
            throw new BusinessException(
                    ErrorCode.ACCOUNT_INSUFFICIENT_BALANCE
            );
        }

        AccountAuthorizationHoldPort.HoldResult hold =
                accountAuthorizationHoldPort.createHold(
                        card.getAccountId(),
                        authorizationId,
                        command.amount(),
                        command.currency()
                );

        return hold.holdId();
    }

    private UUID createCreditHold(
            Card card,
            UUID authorizationId,
            AuthorizeCardPaymentByTokenCommand command
    ) {
        CardCreditFacilityInfo facility =
                cardCreditFacilityPort.getOwnedCreditFacility(
                        card.getCustomerId(),
                        card.getCreditFacilityId()
                );

        if (facility == null) {
            throw new BusinessException(
                    ErrorCode.CREDIT_FACILITY_NOT_FOUND
            );
        }

        if (facility.status()
                != CreditFacilityStatus.ACTIVE) {
            throw new BusinessException(
                    ErrorCode.CREDIT_FACILITY_NOT_ACTIVE
            );
        }

        if (!facility.currency().equals(
                command.currency()
        )) {
            throw new BusinessException(
                    ErrorCode.CARD_AUTHORIZATION_CURRENCY_MISMATCH
            );
        }

        if (command.amount().compareTo(
                facility.availableCredit()
        ) > 0) {
            throw new BusinessException(
                    ErrorCode.CARD_AUTHORIZATION_INSUFFICIENT_CREDIT
            );
        }

        CreditAuthorizationHoldPort.CreditHoldResult hold =
                creditAuthorizationHoldPort.createHold(
                        card.getCreditFacilityId(),
                        authorizationId,
                        command.reference(),
                        command.amount(),
                        command.currency()
                );

        return hold.holdId();
    }

    private AuthorizeCardPaymentByTokenResult toResult(
            CardAuthorization authorization
    ) {
        return new AuthorizeCardPaymentByTokenResult(
                authorization.getId(),
                authorization.getCardId(),
                authorization.getReference(),
                authorization.getStatus(),
                authorization.getAmount().toPlainString(),
                authorization.getCurrency(),
                authorization.getAuthorizedAt(),
                authorization.getExpiresAt()
        );
    }
}
