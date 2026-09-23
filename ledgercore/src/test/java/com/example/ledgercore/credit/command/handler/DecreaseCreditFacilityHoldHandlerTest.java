package com.example.ledgercore.credit.command.handler;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.credit.command.dto.DecreaseCreditFacilityHoldCommand;
import com.example.ledgercore.credit.command.repository.CreditFacilityCommandRepository;
import com.example.ledgercore.credit.entity.CreditFacility;
import com.example.ledgercore.credit.enums.CreditFacilityStatus;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DecreaseCreditFacilityHoldHandlerTest {

    @Mock
    private CreditFacilityCommandRepository
            creditFacilityCommandRepository;

    private DecreaseCreditFacilityHoldHandler handler;

    private UUID creditFacilityId;
    private Currency currency;

    @BeforeEach
    void setUp() {
        handler =
                new DecreaseCreditFacilityHoldHandler(
                        creditFacilityCommandRepository
                );

        creditFacilityId = UUID.randomUUID();
        currency = Currency.VND;
    }

    @Test
    void shouldDecreaseHoldAmountSuccessfully() {
        BigDecimal holdAmount =
                new BigDecimal("10000000");
        BigDecimal amount =
                new BigDecimal("3000000");
        BigDecimal expectedHoldAmount =
                new BigDecimal("7000000");

        CreditFacility facility =
                activeFacility(holdAmount);

        givenFacility(facility);

        handler.execute(
                command(amount, currency)
        );

        assertEquals(
                expectedHoldAmount,
                facility.getHoldAmount()
        );

        verify(creditFacilityCommandRepository)
                .findById(creditFacilityId);
    }

    @Test
    void shouldDecreaseHoldAmountToZero() {
        BigDecimal holdAmount =
                new BigDecimal("10000000");

        CreditFacility facility =
                activeFacility(holdAmount);

        givenFacility(facility);

        handler.execute(
                command(holdAmount, currency)
        );

        assertEquals(
                BigDecimal.ZERO,
                facility.getHoldAmount()
        );
    }

    @Test
    void shouldAllowAmountEqualToHoldAmount() {
        BigDecimal holdAmount =
                new BigDecimal("5000000");

        CreditFacility facility =
                activeFacility(holdAmount);

        givenFacility(facility);

        handler.execute(
                command(
                        new BigDecimal("5000000"),
                        currency
                )
        );

        assertEquals(
                BigDecimal.ZERO,
                facility.getHoldAmount()
        );
    }

    @Test
    void shouldAllowSmallestPositiveAmount() {
        BigDecimal holdAmount =
                new BigDecimal("1.0000");
        BigDecimal amount =
                new BigDecimal("0.0001");

        CreditFacility facility =
                activeFacility(holdAmount);

        givenFacility(facility);

        handler.execute(
                command(amount, currency)
        );

        assertEquals(
                new BigDecimal("0.9999"),
                facility.getHoldAmount()
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
                creditFacilityCommandRepository
        );
    }

    @Test
    void shouldThrowWhenCreditFacilityIdIsNull() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                new DecreaseCreditFacilityHoldCommand(
                                        null,
                                        new BigDecimal("1000000"),
                                        currency
                                )
                        )
                );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                creditFacilityCommandRepository
        );
    }

    @Test
    void shouldThrowWhenAmountIsNull() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                new DecreaseCreditFacilityHoldCommand(
                                        creditFacilityId,
                                        null,
                                        currency
                                )
                        )
                );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                creditFacilityCommandRepository
        );
    }

    @Test
    void shouldThrowWhenAmountIsZero() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                command(
                                        BigDecimal.ZERO,
                                        currency
                                )
                        )
                );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                creditFacilityCommandRepository
        );
    }

    @Test
    void shouldThrowWhenAmountIsNegative() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                command(
                                        new BigDecimal("-1"),
                                        currency
                                )
                        )
                );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                creditFacilityCommandRepository
        );
    }

    @Test
    void shouldThrowWhenCurrencyIsNull() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                new DecreaseCreditFacilityHoldCommand(
                                        creditFacilityId,
                                        new BigDecimal("1000000"),
                                        null
                                )
                        )
                );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                creditFacilityCommandRepository
        );
    }

    @Test
    void shouldThrowWhenCreditFacilityDoesNotExist() {
        when(
                creditFacilityCommandRepository
                        .findById(creditFacilityId)
        ).thenReturn(Optional.empty());

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                command(
                                        new BigDecimal("1000000"),
                                        currency
                                )
                        )
                );

        assertEquals(
                ErrorCode.CREDIT_FACILITY_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(
                creditFacilityCommandRepository
        ).findById(creditFacilityId);
    }

    @Test
    void shouldThrowWhenCreditFacilityIsSuspended() {
        BigDecimal holdAmount =
                new BigDecimal("10000000");

        CreditFacility facility =
                facility(
                        holdAmount,
                        CreditFacilityStatus.SUSPENDED
                );

        givenFacility(facility);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                command(
                                        new BigDecimal("1000000"),
                                        currency
                                )
                        )
                );

        assertEquals(
                ErrorCode.CREDIT_FACILITY_NOT_ACTIVE,
                exception.getErrorCode()
        );

        assertEquals(
                holdAmount,
                facility.getHoldAmount()
        );
    }

    @Test
    void shouldThrowWhenCreditFacilityIsClosed() {
        BigDecimal holdAmount =
                new BigDecimal("10000000");

        CreditFacility facility =
                facility(
                        holdAmount,
                        CreditFacilityStatus.CLOSED
                );

        givenFacility(facility);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                command(
                                        new BigDecimal("1000000"),
                                        currency
                                )
                        )
                );

        assertEquals(
                ErrorCode.CREDIT_FACILITY_NOT_ACTIVE,
                exception.getErrorCode()
        );

        assertEquals(
                holdAmount,
                facility.getHoldAmount()
        );
    }

    @Test
    void shouldThrowWhenCurrencyDoesNotMatch() {
        Currency differentCurrency =
                currency == Currency.VND
                        ? Currency.USD
                        : Currency.VND;

        BigDecimal holdAmount =
                new BigDecimal("10000000");

        CreditFacility facility =
                activeFacility(holdAmount);

        givenFacility(facility);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                command(
                                        new BigDecimal("1000000"),
                                        differentCurrency
                                )
                        )
                );

        assertEquals(
                ErrorCode.CREDIT_FACILITY_CURRENCY_MISMATCH,
                exception.getErrorCode()
        );

        assertEquals(
                holdAmount,
                facility.getHoldAmount()
        );
    }

    @Test
    void shouldThrowWhenHoldAmountIsInsufficient() {
        BigDecimal holdAmount =
                new BigDecimal("5000000");
        BigDecimal amount =
                new BigDecimal("5000001");

        CreditFacility facility =
                activeFacility(holdAmount);

        givenFacility(facility);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                command(amount, currency)
                        )
                );

        assertEquals(
                ErrorCode.CREDIT_HOLD_AMOUNT_INSUFFICIENT,
                exception.getErrorCode()
        );

        assertEquals(
                holdAmount,
                facility.getHoldAmount()
        );
    }

    @Test
    void shouldNotChangeHoldAmountWhenAmountExceedsHoldAmount() {
        BigDecimal holdAmount =
                new BigDecimal("1000000");
        BigDecimal amount =
                new BigDecimal("1000001");

        CreditFacility facility =
                activeFacility(holdAmount);

        givenFacility(facility);

        assertThrows(
                BusinessException.class,
                () -> handler.execute(
                        command(amount, currency)
                )
        );

        assertEquals(
                holdAmount,
                facility.getHoldAmount()
        );
    }

    @Test
    void shouldNotInteractWithRepositoryWhenCommandIsInvalid() {
        assertThrows(
                BusinessException.class,
                () -> handler.execute(
                        new DecreaseCreditFacilityHoldCommand(
                                null,
                                null,
                                null
                        )
                ));

        verifyNoInteractions(
                creditFacilityCommandRepository
        );
    }

    private void givenFacility(CreditFacility facility) {
        when(
                creditFacilityCommandRepository
                        .findById(creditFacilityId)
        ).thenReturn(Optional.of(facility));
    }

    private DecreaseCreditFacilityHoldCommand command(
            BigDecimal amount,
            Currency currency
    ) {
        return new DecreaseCreditFacilityHoldCommand(
                creditFacilityId,
                amount,
                currency
        );
    }

    private CreditFacility activeFacility(
            BigDecimal holdAmount
    ) {
        return facility(
                holdAmount,
                CreditFacilityStatus.ACTIVE
        );
    }

    private CreditFacility facility(
            BigDecimal holdAmount,
            CreditFacilityStatus status
    ) {
        return CreditFacility.builder()
                .id(creditFacilityId)
                .customerId(UUID.randomUUID())
                .productId(UUID.randomUUID())
                .creditLimit(
                        new BigDecimal("100000000")
                )
                .outstandingBalance(
                        BigDecimal.ZERO
                )
                .holdAmount(holdAmount)
                .currency(currency)
                .status(status)
                .openedAt(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}