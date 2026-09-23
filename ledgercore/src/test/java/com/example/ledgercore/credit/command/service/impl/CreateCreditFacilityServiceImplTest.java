package com.example.ledgercore.credit.command.service.impl;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.credit.command.port.outbound.ActiveCreditProductPort;
import com.example.ledgercore.credit.command.port.outbound.CreditLedgerAccountPort;
import com.example.ledgercore.credit.command.port.outbound.dto.ActiveCreditProductInfo;
import com.example.ledgercore.credit.command.repository.CreditFacilityCommandRepository;
import com.example.ledgercore.credit.command.service.dto.CreateCreditFacilityCommand;
import com.example.ledgercore.credit.command.service.dto.CreateCreditFacilityResult;
import com.example.ledgercore.credit.entity.CreditFacility;
import com.example.ledgercore.credit.enums.CreditFacilityStatus;
import com.example.ledgercore.product.enums.ProductType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateCreditFacilityServiceImplTest {

    @Mock
    private ActiveCreditProductPort activeCreditProductPort;

    @Mock
    private CreditFacilityCommandRepository creditFacilityCommandRepository;

    @Mock
    private CreditLedgerAccountPort creditLedgerAccountPort;

    @Mock
    private ActiveCreditProductInfo product;

    private CreateCreditFacilityServiceImpl service;

    private UUID customerId;
    private UUID productId;
    private UUID ledgerAccountId;

    private BigDecimal creditLimit;
    private Currency currency;

    @BeforeEach
    void setUp() {
        service = new CreateCreditFacilityServiceImpl(
                activeCreditProductPort,
                creditFacilityCommandRepository,
                creditLedgerAccountPort
        );

        customerId = UUID.randomUUID();
        productId = UUID.randomUUID();
        ledgerAccountId = UUID.randomUUID();

        creditLimit = new BigDecimal("100000000");
        currency = Currency.VND;
    }

    @Test
    void shouldCreateCreditFacilitySuccessfully() {
        CreateCreditFacilityCommand command = command();

        givenActiveCreditProduct();
        givenCreatedLedgerAccount();
        givenSavedFacility();

        CreateCreditFacilityResult result =
                service.create(command);

        assertNotNull(result);

        assertNotNull(result.facilityId());
        assertEquals(
                customerId,
                result.customerId()
        );
        assertEquals(
                productId,
                result.productId()
        );
        assertEquals(
                creditLimit,
                result.creditLimit()
        );
        assertEquals(
                BigDecimal.ZERO,
                result.outstandingBalance()
        );
        assertEquals(
                currency,
                result.currency()
        );
        assertEquals(
                CreditFacilityStatus.ACTIVE,
                result.status()
        );
        assertNotNull(result.openedAt());

        verify(activeCreditProductPort)
                .getActiveProduct(productId);

        verify(creditLedgerAccountPort)
                .createCreditLedgerAccount(
                        any(UUID.class),
                        eq(currency)
                );

        verify(creditFacilityCommandRepository)
                .save(any(CreditFacility.class));
    }

    @Test
    void shouldGetActiveProductByProductId() {
        CreateCreditFacilityCommand command = command();

        givenActiveCreditProduct();
        givenCreatedLedgerAccount();
        givenSavedFacility();

        service.create(command);

        verify(activeCreditProductPort)
                .getActiveProduct(productId);
    }

    @Test
    void shouldCreateCreditLedgerAccountWithFacilityIdAndCurrency() {
        CreateCreditFacilityCommand command = command();

        givenActiveCreditProduct();
        givenCreatedLedgerAccount();
        givenSavedFacility();

        service.create(command);

        ArgumentCaptor<CreditFacility> captor =
                ArgumentCaptor.forClass(CreditFacility.class);

        verify(creditFacilityCommandRepository)
                .save(captor.capture());

        CreditFacility facility =
                captor.getValue();

        verify(creditLedgerAccountPort)
                .createCreditLedgerAccount(
                        facility.getId(),
                        currency
                );
    }

    @Test
    void shouldSaveCreditFacilityWithCorrectValues() {
        CreateCreditFacilityCommand command = command();

        givenActiveCreditProduct();
        givenCreatedLedgerAccount();
        givenSavedFacility();

        service.create(command);

        ArgumentCaptor<CreditFacility> captor =
                ArgumentCaptor.forClass(CreditFacility.class);

        verify(creditFacilityCommandRepository)
                .save(captor.capture());

        CreditFacility facility =
                captor.getValue();

        assertNotNull(facility.getId());

        assertEquals(
                customerId,
                facility.getCustomerId()
        );
        assertEquals(
                productId,
                facility.getProductId()
        );
        assertEquals(
                creditLimit,
                facility.getCreditLimit()
        );
        assertEquals(
                BigDecimal.ZERO,
                facility.getOutstandingBalance()
        );
        assertEquals(
                currency,
                facility.getCurrency()
        );
        assertEquals(
                CreditFacilityStatus.ACTIVE,
                facility.getStatus()
        );
        assertEquals(
                ledgerAccountId,
                facility.getLedgerAccountId()
        );

        assertNotNull(
                facility.getOpenedAt()
        );
        assertNotNull(
                facility.getCreatedAt()
        );
        assertNotNull(
                facility.getUpdatedAt()
        );
    }

    @Test
    void shouldUseProductIdFromActiveProductWhenSavingFacility() {
        UUID activeProductId =
                UUID.randomUUID();

        CreateCreditFacilityCommand command =
                command();

        when(
                activeCreditProductPort.getActiveProduct(
                        productId
                )
        ).thenReturn(product);

        when(product.id())
                .thenReturn(activeProductId);

        when(product.type())
                .thenReturn(ProductType.CREDIT);

        givenCreatedLedgerAccount();
        givenSavedFacility();

        service.create(command);

        ArgumentCaptor<CreditFacility> captor =
                ArgumentCaptor.forClass(CreditFacility.class);

        verify(creditFacilityCommandRepository)
                .save(captor.capture());

        assertEquals(
                activeProductId,
                captor.getValue().getProductId()
        );
    }

    @Test
    void shouldSetCreatedLedgerAccountIdOnFacility() {
        CreateCreditFacilityCommand command =
                command();

        givenActiveCreditProduct();
        givenCreatedLedgerAccount();
        givenSavedFacility();

        service.create(command);

        ArgumentCaptor<CreditFacility> captor =
                ArgumentCaptor.forClass(CreditFacility.class);

        verify(creditFacilityCommandRepository)
                .save(captor.capture());

        CreditFacility facility =
                captor.getValue();

        assertEquals(
                ledgerAccountId,
                facility.getLedgerAccountId()
        );
    }

    @Test
    void shouldReturnSavedFacilityValues() {
        Instant openedAt =
                Instant.parse(
                        "2026-09-21T10:00:00Z"
                );

        CreateCreditFacilityCommand command =
                command();

        givenActiveCreditProduct();
        givenCreatedLedgerAccount();

        CreditFacility saved =
                CreditFacility.builder()
                        .id(UUID.randomUUID())
                        .customerId(customerId)
                        .productId(productId)
                        .creditLimit(creditLimit)
                        .outstandingBalance(BigDecimal.ZERO)
                        .currency(currency)
                        .status(CreditFacilityStatus.ACTIVE)
                        .ledgerAccountId(ledgerAccountId)
                        .openedAt(openedAt)
                        .createdAt(openedAt)
                        .updatedAt(openedAt)
                        .build();

        when(
                creditFacilityCommandRepository.save(
                        any(CreditFacility.class)
                )
        ).thenReturn(saved);

        CreateCreditFacilityResult result =
                service.create(command);

        assertEquals(
                saved.getId(),
                result.facilityId()
        );
        assertEquals(
                customerId,
                result.customerId()
        );
        assertEquals(
                productId,
                result.productId()
        );
        assertEquals(
                creditLimit,
                result.creditLimit()
        );
        assertEquals(
                BigDecimal.ZERO,
                result.outstandingBalance()
        );
        assertEquals(
                currency,
                result.currency()
        );
        assertEquals(
                CreditFacilityStatus.ACTIVE,
                result.status()
        );
        assertEquals(
                openedAt,
                result.openedAt()
        );
    }

    @Test
    void shouldCreateLedgerAccountBeforeSavingFacility() {
        CreateCreditFacilityCommand command =
                command();

        givenActiveCreditProduct();
        givenCreatedLedgerAccount();
        givenSavedFacility();

        service.create(command);

        InOrder inOrder =
                inOrder(
                        creditLedgerAccountPort,
                        creditFacilityCommandRepository
                );

        inOrder.verify(
                creditLedgerAccountPort
        ).createCreditLedgerAccount(
                any(UUID.class),
                eq(currency)
        );

        inOrder.verify(
                creditFacilityCommandRepository
        ).save(any(CreditFacility.class));
    }

    @Test
    void shouldThrowWhenCommandIsNull() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.create(null)
                );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                activeCreditProductPort,
                creditLedgerAccountPort,
                creditFacilityCommandRepository
        );
    }

    @Test
    void shouldThrowWhenCustomerIdIsNull() {
        CreateCreditFacilityCommand command =
                new CreateCreditFacilityCommand(
                        null,
                        productId,
                        creditLimit,
                        currency
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.create(command)
                );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                activeCreditProductPort,
                creditLedgerAccountPort,
                creditFacilityCommandRepository
        );
    }

    @Test
    void shouldThrowWhenProductIdIsNull() {
        CreateCreditFacilityCommand command =
                new CreateCreditFacilityCommand(
                        customerId,
                        null,
                        creditLimit,
                        currency
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.create(command)
                );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                activeCreditProductPort,
                creditLedgerAccountPort,
                creditFacilityCommandRepository
        );
    }

    @Test
    void shouldThrowWhenCreditLimitIsNull() {
        CreateCreditFacilityCommand command =
                new CreateCreditFacilityCommand(
                        customerId,
                        productId,
                        null,
                        currency
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.create(command)
                );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                activeCreditProductPort,
                creditLedgerAccountPort,
                creditFacilityCommandRepository
        );
    }

    @Test
    void shouldThrowWhenCurrencyIsNull() {
        CreateCreditFacilityCommand command =
                new CreateCreditFacilityCommand(
                        customerId,
                        productId,
                        creditLimit,
                        null
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.create(command)
                );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                activeCreditProductPort,
                creditLedgerAccountPort,
                creditFacilityCommandRepository
        );
    }

    @Test
    void shouldThrowWhenCreditLimitIsZero() {
        CreateCreditFacilityCommand command =
                new CreateCreditFacilityCommand(
                        customerId,
                        productId,
                        BigDecimal.ZERO,
                        currency
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.create(command)
                );

        assertEquals(
                ErrorCode.CREDIT_FACILITY_LIMIT_INVALID,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                activeCreditProductPort,
                creditLedgerAccountPort,
                creditFacilityCommandRepository
        );
    }

    @Test
    void shouldThrowWhenCreditLimitIsNegative() {
        CreateCreditFacilityCommand command =
                new CreateCreditFacilityCommand(
                        customerId,
                        productId,
                        new BigDecimal("-1"),
                        currency
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.create(command)
                );

        assertEquals(
                ErrorCode.CREDIT_FACILITY_LIMIT_INVALID,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                activeCreditProductPort,
                creditLedgerAccountPort,
                creditFacilityCommandRepository
        );
    }

    @Test
    void shouldThrowWhenProductIsNotCredit() {
        CreateCreditFacilityCommand command =
                command();

        when(
                activeCreditProductPort.getActiveProduct(
                        productId
                )
        ).thenReturn(product);

        when(product.type())
                .thenReturn(ProductType.DEPOSIT);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.create(command)
                );

        assertEquals(
                ErrorCode.CREDIT_FACILITY_PRODUCT_TYPE_INVALID,
                exception.getErrorCode()
        );

        verify(
                creditLedgerAccountPort,
                never()
        ).createCreditLedgerAccount(
                any(UUID.class),
                any(Currency.class)
        );

        verify(
                creditFacilityCommandRepository,
                never()
        ).save(any(CreditFacility.class));
    }

    private CreateCreditFacilityCommand command() {
        return new CreateCreditFacilityCommand(
                customerId,
                productId,
                creditLimit,
                currency
        );
    }

    private void givenActiveCreditProduct() {
        when(
                activeCreditProductPort.getActiveProduct(
                        productId
                )
        ).thenReturn(product);

        when(product.id())
                .thenReturn(productId);

        when(product.type())
                .thenReturn(ProductType.CREDIT);
    }

    private void givenCreatedLedgerAccount() {
        when(
                creditLedgerAccountPort.createCreditLedgerAccount(
                        any(UUID.class),
                        any(Currency.class)
                )
        ).thenReturn(ledgerAccountId);
    }

    private void givenSavedFacility() {
        when(
                creditFacilityCommandRepository.save(
                        any(CreditFacility.class)
                )
        ).thenAnswer(invocation ->
                invocation.getArgument(0)
        );
    }
}