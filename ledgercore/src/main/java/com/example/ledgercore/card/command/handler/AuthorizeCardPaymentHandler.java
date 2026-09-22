package com.example.ledgercore.card.command.handler;

import com.example.ledgercore.card.command.dto.AuthorizeCardPaymentCommand;
import com.example.ledgercore.card.command.dto.AuthorizeCardPaymentResult;
import com.example.ledgercore.card.command.port.inbound.AuthorizeCardPaymentUseCase;
import com.example.ledgercore.card.command.port.outbound.AccountAuthorizationHoldPort;
import com.example.ledgercore.card.command.port.outbound.CardAccountPort;
import com.example.ledgercore.card.command.port.outbound.CardCreditFacilityPort;
import com.example.ledgercore.card.command.port.outbound.CreditAuthorizationHoldPort;
import com.example.ledgercore.card.command.port.outbound.dto.CardAccountInfo;
import com.example.ledgercore.card.command.port.outbound.dto.CardCreditFacilityInfo;
import com.example.ledgercore.card.command.repository.CardAuthorizationCommandRepository;
import com.example.ledgercore.card.command.repository.CardCommandRepository;
import com.example.ledgercore.card.command.repository.CardVaultSecretCommandRepository;
import com.example.ledgercore.card.entity.Card;
import com.example.ledgercore.card.entity.CardAuthorization;
import com.example.ledgercore.card.entity.CardVaultSecret;
import com.example.ledgercore.card.enums.*;
import com.example.ledgercore.card.infrastructure.security.CardEncryptionService;
import com.example.ledgercore.card.infrastructure.security.CardPanHashService;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.credit.enums.CreditFacilityStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.YearMonth;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthorizeCardPaymentHandler
        implements AuthorizeCardPaymentUseCase {

    private static final int AUTHORIZATION_EXPIRY_MINUTES = 7;

    private final CardCommandRepository cardCommandRepository;
    private final CardAuthorizationCommandRepository
            cardAuthorizationCommandRepository;
    private final CardVaultSecretCommandRepository
            cardVaultSecretCommandRepository;

    private final CardAccountPort cardAccountPort;
    private final CardCreditFacilityPort cardCreditFacilityPort;

    private final AccountAuthorizationHoldPort
            accountAuthorizationHoldPort;
    private final CreditAuthorizationHoldPort
            creditAuthorizationHoldPort;

    private final CardPanHashService cardPanHashService;
    private final CardEncryptionService cardEncryptionService;

    @Override
    @Transactional
    public AuthorizeCardPaymentResult execute(
            AuthorizeCardPaymentCommand command
    ) {
        validateCommand(command);
        validateReference(command.reference());

        Card card = findCard(command.pan());

        validateCard(card);

        CardVaultSecret vaultSecret =
                cardVaultSecretCommandRepository
                        .findByCardId(card.getId())
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.CARD_VAULT_SECRET_NOT_FOUND
                                )
                        );

        verifyCardCredentials(
                command,
                card,
                vaultSecret
        );

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
                        command,
                        now
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
                        .merchantReference(
                                command.merchantReference()
                        )
                        .authorizationMethod(
                                CardAuthorizationMethod.PAN
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

    private Card findCard(String pan) {
        String panHash =
                cardPanHashService.hash(pan);

        return cardCommandRepository
                .findByPanHash(panHash)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.CARD_NOT_FOUND
                        )
                );
    }

    private void validateCommand(
            AuthorizeCardPaymentCommand command
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

        if (command.pan() == null
                || command.pan().isBlank()) {
            throw new BusinessException(
                    ErrorCode.CARD_AUTHORIZATION_PAN_REQUIRED
            );
        }

        if (command.cvv() == null
                || command.cvv().isBlank()) {
            throw new BusinessException(
                    ErrorCode.CARD_AUTHORIZATION_CVV_REQUIRED
            );
        }

        if (!isValidExpiry(
                command.expiryMonth(),
                command.expiryYear()
        )) {
            throw new BusinessException(
                    ErrorCode.CARD_AUTHORIZATION_EXPIRY_INVALID
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

    private boolean isValidExpiry(
            Short expiryMonth,
            Short expiryYear
    ) {
        return expiryMonth != null
                && expiryYear != null
                && expiryMonth >= 1
                && expiryMonth <= 12
                && expiryYear >= 1;
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

    private void verifyCardCredentials(
            AuthorizeCardPaymentCommand command,
            Card card,
            CardVaultSecret vaultSecret
    ) {
        String actualPan =
                cardEncryptionService.decrypt(
                        vaultSecret.getEncryptedPan(),
                        vaultSecret.getEncryptionVersion()
                );

        if (!actualPan.equals(command.pan())) {
            throw new BusinessException(
                    ErrorCode.CARD_AUTHORIZATION_PAN_INVALID
            );
        }

        String actualCvv =
                cardEncryptionService.decrypt(
                        vaultSecret.getEncryptedCvv(),
                        vaultSecret.getEncryptionVersion()
                );

        if (!actualCvv.equals(command.cvv())) {
            throw new BusinessException(
                    ErrorCode.CARD_AUTHORIZATION_CVV_INVALID
            );
        }

        int cardExpiryYear =
                card.getExpiryYear() % 100;

        if (!command.expiryMonth().equals(
                card.getExpiryMonth()
        ) || command.expiryYear() != cardExpiryYear) {
            throw new BusinessException(
                    ErrorCode.CARD_AUTHORIZATION_EXPIRY_INVALID
            );
        }

        YearMonth cardExpiry =
                YearMonth.of(
                        card.getExpiryYear(),
                        card.getExpiryMonth()
                );

        if (cardExpiry.isBefore(YearMonth.now())) {
            throw new BusinessException(
                    ErrorCode.CARD_AUTHORIZATION_CARD_EXPIRED
            );
        }
    }

    private UUID createHold(
            Card card,
            UUID authorizationId,
            AuthorizeCardPaymentCommand command,
            Instant now
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
            AuthorizeCardPaymentCommand command
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
            AuthorizeCardPaymentCommand command
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

    private AuthorizeCardPaymentResult toResult(
            CardAuthorization authorization
    ) {
        return new AuthorizeCardPaymentResult(
                authorization.getId(),
                authorization.getCardId(),
                authorization.getReference(),
                authorization.getStatus(),
                authorization.getAmount(),
                authorization.getCurrency(),
                authorization.getAuthorizedAt(),
                authorization.getExpiresAt()
        );
    }
}