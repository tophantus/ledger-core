package com.example.ledgercore.card.command.handler;

import com.example.ledgercore.card.command.dto.CreateDebitCardCommand;
import com.example.ledgercore.card.command.dto.CreateDebitCardResult;
import com.example.ledgercore.card.command.port.outbound.CardAccountPort;
import com.example.ledgercore.card.command.port.outbound.CardProductPort;
import com.example.ledgercore.card.command.port.outbound.dto.CardAccountInfo;
import com.example.ledgercore.card.command.port.outbound.dto.CardProductInfo;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.YearMonth;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateDebitCardHandlerTest {

    @Mock
    private CardAccountPort cardAccountPort;

    @Mock
    private CardProductPort cardProductPort;

    @Mock
    private CardCommandRepository cardCommandRepository;

    @Mock
    private CardCredentialCommandRepository
            cardCredentialCommandRepository;

    @Mock
    private CardVaultSecretCommandRepository
            cardVaultSecretCommandRepository;

    @Mock
    private CardNumberGenerator cardNumberGenerator;

    @Mock
    private CardSecurityCodeGenerator
            cardSecurityCodeGenerator;

    @Mock
    private CardSecretHashService cardSecretHashService;

    @Mock
    private CardEncryptionService cardEncryptionService;

    private CreateDebitCardHandler handler;

    private UUID customerId;
    private UUID accountId;
    private UUID productId;

    private CreateDebitCardCommand command;

    private CardAccountInfo account;
    private CardProductInfo product;

    @BeforeEach
    void setUp() {
        handler = new CreateDebitCardHandler(
                cardAccountPort,
                cardProductPort,
                cardCommandRepository,
                cardCredentialCommandRepository,
                cardVaultSecretCommandRepository,
                cardNumberGenerator,
                cardSecurityCodeGenerator,
                cardSecretHashService,
                cardEncryptionService
        );

        customerId = UUID.randomUUID();
        accountId = UUID.randomUUID();
        productId = UUID.randomUUID();

        command = new CreateDebitCardCommand(
                customerId,
                accountId,
                "123456"
        );

        account = mock(CardAccountInfo.class);
        product = mock(CardProductInfo.class);
    }

    @Test
    void shouldCreateDebitCardSuccessfully() {
        givenActiveAccount();
        givenDepositProduct();
        givenNoExistingCard();
        givenSuccessfulCardCreation();

        CreateDebitCardResult result =
                handler.execute(command);

        assertNotNull(result);
        assertNotNull(result.cardId());
        assertEquals(accountId, result.accountId());
        assertEquals(CardType.DEBIT, result.type());
        assertEquals(CardForm.VIRTUAL, result.form());
        assertEquals(CardStatus.ACTIVE, result.status());
        assertEquals("1111", result.panLast4());
        assertNotNull(result.expiryMonth());
        assertNotNull(result.expiryYear());
        assertNotNull(result.issuedAt());

        ArgumentCaptor<Card> cardCaptor =
                ArgumentCaptor.forClass(Card.class);

        verify(cardCommandRepository)
                .save(cardCaptor.capture());

        Card savedCard = cardCaptor.getValue();

        assertEquals(customerId, savedCard.getCustomerId());
        assertEquals(CardType.DEBIT, savedCard.getType());
        assertEquals(CardForm.VIRTUAL, savedCard.getForm());
        assertEquals(CardStatus.ACTIVE, savedCard.getStatus());
        assertEquals(accountId, savedCard.getAccountId());
        assertNull(savedCard.getCreditFacilityId());
        assertEquals("1111", savedCard.getPanLast4());
        assertNotNull(savedCard.getExpiryMonth());
        assertNotNull(savedCard.getExpiryYear());
        assertNotNull(savedCard.getIssuedAt());
        assertNotNull(savedCard.getActivatedAt());
        assertNull(savedCard.getClosedAt());
        assertNotNull(savedCard.getCreatedAt());
        assertNotNull(savedCard.getUpdatedAt());

        ArgumentCaptor<CardCredential> credentialCaptor =
                ArgumentCaptor.forClass(CardCredential.class);

        verify(cardCredentialCommandRepository)
                .save(credentialCaptor.capture());

        CardCredential credential =
                credentialCaptor.getValue();

        assertEquals(
                savedCard.getId(),
                credential.getCardId()
        );

        assertEquals(
                "hashed-pin",
                credential.getPinVerifier()
        );

        assertNotNull(credential.getCreatedAt());
        assertNotNull(credential.getUpdatedAt());

        ArgumentCaptor<CardVaultSecret> vaultCaptor =
                ArgumentCaptor.forClass(CardVaultSecret.class);

        verify(cardVaultSecretCommandRepository)
                .save(vaultCaptor.capture());

        CardVaultSecret vaultSecret =
                vaultCaptor.getValue();

        assertEquals(
                savedCard.getId(),
                vaultSecret.getCardId()
        );

        assertEquals(
                "encrypted-pan",
                vaultSecret.getEncryptedPan()
        );

        assertEquals(
                "encrypted-cvv",
                vaultSecret.getEncryptedCvv()
        );

        assertEquals(
                "v1",
                vaultSecret.getEncryptionVersion()
        );

        verify(cardAccountPort)
                .getOwnedAccount(
                        customerId,
                        accountId
                );

        verify(cardProductPort)
                .getActiveProduct(productId);

        verify(cardNumberGenerator).generate();
        verify(cardSecurityCodeGenerator).generate();
        verify(cardSecretHashService).hash("123456");

        verify(cardEncryptionService)
                .getActiveEncryptionVersion();

        verify(cardEncryptionService)
                .encrypt("4111111111111111");

        verify(cardEncryptionService)
                .encrypt("123");

        verifyNoMoreInteractions(
                cardAccountPort,
                cardProductPort,
                cardCommandRepository,
                cardCredentialCommandRepository,
                cardVaultSecretCommandRepository,
                cardNumberGenerator,
                cardSecurityCodeGenerator,
                cardSecretHashService,
                cardEncryptionService
        );
    }

    @Test
    void shouldThrowWhenCommandIsNull() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(null)
                );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                cardAccountPort,
                cardProductPort,
                cardCommandRepository,
                cardCredentialCommandRepository,
                cardVaultSecretCommandRepository,
                cardNumberGenerator,
                cardSecurityCodeGenerator,
                cardSecretHashService,
                cardEncryptionService
        );
    }

    @Test
    void shouldThrowWhenCustomerIdIsNull() {
        CreateDebitCardCommand invalidCommand =
                new CreateDebitCardCommand(
                        null,
                        accountId,
                        "123456"
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(invalidCommand)
                );

        assertEquals(
                ErrorCode.CARD_CUSTOMER_ID_REQUIRED,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                cardAccountPort,
                cardProductPort,
                cardCommandRepository,
                cardCredentialCommandRepository,
                cardVaultSecretCommandRepository,
                cardNumberGenerator,
                cardSecurityCodeGenerator,
                cardSecretHashService,
                cardEncryptionService
        );
    }

    @Test
    void shouldThrowWhenAccountIdIsNull() {
        CreateDebitCardCommand invalidCommand =
                new CreateDebitCardCommand(
                        customerId,
                        null,
                        "123456"
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(invalidCommand)
                );

        assertEquals(
                ErrorCode.CARD_ACCOUNT_ID_REQUIRED,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                cardAccountPort,
                cardProductPort,
                cardCommandRepository,
                cardCredentialCommandRepository,
                cardVaultSecretCommandRepository,
                cardNumberGenerator,
                cardSecurityCodeGenerator,
                cardSecretHashService,
                cardEncryptionService
        );
    }

    @Test
    void shouldThrowWhenPinIsNull() {
        CreateDebitCardCommand invalidCommand =
                new CreateDebitCardCommand(
                        customerId,
                        accountId,
                        null
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(invalidCommand)
                );

        assertEquals(
                ErrorCode.CARD_PIN_INVALID,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                cardAccountPort,
                cardProductPort,
                cardCommandRepository,
                cardCredentialCommandRepository,
                cardVaultSecretCommandRepository,
                cardNumberGenerator,
                cardSecurityCodeGenerator,
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
            CreateDebitCardCommand invalidCommand =
                    new CreateDebitCardCommand(
                            customerId,
                            accountId,
                            invalidPin
                    );

            BusinessException exception =
                    assertThrows(
                            BusinessException.class,
                            () -> handler.execute(invalidCommand)
                    );

            assertEquals(
                    ErrorCode.CARD_PIN_INVALID,
                    exception.getErrorCode()
            );
        }

        verifyNoInteractions(
                cardAccountPort,
                cardProductPort,
                cardCommandRepository,
                cardCredentialCommandRepository,
                cardVaultSecretCommandRepository,
                cardNumberGenerator,
                cardSecurityCodeGenerator,
                cardSecretHashService,
                cardEncryptionService
        );
    }

    @Test
    void shouldThrowWhenAccountDoesNotExist() {
        when(cardAccountPort.getOwnedAccount(
                customerId,
                accountId
        )).thenReturn(null);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.ACCOUNT_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(cardAccountPort)
                .getOwnedAccount(
                        customerId,
                        accountId
                );

        verifyNoInteractions(
                cardProductPort,
                cardCommandRepository,
                cardCredentialCommandRepository,
                cardVaultSecretCommandRepository,
                cardNumberGenerator,
                cardSecurityCodeGenerator,
                cardSecretHashService,
                cardEncryptionService
        );
    }

    @Test
    void shouldThrowWhenProductDoesNotExist() {
        givenAccountWithProduct();

        when(cardProductPort.getActiveProduct(productId))
                .thenReturn(null);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.PRODUCT_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(cardAccountPort)
                .getOwnedAccount(
                        customerId,
                        accountId
                );

        verify(cardProductPort)
                .getActiveProduct(productId);

        verifyNoInteractions(
                cardCommandRepository,
                cardCredentialCommandRepository,
                cardVaultSecretCommandRepository,
                cardNumberGenerator,
                cardSecurityCodeGenerator,
                cardSecretHashService,
                cardEncryptionService
        );
    }

    @Test
    void shouldThrowWhenProductIsNotDeposit() {
        givenAccountWithProduct();

        when(cardProductPort.getActiveProduct(productId))
                .thenReturn(product);

        when(product.type())
                .thenReturn(ProductType.CREDIT);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.CARD_PRODUCT_TYPE_INVALID,
                exception.getErrorCode()
        );

        verify(cardAccountPort)
                .getOwnedAccount(
                        customerId,
                        accountId
                );

        verify(cardProductPort)
                .getActiveProduct(productId);

        verify(product).type();

        verifyNoInteractions(
                cardCommandRepository,
                cardCredentialCommandRepository,
                cardVaultSecretCommandRepository,
                cardNumberGenerator,
                cardSecurityCodeGenerator,
                cardEncryptionService
        );
    }

    @Test
    void shouldThrowWhenCardAlreadyExists() {
        givenActiveAccountForCardValidation();
        givenDepositProduct();

        when(cardCommandRepository.existsByAccountIdAndStatusIn(
                eq(accountId),
                eq(activeCardStatuses())
        )).thenReturn(true);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.CARD_ALREADY_EXISTS,
                exception.getErrorCode()
        );

        verify(cardAccountPort)
                .getOwnedAccount(
                        customerId,
                        accountId
                );

        verify(cardProductPort)
                .getActiveProduct(productId);

        verify(cardCommandRepository)
                .existsByAccountIdAndStatusIn(
                        eq(accountId),
                        eq(activeCardStatuses())
                );

        verify(cardCommandRepository, never())
                .save(any(Card.class));

        verifyNoInteractions(
                cardNumberGenerator,
                cardSecurityCodeGenerator,
                cardSecretHashService,
                cardEncryptionService,
                cardCredentialCommandRepository,
                cardVaultSecretCommandRepository
        );
    }

    @Test
    void shouldSetExpiryDateToFiveYearsFromCurrentMonth() {
        givenActiveAccount();
        givenDepositProduct();
        givenNoExistingCard();
        givenSuccessfulCardCreation();

        CreateDebitCardResult result =
                handler.execute(command);

        YearMonth expectedExpiry =
                YearMonth.now().plusYears(5);

        assertEquals(
                expectedExpiry.getYear(),
                result.expiryYear().intValue()
        );

        assertEquals(
                expectedExpiry.getMonthValue(),
                result.expiryMonth().intValue()
        );
    }

    @Test
    void shouldHashPinBeforeSavingCredential() {
        givenActiveAccount();
        givenDepositProduct();
        givenNoExistingCard();
        givenSuccessfulCardCreation();

        handler.execute(command);

        verify(cardSecretHashService)
                .hash("123456");

        ArgumentCaptor<CardCredential> captor =
                ArgumentCaptor.forClass(CardCredential.class);

        verify(cardCredentialCommandRepository)
                .save(captor.capture());

        assertEquals(
                "hashed-pin",
                captor.getValue().getPinVerifier()
        );
    }

    @Test
    void shouldEncryptPanAndCvvBeforeSavingVaultSecret() {
        givenActiveAccount();
        givenDepositProduct();
        givenNoExistingCard();
        givenSuccessfulCardCreation();

        handler.execute(command);

        verify(cardEncryptionService)
                .encrypt("4111111111111111");

        verify(cardEncryptionService)
                .encrypt("123");

        ArgumentCaptor<CardVaultSecret> captor =
                ArgumentCaptor.forClass(CardVaultSecret.class);

        verify(cardVaultSecretCommandRepository)
                .save(captor.capture());

        CardVaultSecret vaultSecret =
                captor.getValue();

        assertEquals(
                "encrypted-pan",
                vaultSecret.getEncryptedPan()
        );

        assertEquals(
                "encrypted-cvv",
                vaultSecret.getEncryptedCvv()
        );

        assertEquals(
                "v1",
                vaultSecret.getEncryptionVersion()
        );
    }

    @Test
    void shouldCreateDebitCardWithCorrectCardProperties() {
        givenActiveAccount();
        givenDepositProduct();
        givenNoExistingCard();
        givenSuccessfulCardCreation();

        handler.execute(command);

        ArgumentCaptor<Card> captor =
                ArgumentCaptor.forClass(Card.class);

        verify(cardCommandRepository)
                .save(captor.capture());

        Card card = captor.getValue();

        assertEquals(
                customerId,
                card.getCustomerId()
        );

        assertEquals(
                accountId,
                card.getAccountId()
        );

        assertEquals(
                CardType.DEBIT,
                card.getType()
        );

        assertEquals(
                CardForm.VIRTUAL,
                card.getForm()
        );

        assertEquals(
                CardStatus.ACTIVE,
                card.getStatus()
        );

        assertNull(card.getCreditFacilityId());

        assertEquals(
                "1111",
                card.getPanLast4()
        );
    }

    private void givenActiveAccount() {
        when(cardAccountPort.getOwnedAccount(
                customerId,
                accountId
        )).thenReturn(account);

        when(account.id())
                .thenReturn(accountId);

        when(account.productId())
                .thenReturn(productId);
    }

    private void givenAccountWithProduct() {
        when(cardAccountPort.getOwnedAccount(
                customerId,
                accountId
        )).thenReturn(account);

        when(account.productId())
                .thenReturn(productId);
    }

    private void givenActiveAccountForCardValidation() {
        when(cardAccountPort.getOwnedAccount(
                customerId,
                accountId
        )).thenReturn(account);

        when(account.productId())
                .thenReturn(productId);

        when(account.id())
                .thenReturn(accountId);
    }

    private void givenDepositProduct() {
        when(cardProductPort.getActiveProduct(productId))
                .thenReturn(product);

        when(product.type())
                .thenReturn(ProductType.DEPOSIT);
    }

    private void givenNoExistingCard() {
        when(cardCommandRepository
                .existsByAccountIdAndStatusIn(
                        eq(accountId),
                        eq(activeCardStatuses())
                ))
                .thenReturn(false);
    }

    private void givenSuccessfulCardCreation() {
        when(cardNumberGenerator.generate())
                .thenReturn("4111111111111111");

        when(cardSecurityCodeGenerator.generate())
                .thenReturn("123");

        when(cardSecretHashService.hash("123456"))
                .thenReturn("hashed-pin");

        when(cardEncryptionService.getActiveEncryptionVersion())
                .thenReturn("v1");

        when(cardEncryptionService.encrypt(
                "4111111111111111"
        )).thenReturn("encrypted-pan");

        when(cardEncryptionService.encrypt("123"))
                .thenReturn("encrypted-cvv");

        when(cardCommandRepository.save(any(Card.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));
    }

    private Set<CardStatus> activeCardStatuses() {
        return Set.of(
                CardStatus.PENDING_ACTIVATION,
                CardStatus.ACTIVE,
                CardStatus.BLOCKED
        );
    }
}