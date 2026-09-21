package com.example.ledgercore.card.command.handler;

import com.example.ledgercore.card.command.dto.CreateCreditCardCommand;
import com.example.ledgercore.card.command.dto.CreateCreditCardResult;
import com.example.ledgercore.card.command.port.outbound.CardCreditFacilityPort;
import com.example.ledgercore.card.command.port.outbound.CardProductPort;
import com.example.ledgercore.card.command.port.outbound.dto.CardCreditFacilityInfo;
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
import com.example.ledgercore.card.infrastructure.security.CardPanHashService;
import com.example.ledgercore.card.infrastructure.security.CardSecretHashService;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.credit.enums.CreditFacilityStatus;
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
class CreateCreditCardHandlerTest {

    @Mock
    private CardCreditFacilityPort cardCreditFacilityPort;

    @Mock
    private CardProductPort cardProductPort;

    @Mock
    private CardCommandRepository cardCommandRepository;

    @Mock
    private CardCredentialCommandRepository cardCredentialCommandRepository;

    @Mock
    private CardVaultSecretCommandRepository cardVaultSecretCommandRepository;

    @Mock
    private CardNumberGenerator cardNumberGenerator;

    @Mock
    private CardSecurityCodeGenerator cardSecurityCodeGenerator;

    @Mock
    private CardSecretHashService cardSecretHashService;

    @Mock
    private CardPanHashService cardPanHashService;

    @Mock
    private CardEncryptionService cardEncryptionService;

    private CreateCreditCardHandler handler;

    private UUID customerId;
    private UUID creditFacilityId;
    private UUID productId;

    private CreateCreditCardCommand command;

    private CardCreditFacilityInfo facility;
    private CardProductInfo product;

    @BeforeEach
    void setUp() {
        handler = new CreateCreditCardHandler(
                cardCreditFacilityPort,
                cardProductPort,
                cardCommandRepository,
                cardCredentialCommandRepository,
                cardVaultSecretCommandRepository,
                cardNumberGenerator,
                cardSecurityCodeGenerator,
                cardSecretHashService,
                cardPanHashService,
                cardEncryptionService
        );

        customerId = UUID.randomUUID();
        creditFacilityId = UUID.randomUUID();
        productId = UUID.randomUUID();

        command = new CreateCreditCardCommand(
                customerId,
                creditFacilityId,
                "123456"
        );

        facility = mock(CardCreditFacilityInfo.class);
        product = mock(CardProductInfo.class);
    }

