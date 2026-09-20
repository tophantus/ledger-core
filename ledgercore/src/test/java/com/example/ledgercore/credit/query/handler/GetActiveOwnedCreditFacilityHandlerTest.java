package com.example.ledgercore.credit.query.handler;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.credit.entity.CreditFacility;
import com.example.ledgercore.credit.enums.CreditFacilityStatus;
import com.example.ledgercore.credit.query.dto.GetActiveOwnedCreditFacilityQuery;
import com.example.ledgercore.credit.query.dto.GetActiveOwnedCreditFacilityResult;
import com.example.ledgercore.credit.query.repository.CreditFacilityQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetActiveOwnedCreditFacilityHandlerTest {

    @Mock
    private CreditFacilityQueryRepository creditFacilityQueryRepository;

    @Mock
    private CreditFacility facility;

    @InjectMocks
    private GetActiveOwnedCreditFacilityHandler handler;

    private UUID customerId;
    private UUID facilityId;
    private UUID productId;
    private BigDecimal creditLimit;
    private BigDecimal outstandingBalance;
    private Currency currency;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        facilityId = UUID.randomUUID();
        productId = UUID.randomUUID();
        creditLimit = new BigDecimal("10000000");
        outstandingBalance = new BigDecimal("2500000");
        currency = Currency.VND;
    }

    @Test
    void shouldReturnActiveOwnedCreditFacility() {
        GetActiveOwnedCreditFacilityQuery query = validQuery();

        givenActiveFacility();

        GetActiveOwnedCreditFacilityResult result =
                handler.execute(query);

        assertEquals(facilityId, result.id());
        assertEquals(customerId, result.customerId());
        assertEquals(productId, result.productId());
        assertEquals(creditLimit, result.creditLimit());
        assertEquals(outstandingBalance, result.outstandingBalance());
        assertEquals(currency, result.currency());

        verify(creditFacilityQueryRepository)
                .findByIdAndCustomerId(facilityId, customerId);
    }

    @Test
    void shouldFindFacilityByFacilityIdAndCustomerId() {
        GetActiveOwnedCreditFacilityQuery query = validQuery();

        givenActiveFacility();

        handler.execute(query);

        verify(creditFacilityQueryRepository)
                .findByIdAndCustomerId(facilityId, customerId);
    }

    @Test
    void shouldThrowWhenQueryIsNull() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(null)
                );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verify(creditFacilityQueryRepository, never())
                .findByIdAndCustomerId(
                        org.mockito.ArgumentMatchers.any(UUID.class),
                        org.mockito.ArgumentMatchers.any(UUID.class)
                );
    }

    @Test
    void shouldThrowWhenCustomerIdIsNull() {
        GetActiveOwnedCreditFacilityQuery query =
                new GetActiveOwnedCreditFacilityQuery(
                        null,
                        facilityId
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(query)
                );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verify(creditFacilityQueryRepository, never())
                .findByIdAndCustomerId(
                        org.mockito.ArgumentMatchers.any(UUID.class),
                        org.mockito.ArgumentMatchers.any(UUID.class)
                );
    }

    @Test
    void shouldThrowWhenCreditFacilityIdIsNull() {
        GetActiveOwnedCreditFacilityQuery query =
                new GetActiveOwnedCreditFacilityQuery(
                        customerId,
                        null
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(query)
                );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verify(creditFacilityQueryRepository, never())
                .findByIdAndCustomerId(
                        org.mockito.ArgumentMatchers.any(UUID.class),
                        org.mockito.ArgumentMatchers.any(UUID.class)
                );
    }

    @Test
    void shouldThrowWhenCreditFacilityDoesNotExist() {
        GetActiveOwnedCreditFacilityQuery query = validQuery();

        when(creditFacilityQueryRepository.findByIdAndCustomerId(
                facilityId,
                customerId
        )).thenReturn(Optional.empty());

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(query)
                );

        assertEquals(
                ErrorCode.CREDIT_FACILITY_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(creditFacilityQueryRepository)
                .findByIdAndCustomerId(facilityId, customerId);
    }

    @Test
    void shouldThrowWhenCreditFacilityIsNotActive() {
        GetActiveOwnedCreditFacilityQuery query = validQuery();

        when(creditFacilityQueryRepository.findByIdAndCustomerId(
                facilityId,
                customerId
        )).thenReturn(Optional.of(facility));

        when(facility.getStatus())
                .thenReturn(CreditFacilityStatus.CLOSED);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(query)
                );

        assertEquals(
                ErrorCode.CREDIT_FACILITY_NOT_ACTIVE,
                exception.getErrorCode()
        );

        verify(creditFacilityQueryRepository)
                .findByIdAndCustomerId(facilityId, customerId);

        verify(facility)
                .getStatus();

        verify(facility, never()).getId();
        verify(facility, never()).getCustomerId();
        verify(facility, never()).getProductId();
        verify(facility, never()).getCreditLimit();
        verify(facility, never()).getOutstandingBalance();
        verify(facility, never()).getCurrency();
    }

    @Test
    void shouldMapFacilityFieldsToResult() {
        GetActiveOwnedCreditFacilityQuery query = validQuery();

        givenActiveFacility();

        GetActiveOwnedCreditFacilityResult result =
                handler.execute(query);

        assertEquals(facilityId, result.id());
        assertEquals(customerId, result.customerId());
        assertEquals(productId, result.productId());
        assertEquals(creditLimit, result.creditLimit());
        assertEquals(outstandingBalance, result.outstandingBalance());
        assertEquals(currency, result.currency());
    }

    private GetActiveOwnedCreditFacilityQuery validQuery() {
        return new GetActiveOwnedCreditFacilityQuery(
                customerId,
                facilityId
        );
    }

    private void givenActiveFacility() {
        when(creditFacilityQueryRepository.findByIdAndCustomerId(
                facilityId,
                customerId
        )).thenReturn(Optional.of(facility));

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
}