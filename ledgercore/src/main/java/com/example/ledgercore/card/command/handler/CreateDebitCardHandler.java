package com.example.ledgercore.card.command.handler;

import com.example.ledgercore.card.command.dto.CreateDebitCardCommand;
import com.example.ledgercore.card.command.dto.CreateDebitCardResult;
import com.example.ledgercore.card.command.port.inbound.CreateDebitCardUseCase;
import com.example.ledgercore.card.command.port.outbound.AccountPort;
import com.example.ledgercore.card.command.port.outbound.ProductPort;
import com.example.ledgercore.card.command.port.outbound.dto.AccountInfo;
import com.example.ledgercore.card.command.port.outbound.dto.ProductInfo;
import com.example.ledgercore.card.command.repository.CardCommandRepository;
import com.example.ledgercore.card.command.repository.CardCredentialCommandRepository;
import com.example.ledgercore.card.command.repository.CardVaultSecretCommandRepository;
import com.example.ledgercore.card.entity.Card;
import com.example.ledgercore.card.entity.CardCredential;
import com.example.ledgercore.card.entity.CardVaultSecret;
import com.example.ledgercore.card.enums.CardForm;
import com.example.ledgercore.card.enums.CardStatus;
import com.example.ledgercore.card.enums.CardType;
import com.example.ledgercore.card.infrastructure.generator.CardNumberGenerator;
import com.example.ledgercore.card.infrastructure.generator.CardSecurityCodeGenerator;
import com.example.ledgercore.card.infrastructure.security.CardEncryptionService;
import com.example.ledgercore.card.infrastructure.security.CardSecretHashService;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.product.enums.ProductType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.YearMonth;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateDebitCardHandler
        implements CreateDebitCardUseCase {

    private static final int CARD_EXPIRY_YEARS = 5;

    private final AccountPort accountPort;
    private final ProductPort productPort;

    private final CardCommandRepository cardCommandRepository;
    private final CardCredentialCommandRepository
            cardCredentialCommandRepository;
    private final CardVaultSecretCommandRepository
            cardVaultSecretCommandRepository;

    private final CardNumberGenerator cardNumberGenerator;
    private final CardSecurityCodeGenerator
            cardSecurityCodeGenerator;

    private final CardSecretHashService cardSecretHashService;
    private final CardEncryptionService cardEncryptionService;

    @Override
    @Transactional
    public CreateDebitCardResult execute(
            CreateDebitCardCommand command
    ) {
        validateCommand(command);

        AccountInfo account =
                accountPort.getOwnedAccount(
                        command.customerId(),
                        command.accountId()
                );

        validateAccount(account);

        ProductInfo product =
                productPort.getActiveProduct(
                        account.productId()
                );

        validateDepositProduct(product);

        validateExistingCard(
                account.id()
        );

        String pan =
                cardNumberGenerator.generate();

        String cvv =
                cardSecurityCodeGenerator.generate();

        String pinVerifier =
                cardSecretHashService.hash(command.pin());

        Instant now = Instant.now();

        YearMonth expiry =
                YearMonth.now()
                        .plusYears(CARD_EXPIRY_YEARS);

        UUID cardId = UUID.randomUUID();

        Card card = Card.builder()
                .id(cardId)
                .customerId(command.customerId())
                .type(CardType.DEBIT)
                .form(CardForm.PHYSICAL)
                .status(CardStatus.ACTIVE)
                .accountId(account.id())
                .creditFacilityId(null)
                .panLast4(
                        pan.substring(pan.length() - 4)
                )
                .expiryMonth(
                        (short) expiry.getMonthValue()
                )
                .expiryYear(
                        (short) expiry.getYear()
                )
                .issuedAt(now)
                .activatedAt(null)
                .closedAt(null)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Card savedCard = cardCommandRepository.save(card);

        CardCredential credential =
                CardCredential.builder()
                        .id(UUID.randomUUID())
                        .cardId(cardId)
                        .pinVerifier(pinVerifier)
                        .createdAt(now)
                        .updatedAt(now)
                        .build();

        cardCredentialCommandRepository.save(
                credential
        );

        String encryptionVersion =
                cardEncryptionService
                        .getActiveEncryptionVersion();

        CardVaultSecret vaultSecret =
                CardVaultSecret.builder()
                        .id(UUID.randomUUID())
                        .cardId(savedCard.getId())
                        .encryptedPan(
                                cardEncryptionService.encrypt(pan)
                        )
                        .encryptedCvv(
                                cardEncryptionService.encrypt(cvv)
                        )
                        .encryptionVersion(
                                encryptionVersion
                        )
                        .createdAt(now)
                        .updatedAt(now)
                        .build();

        cardVaultSecretCommandRepository.save(
                vaultSecret
        );

        return new CreateDebitCardResult(
                savedCard.getId(),
                savedCard.getAccountId(),
                savedCard.getType(),
                savedCard.getForm(),
                savedCard.getStatus(),
                savedCard.getPanLast4(),
                savedCard.getExpiryMonth(),
                savedCard.getExpiryYear(),
                savedCard.getIssuedAt()
        );
    }

    private void validateCommand(
            CreateDebitCardCommand command
    ) {
        if (command == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (command.customerId() == null) {
            throw new BusinessException(
                    ErrorCode.CARD_CUSTOMER_ID_REQUIRED
            );
        }

        if (command.accountId() == null) {
            throw new BusinessException(
                    ErrorCode.CARD_ACCOUNT_ID_REQUIRED
            );
        }

        if (command.pin() == null
                || !command.pin().matches("\\d{6}")) {
            throw new BusinessException(
                    ErrorCode.CARD_PIN_INVALID
            );
        }
    }

    private void validateAccount(
            AccountInfo account
    ) {
        if (account == null) {
            throw new BusinessException(
                    ErrorCode.ACCOUNT_NOT_FOUND
            );
        }
    }

    private void validateDepositProduct(
            ProductInfo product
    ) {
        if (product == null) {
            throw new BusinessException(
                    ErrorCode.PRODUCT_NOT_FOUND
            );
        }

        if (product.type() != ProductType.DEPOSIT) {
            throw new BusinessException(
                    ErrorCode.CARD_PRODUCT_TYPE_INVALID
            );
        }
    }

    private void validateExistingCard(
            UUID accountId
    ) {
        boolean exists =
                cardCommandRepository
                        .existsByAccountIdAndStatusIn(
                                accountId,
                                Set.of(
                                        CardStatus.PENDING_ACTIVATION,
                                        CardStatus.ACTIVE,
                                        CardStatus.BLOCKED
                                )
                        );

        if (exists) {
            throw new BusinessException(
                    ErrorCode.CARD_ALREADY_EXISTS
            );
        }
    }
}