    @Test
    void shouldCreateCreditCardSuccessfully() {
        String pan = "4111111111111111";
        String cvv = "123";
        String pinVerifier = "hashed-pin";
        String panHash = "hashed-pan";
        String encryptionVersion = "v1";

        givenActiveFacility();
        givenCreditProduct();
        givenNoExistingCard();

        when(cardNumberGenerator.generate())
                .thenReturn(pan);

        when(cardSecurityCodeGenerator.generate())
                .thenReturn(cvv);

        when(cardSecretHashService.hash("123456"))
                .thenReturn(pinVerifier);

        when(cardPanHashService.hash(pan))
                .thenReturn(panHash);

        when(cardEncryptionService.getActiveEncryptionVersion())
                .thenReturn(encryptionVersion);

        when(cardEncryptionService.encrypt(pan))
                .thenReturn("encrypted-pan");

        when(cardEncryptionService.encrypt(cvv))
                .thenReturn("encrypted-cvv");

        when(cardCommandRepository.save(any(Card.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        CreateCreditCardResult result =
                handler.execute(command);

        assertNotNull(result);

        assertNotNull(result.cardId());

        assertEquals(
                creditFacilityId,
                result.creditFacilityId()
        );

        assertEquals(
                CardType.CREDIT,
                result.type()
        );

        assertEquals(
                CardForm.VIRTUAL,
                result.form()
        );

        assertEquals(
                CardStatus.ACTIVE,
                result.status()
        );

        assertEquals(
                "1111",
                result.panLast4()
        );

        assertNotNull(result.expiryMonth());
        assertNotNull(result.expiryYear());
        assertNotNull(result.issuedAt());

        ArgumentCaptor<Card> cardCaptor =
                ArgumentCaptor.forClass(Card.class);

        verify(cardCommandRepository)
                .save(cardCaptor.capture());

        Card savedCard = cardCaptor.getValue();

        assertEquals(
                customerId,
                savedCard.getCustomerId()
        );

        assertEquals(
                CardType.CREDIT,
                savedCard.getType()
        );

        assertEquals(
                CardForm.VIRTUAL,
                savedCard.getForm()
        );

        assertEquals(
                CardStatus.ACTIVE,
                savedCard.getStatus()
        );

        assertNull(
                savedCard.getAccountId()
        );

        assertEquals(
                creditFacilityId,
                savedCard.getCreditFacilityId()
        );

        assertEquals(
                panHash,
                savedCard.getPanHash()
        );

        assertEquals(
                "1111",
                savedCard.getPanLast4()
        );

        assertNotNull(
                savedCard.getExpiryMonth()
        );

        assertNotNull(
                savedCard.getExpiryYear()
        );

        assertNotNull(
                savedCard.getIssuedAt()
        );

        assertNotNull(
                savedCard.getActivatedAt()
        );

        assertNull(
                savedCard.getClosedAt()
        );

        assertNotNull(
                savedCard.getCreatedAt()
        );

        assertNotNull(
                savedCard.getUpdatedAt()
        );

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
                pinVerifier,
                credential.getPinVerifier()
        );

        assertNotNull(
                credential.getCreatedAt()
        );

        assertNotNull(
                credential.getUpdatedAt()
        );

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
                encryptionVersion,
                vaultSecret.getEncryptionVersion()
        );

        assertNotNull(
                vaultSecret.getCreatedAt()
        );

        assertNotNull(
                vaultSecret.getUpdatedAt()
        );

        verify(cardCreditFacilityPort)
                .getOwnedCreditFacility(
                        customerId,
                        creditFacilityId
                );

        verify(cardProductPort)
                .getActiveProduct(productId);

        verify(cardCommandRepository)
                .existsByCreditFacilityIdAndStatusIn(
                        eq(creditFacilityId),
                        eq(activeCardStatuses())
                );

        verify(cardNumberGenerator)
                .generate();

        verify(cardSecurityCodeGenerator)
                .generate();

        verify(cardSecretHashService)
                .hash("123456");

        verify(cardPanHashService)
                .hash(pan);

        verify(cardEncryptionService)
                .getActiveEncryptionVersion();

        verify(cardEncryptionService)
                .encrypt(pan);

        verify(cardEncryptionService)
                .encrypt(cvv);

        verifyNoMoreInteractions(
                cardCreditFacilityPort,
                cardProductPort,
                cardCommandRepository,
                cardCredentialCommandRepository,
                cardVaultSecretCommandRepository,
                cardNumberGenerator,
                cardSecurityCodeGenerator,
                cardSecretHashService,
                cardPanHashService,
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
                cardCreditFacilityPort,
                cardProductPort,
                cardCommandRepository,
                cardCredentialCommandRepository,
                cardVaultSecretCommandRepository,
                cardNumberGenerator,
                cardSecurityCodeGenerator,
                cardSecretHashService,
                cardPanHashService,
                cardEncryptionService
        );
    }

    @Test
    void shouldThrowWhenCustomerIdIsNull() {
        CreateCreditCardCommand invalidCommand =
                new CreateCreditCardCommand(
                        null,
                        creditFacilityId,
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
                cardCreditFacilityPort,
                cardProductPort,
                cardCommandRepository,
                cardCredentialCommandRepository,
                cardVaultSecretCommandRepository,
                cardNumberGenerator,
                cardSecurityCodeGenerator,
                cardSecretHashService,
                cardPanHashService,
                cardEncryptionService
        );
    }

    @Test
    void shouldThrowWhenCreditFacilityIdIsNull() {
        CreateCreditCardCommand invalidCommand =
                new CreateCreditCardCommand(
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
                ErrorCode.CARD_CREDIT_FACILITY_ID_REQUIRED,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                cardCreditFacilityPort,
                cardProductPort,
                cardCommandRepository,
                cardCredentialCommandRepository,
                cardVaultSecretCommandRepository,
                cardNumberGenerator,
                cardSecurityCodeGenerator,
                cardSecretHashService,
                cardPanHashService,
                cardEncryptionService
        );
    }

    @Test
    void shouldThrowWhenPinIsNull() {
        CreateCreditCardCommand invalidCommand =
                new CreateCreditCardCommand(
                        customerId,
                        creditFacilityId,
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
                cardCreditFacilityPort,
                cardProductPort,
                cardCommandRepository,
                cardCredentialCommandRepository,
                cardVaultSecretCommandRepository,
                cardNumberGenerator,
                cardSecurityCodeGenerator,
                cardSecretHashService,
                cardPanHashService,
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
            CreateCreditCardCommand invalidCommand =
                    new CreateCreditCardCommand(
                            customerId,
                            creditFacilityId,
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
                cardCreditFacilityPort,
                cardProductPort,
                cardCommandRepository,
                cardCredentialCommandRepository,
                cardVaultSecretCommandRepository,
                cardNumberGenerator,
                cardSecurityCodeGenerator,
                cardSecretHashService,
                cardPanHashService,
                cardEncryptionService
        );
    }

    @Test
    void shouldThrowWhenCreditFacilityDoesNotExist() {
        when(cardCreditFacilityPort.getOwnedCreditFacility(
                customerId,
                creditFacilityId
        )).thenReturn(null);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.CREDIT_FACILITY_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(cardCreditFacilityPort)
                .getOwnedCreditFacility(
                        customerId,
                        creditFacilityId
                );

        verifyNoInteractions(
                cardProductPort,
                cardCommandRepository,
                cardCredentialCommandRepository,
                cardVaultSecretCommandRepository,
                cardNumberGenerator,
                cardSecurityCodeGenerator,
                cardSecretHashService,
                cardPanHashService,
                cardEncryptionService
        );
    }

    @Test
    void shouldThrowWhenCreditFacilityIsNotActive() {
        when(cardCreditFacilityPort.getOwnedCreditFacility(
                customerId,
                creditFacilityId
        )).thenReturn(facility);

        when(facility.status())
                .thenReturn(CreditFacilityStatus.SUSPENDED);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.CREDIT_FACILITY_NOT_ACTIVE,
                exception.getErrorCode()
        );

        verify(cardCreditFacilityPort)
                .getOwnedCreditFacility(
                        customerId,
                        creditFacilityId
                );

        verify(facility)
                .status();

        verifyNoInteractions(
                cardProductPort,
                cardCommandRepository,
                cardCredentialCommandRepository,
                cardVaultSecretCommandRepository,
                cardNumberGenerator,
                cardSecurityCodeGenerator,
                cardSecretHashService,
                cardPanHashService,
                cardEncryptionService
        );
    }

    @Test
    void shouldThrowWhenProductDoesNotExist() {
        givenActiveFacilityForProductLookup();

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

        verify(cardCreditFacilityPort)
                .getOwnedCreditFacility(
                        customerId,
                        creditFacilityId
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
                cardPanHashService,
                cardEncryptionService
        );
    }

    @Test
    void shouldThrowWhenProductIsNotCredit() {
        givenActiveFacilityForProductLookup();

        when(cardProductPort.getActiveProduct(productId))
                .thenReturn(product);

        when(product.type())
                .thenReturn(ProductType.DEPOSIT);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.CARD_PRODUCT_TYPE_INVALID,
                exception.getErrorCode()
        );

        verify(cardCreditFacilityPort)
                .getOwnedCreditFacility(
                        customerId,
                        creditFacilityId
                );

        verify(cardProductPort)
                .getActiveProduct(productId);

        verify(product)
                .type();

        verifyNoInteractions(
                cardCommandRepository,
                cardCredentialCommandRepository,
                cardVaultSecretCommandRepository,
                cardNumberGenerator,
                cardSecurityCodeGenerator,
                cardSecretHashService,
                cardPanHashService,
                cardEncryptionService
        );
    }

