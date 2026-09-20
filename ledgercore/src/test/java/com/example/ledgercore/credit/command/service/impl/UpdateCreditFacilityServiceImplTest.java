package com.example.ledgercore.credit.command.service.impl;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.credit.command.port.outbound.ActiveCreditProductPort;
import com.example.ledgercore.credit.command.port.outbound.dto.ActiveCreditProductInfo;
import com.example.ledgercore.credit.command.repository.CreditFacilityCommandRepository;
import com.example.ledgercore.credit.command.service.dto.UpdateCreditFacilityCommand;
import com.example.ledgercore.credit.command.service.dto.UpdateCreditFacilityResult;
import com.example.ledgercore.credit.entity.CreditFacility;
import com.example.ledgercore.credit.enums.CreditFacilityStatus;
import com.example.ledgercore.product.enums.ProductType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateCreditFacilityServiceImplTest {

    @Mock
    private CreditFacilityCommandRepository creditFacilityCommandRepository;

    @Mock
    private ActiveCreditProductPort activeCreditProductPort;

    @Mock
    private CreditFacility facility;

    @Mock
    private ActiveCreditProductInfo product;

    private UpdateCreditFacilityServiceImpl service;

    private UUID customerId;
    private UUID facilityId;
    private UUID productId;

    private BigDecimal creditLimit;
    private BigDecimal outstandingBalance;
    private Currency currency;

    @BeforeEach
    void setUp() {
        service = new UpdateCreditFacilityServiceImpl(
                creditFacilityCommandRepository,
                activeCreditProductPort
        );

        customerId = UUID.randomUUID();
        facilityId = UUID.randomUUID();
        productId = UUID.randomUUID();

        creditLimit = new BigDecimal("100000000");
        outstandingBalance = new BigDecimal("25000000");
        currency = Currency.VND;
    }

    @Test
    void shouldUpdateCreditFacilitySuccessfully() {
        UpdateCreditFacilityCommand command = validCommand();

        givenActiveFacility();
        givenActiveCreditProduct();
        givenSavedFacility();

        UpdateCreditFacilityResult result =
                service.update(command);

        assertNotNull(result);
        assertEquals(facilityId, result.facilityId());
        assertEquals(customerId, result.customerId());
        assertEquals(productId, result.productId());
        assertEquals(creditLimit, result.creditLimit());
        assertEquals(outstandingBalance, result.outstandingBalance());
        assertEquals(currency, result.currency());
        assertEquals(
                CreditFacilityStatus.ACTIVE,
                result.status()
        );

        verify(creditFacilityCommandRepository)
                .findByIdAndCustomerId(
                        facilityId,
                        customerId
                );

        verify(activeCreditProductPort)
                .getActiveProduct(productId);

        verify(facility).updateCreditTerms(
                eq(productId),
                eq(creditLimit),
                eq(currency),
                any(Instant.class)
        );

        verify(creditFacilityCommandRepository)
                .save(facility);
    }

    @Test
    void shouldFindFacilityByFacilityIdAndCustomerId() {
        UpdateCreditFacilityCommand command = validCommand();

        givenActiveFacility();
        givenActiveCreditProduct();
        givenSavedFacility();

        service.update(command);

        verify(creditFacilityCommandRepository)
                .findByIdAndCustomerId(
                        facilityId,
                        customerId
                );
    }

    @Test
    void shouldGetActiveProductByProductId() {
        UpdateCreditFacilityCommand command = validCommand();

        givenActiveFacility();
        givenActiveCreditProduct();
        givenSavedFacility();

        service.update(command);

        verify(activeCreditProductPort)
                .getActiveProduct(productId);
    }

    @Test
    void shouldUpdateFacilityWithCorrectCreditTerms() {
        UpdateCreditFacilityCommand command = validCommand();

        givenActiveFacility();
        givenActiveCreditProduct();
        givenSavedFacility();

        service.update(command);

        verify(facility).updateCreditTerms(
                eq(productId),
                eq(creditLimit),
                eq(currency),
                any(Instant.class)
        );
    }

    @Test
    void shouldSaveUpdatedFacility() {
        UpdateCreditFacilityCommand command = validCommand();

        givenActiveFacility();
        givenActiveCreditProduct();
        givenSavedFacility();

        service.update(command);

        verify(creditFacilityCommandRepository)
                .save(facility);
    }

    @Test
    void shouldReturnSavedFacilityValues() {
        UpdateCreditFacilityCommand command = validCommand();

        givenActiveFacility();
        givenActiveCreditProduct();
        givenSavedFacility();

        UpdateCreditFacilityResult result =
                service.update(command);

        assertEquals(facilityId, result.facilityId());
        assertEquals(customerId, result.customerId());
        assertEquals(productId, result.productId());
        assertEquals(creditLimit, result.creditLimit());
        assertEquals(outstandingBalance, result.outstandingBalance());
        assertEquals(currency, result.currency());
        assertEquals(
                CreditFacilityStatus.ACTIVE,
                result.status()
        );
    }

    @Test
    void shouldThrowWhenCommandIsNull() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.update(null)
                );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verify(
                creditFacilityCommandRepository,
                never()
        ).findByIdAndCustomerId(
                any(UUID.class),
                any(UUID.class)
        );

        verify(
                activeCreditProductPort,
                never()
        ).getActiveProduct(any(UUID.class));

        verify(
                creditFacilityCommandRepository,
                never()
        ).save(any(CreditFacility.class));
    }

    @Test
    void shouldThrowWhenCustomerIdIsNull() {
        assertInvalidRequest(
                new UpdateCreditFacilityCommand(
                        null,
                        facilityId,
                        productId,
                        creditLimit,
                        currency
                )
        );
    }

    @Test
    void shouldThrowWhenFacilityIdIsNull() {
        assertInvalidRequest(
                new UpdateCreditFacilityCommand(
                        customerId,
                        null,
                        productId,
                        creditLimit,
                        currency
                )
        );
    }

    @Test
    void shouldThrowWhenProductIdIsNull() {
        assertInvalidRequest(
                new UpdateCreditFacilityCommand(
                        customerId,
                        facilityId,
                        null,
                        creditLimit,
                        currency
                )
        );
    }

    @Test
    void shouldThrowWhenCreditLimitIsNull() {
        assertInvalidRequest(
                new UpdateCreditFacilityCommand(
                        customerId,
                        facilityId,
                        productId,
                        null,
                        currency
                )
        );
    }

    @Test
    void shouldThrowWhenCurrencyIsNull() {
        assertInvalidRequest(
                new UpdateCreditFacilityCommand(
                        customerId,
                        facilityId,
                        productId,
                        creditLimit,
                        null
                )
        );
    }

    @Test
    void shouldThrowWhenCreditLimitIsZero() {
        UpdateCreditFacilityCommand command =
                new UpdateCreditFacilityCommand(
                        customerId,
                        facilityId,
                        productId,
                        BigDecimal.ZERO,
                        currency
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.update(command)
                );

        assertEquals(
                ErrorCode.CREDIT_FACILITY_LIMIT_INVALID,
                exception.getErrorCode()
        );

        verify(
                creditFacilityCommandRepository,
                never()
        ).findByIdAndCustomerId(
                any(UUID.class),
                any(UUID.class)
        );

        verify(
                activeCreditProductPort,
                never()
        ).getActiveProduct(any(UUID.class));

        verify(
                creditFacilityCommandRepository,
                never()
        ).save(any(CreditFacility.class));
    }

    @Test
    void shouldThrowWhenCreditLimitIsNegative() {
        UpdateCreditFacilityCommand command =
                new UpdateCreditFacilityCommand(
                        customerId,
                        facilityId,
                        productId,
                        new BigDecimal("-1"),
                        currency
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.update(command)
                );

        assertEquals(
                ErrorCode.CREDIT_FACILITY_LIMIT_INVALID,
                exception.getErrorCode()
        );

        verify(
                creditFacilityCommandRepository,
                never()
        ).findByIdAndCustomerId(
                any(UUID.class),
                any(UUID.class)
        );

        verify(
                activeCreditProductPort,
                never()
        ).getActiveProduct(any(UUID.class));

        verify(
                creditFacilityCommandRepository,
                never()
        ).save(any(CreditFacility.class));
    }

    @Test
    void shouldThrowWhenFacilityDoesNotExist() {
        UpdateCreditFacilityCommand command = validCommand();

        when(
                creditFacilityCommandRepository
                        .findByIdAndCustomerId(
                                facilityId,
                                customerId
                        )
        ).thenReturn(Optional.empty());

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.update(command)
                );

        assertEquals(
                ErrorCode.CREDIT_FACILITY_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(
                activeCreditProductPort,
                never()
        ).getActiveProduct(any(UUID.class));

        verify(
                creditFacilityCommandRepository,
                never()
        ).save(any(CreditFacility.class));
    }

    @Test
    void shouldThrowWhenFacilityIsNotActive() {
        UpdateCreditFacilityCommand command = validCommand();

        when(
                creditFacilityCommandRepository
                        .findByIdAndCustomerId(
                                facilityId,
                                customerId
                        )
        ).thenReturn(Optional.of(facility));

        when(facility.getStatus())
                .thenReturn(CreditFacilityStatus.CLOSED);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.update(command)
                );

        assertEquals(
                ErrorCode.CREDIT_FACILITY_NOT_ACTIVE,
                exception.getErrorCode()
        );

        verify(
                activeCreditProductPort,
                never()
        ).getActiveProduct(any(UUID.class));

        verify(
                facility,
                never()
        ).updateCreditTerms(
                any(UUID.class),
                any(BigDecimal.class),
                any(Currency.class),
                any(Instant.class)
        );

        verify(
                creditFacilityCommandRepository,
                never()
        ).save(any(CreditFacility.class));
    }

    @Test
    void shouldThrowWhenProductIsNotCredit() {
        UpdateCreditFacilityCommand command = validCommand();

        when(
                creditFacilityCommandRepository
                        .findByIdAndCustomerId(
                                facilityId,
                                customerId
                        )
        ).thenReturn(Optional.of(facility));

        when(facility.getStatus())
                .thenReturn(CreditFacilityStatus.ACTIVE);

        when(
                activeCreditProductPort.getActiveProduct(productId)
        ).thenReturn(product);

        when(product.type())
                .thenReturn(ProductType.DEPOSIT);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.update(command)
                );

        assertEquals(
                ErrorCode.CREDIT_FACILITY_PRODUCT_TYPE_INVALID,
                exception.getErrorCode()
        );

        verify(
                facility,
                never()
        ).updateCreditTerms(
                any(UUID.class),
                any(BigDecimal.class),
                any(Currency.class),
                any(Instant.class)
        );

        verify(
                creditFacilityCommandRepository,
                never()
        ).save(any(CreditFacility.class));
    }

    private UpdateCreditFacilityCommand validCommand() {
        return new UpdateCreditFacilityCommand(
                customerId,
                facilityId,
                productId,
                creditLimit,
                currency
        );
    }

    private void givenActiveFacility() {
        when(
                creditFacilityCommandRepository
                        .findByIdAndCustomerId(
                                facilityId,
                                customerId
                        )
        ).thenReturn(Optional.of(facility));

        when(facility.getStatus())
                .thenReturn(CreditFacilityStatus.ACTIVE);

        when(facility.getId())
                .thenReturn(facilityId);

        when(facility.getCustomerId())
                .thenReturn(customerId);

        when(facility.getProductId())
                .thenReturn(productId);

        when(facility.getCreditLimit())
                .thenReturn(creditLimit);

        when(facility.getOutstandingBalance())
                .thenReturn(outstandingBalance);

        when(facility.getCurrency())
                .thenReturn(currency);
    }

    private void givenActiveCreditProduct() {
        when(
                activeCreditProductPort.getActiveProduct(productId)
        ).thenReturn(product);

        when(product.id())
                .thenReturn(productId);

        when(product.type())
                .thenReturn(ProductType.CREDIT);
    }

    private void givenSavedFacility() {
        when(
                creditFacilityCommandRepository.save(facility)
        ).thenReturn(facility);
    }

    private void assertInvalidRequest(
            UpdateCreditFacilityCommand command
    ) {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.update(command)
                );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verify(
                creditFacilityCommandRepository,
                never()
        ).findByIdAndCustomerId(
                any(UUID.class),
                any(UUID.class)
        );

        verify(
                activeCreditProductPort,
                never()
        ).getActiveProduct(any(UUID.class));

        verify(
                creditFacilityCommandRepository,
                never()
        ).save(any(CreditFacility.class));
    }
}