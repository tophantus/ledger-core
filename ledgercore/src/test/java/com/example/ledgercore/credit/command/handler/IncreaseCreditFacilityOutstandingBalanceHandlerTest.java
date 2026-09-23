package com.example.ledgercore.credit.command.handler;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.credit.command.dto.IncreaseCreditFacilityOutstandingBalanceCommand;
import com.example.ledgercore.credit.command.repository.CreditFacilityCommandRepository;
import com.example.ledgercore.credit.command.service.CreditDailyBalanceService;
import com.example.ledgercore.credit.entity.CreditFacility;
import com.example.ledgercore.credit.enums.CreditFacilityStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IncreaseCreditFacilityOutstandingBalanceHandlerTest {

    @Mock
    private CreditFacilityCommandRepository
            creditFacilityCommandRepository;

    @Mock
    private CreditDailyBalanceService
            creditDailyBalanceService;

    private IncreaseCreditFacilityOutstandingBalanceHandler handler;

    private UUID creditFacilityId;
    private Currency currency;
    private LocalDate businessDate;

    @BeforeEach
    void setUp() {
        handler =
                new IncreaseCreditFacilityOutstandingBalanceHandler(
                        creditFacilityCommandRepository,
                        creditDailyBalanceService
                );

        creditFacilityId = UUID.randomUUID();
        currency = Currency.VND;
        businessDate = LocalDate.of(2026, 9, 23);
    }

    @Test
    void shouldIncreaseOutstandingBalanceSuccessfully() {
        BigDecimal outstandingBalance =
                new BigDecimal("30000000");
        BigDecimal amount =
                new BigDecimal("10000000");
        BigDecimal creditLimit =
                new BigDecimal("100000000");

        BigDecimal expectedOutstandingBalance =
                new BigDecimal("40000000");

        CreditFacility facility =
                activeFacility(
                        outstandingBalance,
                        creditLimit
                );

        givenFacility(facility);

        handler.execute(
                command(
                        amount,
                        currency,
                        businessDate
                )
        );

        assertEquals(
                expectedOutstandingBalance,
                facility.getOutstandingBalance()
        );

        verify(
                creditFacilityCommandRepository
        ).findById(creditFacilityId);

        verify(
                creditDailyBalanceService
        ).updateClosingBalance(
                creditFacilityId,
                businessDate,
                expectedOutstandingBalance
        );
    }

    @Test
    void shouldIncreaseOutstandingBalanceFromZero() {
        BigDecimal amount =
                new BigDecimal("10000000");
        BigDecimal creditLimit =
                new BigDecimal("100000000");

        CreditFacility facility =
                activeFacility(
                        BigDecimal.ZERO,
                        creditLimit
                );

        givenFacility(facility);

        handler.execute(
                command(
                        amount,
                        currency,
                        businessDate
                )
        );

        assertEquals(
                amount,
                facility.getOutstandingBalance()
        );

        verify(
                creditDailyBalanceService
        ).updateClosingBalance(
                creditFacilityId,
                businessDate,
                amount
        );
    }

    @Test
    void shouldAllowOutstandingBalanceToEqualCreditLimit() {
        BigDecimal outstandingBalance =
                new BigDecimal("90000000");
        BigDecimal amount =
                new BigDecimal("10000000");
        BigDecimal creditLimit =
                new BigDecimal("100000000");

        CreditFacility facility =
                activeFacility(
                        outstandingBalance,
                        creditLimit
                );

        givenFacility(facility);

        handler.execute(
                command(
                        amount,
                        currency,
                        businessDate
                )
        );

        assertEquals(
                creditLimit,
                facility.getOutstandingBalance()
        );

        verify(
                creditDailyBalanceService
        ).updateClosingBalance(
                creditFacilityId,
                businessDate,
                creditLimit
        );
    }

    @Test
    void shouldAllowSmallestPositiveAmount() {
        BigDecimal outstandingBalance =
                new BigDecimal("1.0000");
        BigDecimal amount =
                new BigDecimal("0.0001");
        BigDecimal creditLimit =
                new BigDecimal("100.0000");

        CreditFacility facility =
                activeFacility(
                        outstandingBalance,
                        creditLimit
                );

        givenFacility(facility);

        handler.execute(
                command(
                        amount,
                        currency,
                        businessDate
                )
        );

        assertEquals(
                new BigDecimal("1.0001"),
                facility.getOutstandingBalance()
        );

        verify(
                creditDailyBalanceService
        ).updateClosingBalance(
                creditFacilityId,
                businessDate,
                new BigDecimal("1.0001")
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
                creditFacilityCommandRepository,
                creditDailyBalanceService
        );
    }

    @Test
    void shouldThrowWhenCreditFacilityIdIsNull() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                new IncreaseCreditFacilityOutstandingBalanceCommand(
                                        null,
                                        new BigDecimal("1000000"),
                                        currency,
                                        businessDate
                                )
                        )
                );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                creditFacilityCommandRepository,
                creditDailyBalanceService
        );
    }

    @Test
    void shouldThrowWhenAmountIsNull() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                new IncreaseCreditFacilityOutstandingBalanceCommand(
                                        creditFacilityId,
                                        null,
                                        currency,
                                        businessDate
                                )
                        )
                );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                creditFacilityCommandRepository,
                creditDailyBalanceService
        );
    }

    @Test
    void shouldThrowWhenCurrencyIsNull() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                new IncreaseCreditFacilityOutstandingBalanceCommand(
                                        creditFacilityId,
                                        new BigDecimal("1000000"),
                                        null,
                                        businessDate
                                )
                        )
                );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                creditFacilityCommandRepository,
                creditDailyBalanceService
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
                                        currency,
                                        businessDate
                                )
                        )
                );

        assertEquals(
                ErrorCode.INVALID_AMOUNT,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                creditFacilityCommandRepository,
                creditDailyBalanceService
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
                                        currency,
                                        businessDate
                                )
                        )
                );

        assertEquals(
                ErrorCode.INVALID_AMOUNT,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                creditFacilityCommandRepository,
                creditDailyBalanceService
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
                                        currency,
                                        businessDate
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

        verifyNoInteractions(
                creditDailyBalanceService
        );
    }

    @Test
    void shouldThrowWhenCreditFacilityIsSuspended() {
        BigDecimal outstandingBalance =
                new BigDecimal("30000000");

        CreditFacility facility =
                facility(
                        outstandingBalance,
                        new BigDecimal("100000000"),
                        CreditFacilityStatus.SUSPENDED
                );

        givenFacility(facility);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                command(
                                        new BigDecimal("1000000"),
                                        currency,
                                        businessDate
                                )
                        )
                );

        assertEquals(
                ErrorCode.CREDIT_FACILITY_NOT_ACTIVE,
                exception.getErrorCode()
        );

        assertEquals(
                outstandingBalance,
                facility.getOutstandingBalance()
        );

        verifyNoInteractions(
                creditDailyBalanceService
        );
    }

    @Test
    void shouldThrowWhenCreditFacilityIsClosed() {
        BigDecimal outstandingBalance =
                new BigDecimal("30000000");

        CreditFacility facility =
                facility(
                        outstandingBalance,
                        new BigDecimal("100000000"),
                        CreditFacilityStatus.CLOSED
                );

        givenFacility(facility);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                command(
                                        new BigDecimal("1000000"),
                                        currency,
                                        businessDate
                                )
                        )
                );

        assertEquals(
                ErrorCode.CREDIT_FACILITY_NOT_ACTIVE,
                exception.getErrorCode()
        );

        assertEquals(
                outstandingBalance,
                facility.getOutstandingBalance()
        );

        verifyNoInteractions(
                creditDailyBalanceService
        );
    }

    @Test
    void shouldThrowWhenCurrencyDoesNotMatch() {
        Currency differentCurrency =
                currency == Currency.VND
                        ? Currency.USD
                        : Currency.VND;

        BigDecimal outstandingBalance =
                new BigDecimal("30000000");

        CreditFacility facility =
                activeFacility(
                        outstandingBalance,
                        new BigDecimal("100000000")
                );

        givenFacility(facility);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                command(
                                        new BigDecimal("1000000"),
                                        differentCurrency,
                                        businessDate
                                )
                        )
                );

        assertEquals(
                ErrorCode.CREDIT_FACILITY_CURRENCY_MISMATCH,
                exception.getErrorCode()
        );

        assertEquals(
                outstandingBalance,
                facility.getOutstandingBalance()
        );

        verifyNoInteractions(
                creditDailyBalanceService
        );
    }

    @Test
    void shouldThrowWhenCreditLimitIsExceeded() {
        BigDecimal outstandingBalance =
                new BigDecimal("90000000");
        BigDecimal amount =
                new BigDecimal("10000001");
        BigDecimal creditLimit =
                new BigDecimal("100000000");

        CreditFacility facility =
                activeFacility(
                        outstandingBalance,
                        creditLimit
                );

        givenFacility(facility);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                command(
                                        amount,
                                        currency,
                                        businessDate
                                )
                        )
                );

        assertEquals(
                ErrorCode.CREDIT_LIMIT_EXCEEDED,
                exception.getErrorCode()
        );

        assertEquals(
                outstandingBalance,
                facility.getOutstandingBalance()
        );

        verifyNoInteractions(
                creditDailyBalanceService
        );
    }

    @Test
    void shouldNotChangeOutstandingBalanceWhenCreditLimitIsExceeded() {
        BigDecimal outstandingBalance =
                new BigDecimal("100000000");
        BigDecimal amount =
                new BigDecimal("1");
        BigDecimal creditLimit =
                new BigDecimal("100000000");

        CreditFacility facility =
                activeFacility(
                        outstandingBalance,
                        creditLimit
                );

        givenFacility(facility);

        assertThrows(
                BusinessException.class,
                () -> handler.execute(
                        command(
                                amount,
                                currency,
                                businessDate
                        )
                )
        );

        assertEquals(
                outstandingBalance,
                facility.getOutstandingBalance()
        );

        verifyNoInteractions(
                creditDailyBalanceService
        );
    }

    @Test
    void shouldNotInteractWithDependenciesWhenInputIsInvalid() {
        assertThrows(
                BusinessException.class,
                () -> handler.execute(
                        new IncreaseCreditFacilityOutstandingBalanceCommand(
                                null,
                                null,
                                null,
                                null
                        )
                ));

        verifyNoInteractions(
                creditFacilityCommandRepository,
                creditDailyBalanceService
        );
    }

    private void givenFacility(CreditFacility facility) {
        when(
                creditFacilityCommandRepository
                        .findById(creditFacilityId)
        ).thenReturn(Optional.of(facility));
    }

    private IncreaseCreditFacilityOutstandingBalanceCommand command(
            BigDecimal amount,
            Currency currency,
            LocalDate businessDate
    ) {
        return new IncreaseCreditFacilityOutstandingBalanceCommand(
                creditFacilityId,
                amount,
                currency,
                businessDate
        );
    }

    private CreditFacility activeFacility(
            BigDecimal outstandingBalance,
            BigDecimal creditLimit
    ) {
        return facility(
                outstandingBalance,
                creditLimit,
                CreditFacilityStatus.ACTIVE
        );
    }

    private CreditFacility facility(
            BigDecimal outstandingBalance,
            BigDecimal creditLimit,
            CreditFacilityStatus status
    ) {
        return CreditFacility.builder()
                .id(creditFacilityId)
                .customerId(UUID.randomUUID())
                .productId(UUID.randomUUID())
                .creditLimit(creditLimit)
                .outstandingBalance(outstandingBalance)
                .holdAmount(BigDecimal.ZERO)
                .currency(currency)
                .status(status)
                .openedAt(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}