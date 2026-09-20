package com.example.ledgercore.card.query.handler;

import com.example.ledgercore.card.entity.Card;
import com.example.ledgercore.card.entity.CardCredential;
import com.example.ledgercore.card.entity.CardVaultSecret;
import com.example.ledgercore.card.enums.CardForm;
import com.example.ledgercore.card.enums.CardStatus;
import com.example.ledgercore.card.enums.CardType;
import com.example.ledgercore.card.infrastructure.security.CardEncryptionService;
import com.example.ledgercore.card.infrastructure.security.CardSecretHashService;
import com.example.ledgercore.card.query.dto.RevealCardDetailsQuery;
import com.example.ledgercore.card.query.dto.RevealedCardDetails;
import com.example.ledgercore.card.query.repository.CardCredentialQueryRepository;
import com.example.ledgercore.card.query.repository.CardQueryRepository;
import com.example.ledgercore.card.query.repository.CardVaultSecretQueryRepository;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RevealCardDetailsHandlerTest {

    @Mock
    private CardQueryRepository cardQueryRepository;

    @Mock
    private CardCredentialQueryRepository
            cardCredentialQueryRepository;

    @Mock
    private CardVaultSecretQueryRepository
            cardVaultSecretQueryRepository;

    @Mock
    private CardSecretHashService cardSecretHashService;

    @Mock
    private CardEncryptionService cardEncryptionService;

    private RevealCardDetailsHandler handler;

    private UUID customerId;
    private UUID cardId;

    private RevealCardDetailsQuery query;

    @BeforeEach
    void setUp() {
        handler = new RevealCardDetailsHandler(
                cardQueryRepository,
                cardCredentialQueryRepository,
                cardVaultSecretQueryRepository,
                cardSecretHashService,
                cardEncryptionService
        );

        customerId = UUID.randomUUID();
        cardId = UUID.randomUUID();

        query = new RevealCardDetailsQuery(
                customerId,
                cardId,
                "123456"
        );
    }

    @Test
    void shouldRevealCardDetailsSuccessfully() {
        Card card = givenActiveCard();
        CardCredential credential = givenCredential();
        CardVaultSecret vaultSecret = givenVaultSecret();

        when(cardQueryRepository.findByIdAndCustomerId(
                cardId,
                customerId
        )).thenReturn(Optional.of(card));

        when(cardCredentialQueryRepository.findByCardId(
                cardId
        )).thenReturn(Optional.of(credential));

        when(cardSecretHashService.matches(
                "123456",
                "hashed-pin"
        )).thenReturn(true);

        when(cardVaultSecretQueryRepository.findByCardId(
                cardId
        )).thenReturn(Optional.of(vaultSecret));

        when(cardEncryptionService.decrypt(
                "encrypted-pan",
                "v1"
        )).thenReturn("4111111111111111");

        when(cardEncryptionService.decrypt(
                "encrypted-cvv",
                "v1"
        )).thenReturn("123");

        RevealedCardDetails result =
                handler.execute(query);

        assertNotNull(result);

        assertEquals(
                cardId,
                result.cardId()
        );

        assertEquals(
                "4111111111111111",
                result.pan()
        );

        assertEquals(
                "123",
                result.cvv()
        );

        verify(cardQueryRepository)
                .findByIdAndCustomerId(
                        cardId,
                        customerId
                );

        verify(cardCredentialQueryRepository)
                .findByCardId(cardId);

        verify(cardSecretHashService)
                .matches(
                        "123456",
                        "hashed-pin"
                );

        verify(cardVaultSecretQueryRepository)
                .findByCardId(cardId);

        verify(cardEncryptionService)
                .decrypt(
                        "encrypted-pan",
                        "v1"
                );

        verify(cardEncryptionService)
                .decrypt(
                        "encrypted-cvv",
                        "v1"
                );

        verifyNoMoreInteractions(
                cardQueryRepository,
                cardCredentialQueryRepository,
                cardVaultSecretQueryRepository,
                cardSecretHashService,
                cardEncryptionService
        );
    }

    @Test
    void shouldThrowWhenPinIsNull() {
        RevealCardDetailsQuery invalidQuery =
                new RevealCardDetailsQuery(
                        customerId,
                        cardId,
                        null
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(invalidQuery)
                );

        assertEquals(
                ErrorCode.CARD_PIN_INVALID,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                cardQueryRepository,
                cardCredentialQueryRepository,
                cardVaultSecretQueryRepository,
                cardSecretHashService,
                cardEncryptionService
        );
    }

    @Test
    void shouldThrowWhenPinHasInvalidFormat() {
        String[] invalidPins = {
                "",
                "12345",
                "1234567",
                "12345a",
                "abcdef",
                "12 456",
                "１２３４５６"
        };

        for (String invalidPin : invalidPins) {
            RevealCardDetailsQuery invalidQuery =
                    new RevealCardDetailsQuery(
                            customerId,
                            cardId,
                            invalidPin
                    );

            BusinessException exception =
                    assertThrows(
                            BusinessException.class,
                            () -> handler.execute(invalidQuery)
                    );

            assertEquals(
                    ErrorCode.CARD_PIN_INVALID,
                    exception.getErrorCode()
            );
        }

        verifyNoInteractions(
                cardQueryRepository,
                cardCredentialQueryRepository,
                cardVaultSecretQueryRepository,
                cardSecretHashService,
                cardEncryptionService
        );
    }

    @Test
    void shouldThrowWhenCardDoesNotExist() {
        when(cardQueryRepository.findByIdAndCustomerId(
                cardId,
                customerId
        )).thenReturn(Optional.empty());

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(query)
                );

        assertEquals(
                ErrorCode.CARD_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(cardQueryRepository)
                .findByIdAndCustomerId(
                        cardId,
                        customerId
                );

        verifyNoInteractions(
                cardCredentialQueryRepository,
                cardVaultSecretQueryRepository,
                cardSecretHashService,
                cardEncryptionService
        );
    }

    @Test
    void shouldThrowWhenCardIsNotActive() {
        Card card = givenCard(CardStatus.BLOCKED);

        when(cardQueryRepository.findByIdAndCustomerId(
                cardId,
                customerId
        )).thenReturn(Optional.of(card));

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(query)
                );

        assertEquals(
                ErrorCode.CARD_NOT_ACTIVE,
                exception.getErrorCode()
        );

        verify(cardQueryRepository)
                .findByIdAndCustomerId(
                        cardId,
                        customerId
                );

        verifyNoInteractions(
                cardCredentialQueryRepository,
                cardVaultSecretQueryRepository,
                cardSecretHashService,
                cardEncryptionService
        );
    }

    @Test
    void shouldThrowWhenCredentialDoesNotExist() {
        Card card = givenActiveCard();

        when(cardQueryRepository.findByIdAndCustomerId(
                cardId,
                customerId
        )).thenReturn(Optional.of(card));

        when(cardCredentialQueryRepository.findByCardId(
                cardId
        )).thenReturn(Optional.empty());

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(query)
                );

        assertEquals(
                ErrorCode.CARD_CREDENTIAL_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(cardQueryRepository)
                .findByIdAndCustomerId(
                        cardId,
                        customerId
                );

        verify(cardCredentialQueryRepository)
                .findByCardId(cardId);

        verifyNoInteractions(
                cardVaultSecretQueryRepository,
                cardSecretHashService,
                cardEncryptionService
        );
    }

    @Test
    void shouldThrowWhenPinDoesNotMatch() {
        Card card = givenActiveCard();
        CardCredential credential = givenCredential();

        when(cardQueryRepository.findByIdAndCustomerId(
                cardId,
                customerId
        )).thenReturn(Optional.of(card));

        when(cardCredentialQueryRepository.findByCardId(
                cardId
        )).thenReturn(Optional.of(credential));

        when(cardSecretHashService.matches(
                "123456",
                "hashed-pin"
        )).thenReturn(false);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(query)
                );

        assertEquals(
                ErrorCode.CARD_PIN_INVALID,
                exception.getErrorCode()
        );

        verify(cardSecretHashService)
                .matches(
                        "123456",
                        "hashed-pin"
                );

        verifyNoInteractions(
                cardVaultSecretQueryRepository,
                cardEncryptionService
        );
    }

    @Test
    void shouldThrowWhenVaultSecretDoesNotExist() {
        Card card = givenActiveCard();
        CardCredential credential = givenCredential();

        when(cardQueryRepository.findByIdAndCustomerId(
                cardId,
                customerId
        )).thenReturn(Optional.of(card));

        when(cardCredentialQueryRepository.findByCardId(
                cardId
        )).thenReturn(Optional.of(credential));

        when(cardSecretHashService.matches(
                "123456",
                "hashed-pin"
        )).thenReturn(true);

        when(cardVaultSecretQueryRepository.findByCardId(
                cardId
        )).thenReturn(Optional.empty());

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(query)
                );

        assertEquals(
                ErrorCode.CARD_VAULT_SECRET_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(cardVaultSecretQueryRepository)
                .findByCardId(cardId);

        verifyNoInteractions(cardEncryptionService);
    }

    @Test
    void shouldDecryptPanAndCvvUsingVaultEncryptionVersion() {
        Card card = givenActiveCard();
        CardCredential credential = givenCredential();
        CardVaultSecret vaultSecret = givenVaultSecret();

        when(cardQueryRepository.findByIdAndCustomerId(
                cardId,
                customerId
        )).thenReturn(Optional.of(card));

        when(cardCredentialQueryRepository.findByCardId(
                cardId
        )).thenReturn(Optional.of(credential));

        when(cardSecretHashService.matches(
                "123456",
                "hashed-pin"
        )).thenReturn(true);

        when(cardVaultSecretQueryRepository.findByCardId(
                cardId
        )).thenReturn(Optional.of(vaultSecret));

        when(cardEncryptionService.decrypt(
                "encrypted-pan",
                "v1"
        )).thenReturn("4111111111111111");

        when(cardEncryptionService.decrypt(
                "encrypted-cvv",
                "v1"
        )).thenReturn("123");

        handler.execute(query);

        verify(cardEncryptionService)
                .decrypt(
                        "encrypted-pan",
                        "v1"
                );

        verify(cardEncryptionService)
                .decrypt(
                        "encrypted-cvv",
                        "v1"
                );

        verifyNoMoreInteractions(cardEncryptionService);
    }

    @Test
    void shouldUseCardIdFromLoadedCardWhenLoadingCredentialAndVaultSecret() {
        UUID loadedCardId = UUID.randomUUID();

        Card card = Card.builder()
                .id(loadedCardId)
                .customerId(customerId)
                .type(CardType.DEBIT)
                .form(CardForm.PHYSICAL)
                .status(CardStatus.ACTIVE)
                .accountId(UUID.randomUUID())
                .creditFacilityId(null)
                .panLast4("1111")
                .expiryMonth((short) 9)
                .expiryYear((short) 2031)
                .issuedAt(Instant.parse(
                        "2026-09-20T10:00:00Z"
                ))
                .activatedAt(Instant.parse(
                        "2026-09-20T10:05:00Z"
                ))
                .closedAt(null)
                .createdAt(Instant.parse(
                        "2026-09-20T10:00:00Z"
                ))
                .updatedAt(Instant.parse(
                        "2026-09-20T10:05:00Z"
                ))
                .build();

        CardCredential credential = givenCredential(
                loadedCardId
        );

        CardVaultSecret vaultSecret = givenVaultSecret(
                loadedCardId
        );

        when(cardQueryRepository.findByIdAndCustomerId(
                cardId,
                customerId
        )).thenReturn(Optional.of(card));

        when(cardCredentialQueryRepository.findByCardId(
                loadedCardId
        )).thenReturn(Optional.of(credential));

        when(cardSecretHashService.matches(
                "123456",
                "hashed-pin"
        )).thenReturn(true);

        when(cardVaultSecretQueryRepository.findByCardId(
                loadedCardId
        )).thenReturn(Optional.of(vaultSecret));

        when(cardEncryptionService.decrypt(
                "encrypted-pan",
                "v1"
        )).thenReturn("4111111111111111");

        when(cardEncryptionService.decrypt(
                "encrypted-cvv",
                "v1"
        )).thenReturn("123");

        RevealedCardDetails result =
                handler.execute(query);

        assertEquals(
                loadedCardId,
                result.cardId()
        );

        verify(cardCredentialQueryRepository)
                .findByCardId(loadedCardId);

        verify(cardVaultSecretQueryRepository)
                .findByCardId(loadedCardId);
    }

    private Card givenActiveCard() {
        return givenCard(CardStatus.ACTIVE);
    }

    private Card givenCard(CardStatus status) {
        return Card.builder()
                .id(cardId)
                .customerId(customerId)
                .type(CardType.DEBIT)
                .form(CardForm.PHYSICAL)
                .status(status)
                .accountId(UUID.randomUUID())
                .creditFacilityId(null)
                .panLast4("1111")
                .expiryMonth((short) 9)
                .expiryYear((short) 2031)
                .issuedAt(Instant.parse(
                        "2026-09-20T10:00:00Z"
                ))
                .activatedAt(Instant.parse(
                        "2026-09-20T10:05:00Z"
                ))
                .closedAt(null)
                .createdAt(Instant.parse(
                        "2026-09-20T10:00:00Z"
                ))
                .updatedAt(Instant.parse(
                        "2026-09-20T10:05:00Z"
                ))
                .build();
    }

    private CardCredential givenCredential() {
        return givenCredential(cardId);
    }

    private CardCredential givenCredential(UUID credentialCardId) {
        return CardCredential.builder()
                .id(UUID.randomUUID())
                .cardId(credentialCardId)
                .pinVerifier("hashed-pin")
                .createdAt(Instant.parse(
                        "2026-09-20T10:00:00Z"
                ))
                .updatedAt(Instant.parse(
                        "2026-09-20T10:05:00Z"
                ))
                .build();
    }

    private CardVaultSecret givenVaultSecret() {
        return givenVaultSecret(cardId);
    }

    private CardVaultSecret givenVaultSecret(UUID vaultCardId) {
        return CardVaultSecret.builder()
                .id(UUID.randomUUID())
                .cardId(vaultCardId)
                .encryptedPan("encrypted-pan")
                .encryptedCvv("encrypted-cvv")
                .encryptionVersion("v1")
                .createdAt(Instant.parse(
                        "2026-09-20T10:00:00Z"
                ))
                .updatedAt(Instant.parse(
                        "2026-09-20T10:00:00Z"
                ))
                .build();
    }
}