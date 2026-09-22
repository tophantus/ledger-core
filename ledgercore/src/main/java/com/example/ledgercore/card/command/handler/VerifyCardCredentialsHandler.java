package com.example.ledgercore.card.command.handler;

import com.example.ledgercore.card.command.dto.VerifyCardCredentialsCommand;
import com.example.ledgercore.card.command.dto.VerifyCardCredentialsResult;
import com.example.ledgercore.card.command.port.inbound.VerifyCardCredentialsUseCase;
import com.example.ledgercore.card.command.repository.CardCommandRepository;
import com.example.ledgercore.card.command.repository.CardVaultSecretCommandRepository;
import com.example.ledgercore.card.entity.Card;
import com.example.ledgercore.card.entity.CardVaultSecret;
import com.example.ledgercore.card.enums.CardStatus;
import com.example.ledgercore.card.infrastructure.security.CardEncryptionService;
import com.example.ledgercore.card.infrastructure.security.CardPanHashService;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class VerifyCardCredentialsHandler
        implements VerifyCardCredentialsUseCase {

    private final CardCommandRepository cardCommandRepository;
    private final CardVaultSecretCommandRepository
            cardVaultSecretCommandRepository;

    private final CardPanHashService cardPanHashService;
    private final CardEncryptionService cardEncryptionService;

    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public VerifyCardCredentialsResult execute(
            VerifyCardCredentialsCommand command
    ) {
        validateCommand(command);

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

        verifyPan(
                command.pan(),
                vaultSecret
        );

        verifyCvv(
                command.cvv(),
                vaultSecret
        );

        verifyExpiry(
                command.expiryMonth(),
                command.expiryYear(),
                card
        );

        return new VerifyCardCredentialsResult(
                card.getId()
        );
    }

    private void validateCommand(
            VerifyCardCredentialsCommand command
    ) {
        if (command == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
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

    private void validateCard(Card card) {
        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new BusinessException(
                    ErrorCode.CARD_NOT_ACTIVE
            );
        }
    }

    private void verifyPan(
            String pan,
            CardVaultSecret vaultSecret
    ) {
        String actualPan =
                cardEncryptionService.decrypt(
                        vaultSecret.getEncryptedPan(),
                        vaultSecret.getEncryptionVersion()
                );

        if (!actualPan.equals(pan)) {
            throw new BusinessException(
                    ErrorCode.CARD_AUTHORIZATION_PAN_INVALID
            );
        }
    }

    private void verifyCvv(
            String cvv,
            CardVaultSecret vaultSecret
    ) {
        String actualCvv =
                cardEncryptionService.decrypt(
                        vaultSecret.getEncryptedCvv(),
                        vaultSecret.getEncryptionVersion()
                );

        if (!actualCvv.equals(cvv)) {
            throw new BusinessException(
                    ErrorCode.CARD_AUTHORIZATION_CVV_INVALID
            );
        }
    }

    private void verifyExpiry(
            Short expiryMonth,
            Short expiryYear,
            Card card
    ) {
        int cardExpiryYear =
                card.getExpiryYear() % 100;

        if (!expiryMonth.equals(card.getExpiryMonth())
                || expiryYear != cardExpiryYear) {
            throw new BusinessException(
                    ErrorCode.CARD_AUTHORIZATION_EXPIRY_INVALID
            );
        }

        YearMonth cardExpiry =
                YearMonth.of(
                        card.getExpiryYear(),
                        card.getExpiryMonth()
                );

        if (cardExpiry.isBefore(
                YearMonth.now(clock)
        )) {
            throw new BusinessException(
                    ErrorCode.CARD_AUTHORIZATION_CARD_EXPIRED
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
}