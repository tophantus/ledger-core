package com.example.ledgercore.card.query.handler;

import com.example.ledgercore.card.entity.Card;
import com.example.ledgercore.card.entity.CardCredential;
import com.example.ledgercore.card.entity.CardVaultSecret;
import com.example.ledgercore.card.enums.CardStatus;
import com.example.ledgercore.card.infrastructure.security.CardEncryptionService;
import com.example.ledgercore.card.infrastructure.security.CardSecretHashService;
import com.example.ledgercore.card.query.dto.RevealCardDetailsQuery;
import com.example.ledgercore.card.query.dto.RevealedCardDetails;
import com.example.ledgercore.card.query.port.inbound.RevealCardDetailsUseCase;
import com.example.ledgercore.card.query.repository.CardCredentialQueryRepository;
import com.example.ledgercore.card.query.repository.CardQueryRepository;
import com.example.ledgercore.card.query.repository.CardVaultSecretQueryRepository;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RevealCardDetailsHandler
        implements RevealCardDetailsUseCase {

    private final CardQueryRepository cardQueryRepository;
    private final CardCredentialQueryRepository cardCredentialQueryRepository;
    private final CardVaultSecretQueryRepository cardVaultSecretQueryRepository;
    private final CardSecretHashService cardSecretHashService;
    private final CardEncryptionService cardEncryptionService;

    @Override
    @Transactional(readOnly = true)
    public RevealedCardDetails execute(
            RevealCardDetailsQuery query
    ) {
        validatePin(query.pin());

        Card card = cardQueryRepository
                .findByIdAndCustomerId(
                        query.cardId(),
                        query.customerId()
                )
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.CARD_NOT_FOUND
                        )
                );

        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new BusinessException(
                    ErrorCode.CARD_NOT_ACTIVE
            );
        }

        CardCredential credential =
                cardCredentialQueryRepository
                        .findByCardId(card.getId())
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.CARD_CREDENTIAL_NOT_FOUND
                                )
                        );

        if (!cardSecretHashService.matches(
                query.pin(),
                credential.getPinVerifier()
        )) {
            throw new BusinessException(
                    ErrorCode.CARD_PIN_INVALID
            );
        }

        CardVaultSecret vaultSecret =
                cardVaultSecretQueryRepository
                        .findByCardId(card.getId())
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.CARD_VAULT_SECRET_NOT_FOUND
                                )
                        );

        String pan = cardEncryptionService.decrypt(
                vaultSecret.getEncryptedPan(),
                vaultSecret.getEncryptionVersion()
        );

        String cvv = cardEncryptionService.decrypt(
                vaultSecret.getEncryptedCvv(),
                vaultSecret.getEncryptionVersion()
        );

        return new RevealedCardDetails(
                card.getId(),
                pan,
                cvv
        );
    }

    private void validatePin(String pin) {
        if (pin == null || !pin.matches("\\d{6}")) {
            throw new BusinessException(
                    ErrorCode.CARD_PIN_INVALID
            );
        }
    }
}