    @Test
    void shouldThrowWhenCardAlreadyExists() {
        givenActiveFacility();
        givenCreditProduct();

        when(cardCommandRepository
                .existsByCreditFacilityIdAndStatusIn(
                        eq(creditFacilityId),
                        eq(activeCardStatuses())
                ))
                .thenReturn(true);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.CARD_ALREADY_EXISTS,
                exception.getErrorCode()
        );

        verify(cardCommandRepository)
                .existsByCreditFacilityIdAndStatusIn(
                        eq(creditFacilityId),
                        eq(activeCardStatuses())
                );

        verify(cardCommandRepository, never())
                .save(any(Card.class));

        verifyNoInteractions(
                cardNumberGenerator,
                cardSecurityCodeGenerator,
                cardSecretHashService,
                cardPanHashService,
                cardEncryptionService,
                cardCredentialCommandRepository,
                cardVaultSecretCommandRepository
        );
    }

    @Test
    void shouldAllowCreationWhenNoExistingActiveCardExists() {
        givenActiveFacility();
        givenCreditProduct();
        givenNoExistingCard();
        givenSuccessfulCardCreation();

        CreateCreditCardResult result =
                handler.execute(command);

        assertNotNull(result);

        verify(cardCommandRepository)
                .existsByCreditFacilityIdAndStatusIn(
                        eq(creditFacilityId),
                        eq(activeCardStatuses())
                );

        verify(cardCommandRepository)
                .save(any(Card.class));
    }

    @Test
    void shouldHashPinBeforeSavingCredential() {
        givenActiveFacility();
        givenCreditProduct();
        givenNoExistingCard();
        givenSuccessfulCardCreation();

        handler.execute(command);

        verify(cardSecretHashService)
                .hash("123456");

        ArgumentCaptor<CardCredential> captor =
                ArgumentCaptor.forClass(CardCredential.class);

        verify(cardCredentialCommandRepository)
                .save(captor.capture());

        CardCredential credential =
                captor.getValue();

        assertEquals(
                "hashed-pin",
                credential.getPinVerifier()
        );
    }

    @Test
    void shouldHashPanBeforeSavingCard() {
        givenActiveFacility();
        givenCreditProduct();
        givenNoExistingCard();
        givenSuccessfulCardCreation();

        handler.execute(command);

        verify(cardPanHashService)
                .hash("4111111111111111");

        ArgumentCaptor<Card> captor =
                ArgumentCaptor.forClass(Card.class);

        verify(cardCommandRepository)
                .save(captor.capture());

        Card card = captor.getValue();

        assertEquals(
                "hashed-pan",
                card.getPanHash()
        );
    }

    @Test
    void shouldEncryptPanAndCvvBeforeSavingVaultSecret() {
        givenActiveFacility();
        givenCreditProduct();
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
    void shouldSetExpiryDateToFiveYearsFromCurrentMonth() {
        givenActiveFacility();
        givenCreditProduct();
        givenNoExistingCard();
        givenSuccessfulCardCreation();

        CreateCreditCardResult result =
                handler.execute(command);

        YearMonth expectedExpiry =
                YearMonth.now()
                        .plusYears(5);

        assertEquals(
                expectedExpiry.getYear(),
                result.expiryYear().intValue()
        );

        assertEquals(
                expectedExpiry.getMonthValue(),
                result.expiryMonth().intValue()
        );
    }

    private void givenActiveFacilityForProductLookup() {
        when(cardCreditFacilityPort.getOwnedCreditFacility(
                customerId,
                creditFacilityId
        )).thenReturn(facility);

        when(facility.productId())
                .thenReturn(productId);

        when(facility.status())
                .thenReturn(CreditFacilityStatus.ACTIVE);
    }

    private void givenActiveFacility() {
        when(cardCreditFacilityPort.getOwnedCreditFacility(
                customerId,
                creditFacilityId
        )).thenReturn(facility);

        when(facility.id())
                .thenReturn(creditFacilityId);

        when(facility.productId())
                .thenReturn(productId);

        when(facility.status())
                .thenReturn(CreditFacilityStatus.ACTIVE);
    }

    private void givenCreditProduct() {
        when(cardProductPort.getActiveProduct(productId))
                .thenReturn(product);

        when(product.type())
                .thenReturn(ProductType.CREDIT);
    }

    private void givenNoExistingCard() {
        when(cardCommandRepository
                .existsByCreditFacilityIdAndStatusIn(
                        eq(creditFacilityId),
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

        when(cardPanHashService.hash("4111111111111111"))
                .thenReturn("hashed-pan");

        when(cardEncryptionService.getActiveEncryptionVersion())
                .thenReturn("v1");

        when(cardEncryptionService.encrypt(
                "4111111111111111"
        )).thenReturn("encrypted-pan");

        when(cardEncryptionService.encrypt("123"))
                .thenReturn("encrypted-cvv");

        when(cardCommandRepository.save(any(Card.class)))
                .thenAnswer(invocation ->
                        invocation.<Card>getArgument(0));
    }

    private Set<CardStatus> activeCardStatuses() {
        return Set.of(
                CardStatus.PENDING_ACTIVATION,
                CardStatus.ACTIVE,
                CardStatus.BLOCKED
        );
    }
}