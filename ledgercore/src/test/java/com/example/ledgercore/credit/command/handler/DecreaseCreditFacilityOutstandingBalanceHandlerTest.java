package com.example.ledgercore.credit.command.handler;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.credit.command.dto.DecreaseCreditFacilityOutstandingBalanceCommand;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DecreaseCreditFacilityOutstandingBalanceHandlerTest {

    @Mock
    private CreditFacilityCommandRepository creditFacilityCommandRepository;

    @Mock
    private CreditDailyBalanceService creditDailyBalanceService;

    private DecreaseCreditFacilityOutstandingBalanceHandler handler;

    private UUID creditFacilityId;
    private Currency currency;
    private LocalDate businessDate;

    @BeforeEach
    void setUp() {
        handler =
                new DecreaseCreditFacilityOutstandingBalanceHandler(
                        creditFacilityCommandRepository,
                        creditDailyBalanceService
                );

        creditFacilityId = UUID.randomUUID();
        currency = Currency.VND;
        businessDate = LocalDate.of(2026, 9, 23);
    }

    @Test
    void shouldDecreaseOutstandingBalanceSuccessfully() {
        BigDecimal outstandingBalance =
                new BigDecimal("10000000");

        BigDecimal amount =
                new BigDecimal("3000000");

        BigDecimal expectedBalance =
                new BigDecimal("7000000");

        CreditFacility facility =
                activeFacility(
                        outstandingBalance
                );

        DecreaseCreditFacilityOutstandingBalanceCommand command =
                command(amount);

        when(
                creditFacilityCommandRepository.findById(
                        creditFacilityId
                )
        ).thenReturn(Optional.of(facility));

        handler.execute(command);

        assertEquals(
                expectedBalance,
                facility.getOutstandingBalance()
        );

        verify(
                creditDailyBalanceService
        ).updateClosingBalance(
                creditFacilityId,
                businessDate,
                expectedBalance
        );
    }

    @Test
    void shouldDecreaseOutstandingBalanceToZero() {
        BigDecimal outstandingBalance =
                new BigDecimal("10000000");

        CreditFacility facility =
                activeFacility(
                        outstandingBalance
                );

        DecreaseCreditFacilityOutstandingBalanceCommand command =
                command(outstandingBalance);

        when(
                creditFacilityCommandRepository.findById(
                        creditFacilityId
                )
        ).thenReturn(Optional.of(facility));

        handler.execute(command);

        assertEquals(
                BigDecimal.ZERO,
                facility.getOutstandingBalance()
        );

        verify(
                creditDailyBalanceService
        ).updateClosingBalance(
                creditFacilityId,
                businessDate,
                BigDecimal.ZERO
        );
    }

    @Test
    void shouldUpdateClosingBalanceWithNewOutstandingBalance() {
        BigDecimal outstandingBalance =
                new BigDecimal("50000000");

        BigDecimal amount =
                new BigDecimal("12500000");

        BigDecimal expectedBalance =
                new BigDecimal("37500000");

        CreditFacility facility =
                activeFacility(
                        outstandingBalance
                );

        DecreaseCreditFacilityOutstandingBalanceCommand command =
                command(amount);

        when(
                creditFacilityCommandRepository.findById(
                        creditFacilityId
                )
        ).thenReturn(Optional.of(facility));

        handler.execute(command);

        verify(
                creditDailyBalanceService
        ).updateClosingBalance(
                creditFacilityId,
                businessDate,
                expectedBalance
        );
    }

    @Test
    void shouldUpdateFacilityUpdatedAt() {
        Instant before =
                Instant.now();

        CreditFacility facility =
                activeFacility(
                        new BigDecimal("10000000")
                );

        facility.setUpdatedAt(before);

        DecreaseCreditFacilityOutstandingBalanceCommand command =
                command(new BigDecimal("1000000"));

        when(
                creditFacilityCommandRepository.findById(
                        creditFacilityId
                )
        ).thenReturn(Optional.of(facility));

        handler.execute(command);

        assertEquals(
                false,
                facility.getUpdatedAt().isBefore(before)
        );
    }

    @Test
    void shouldAllowSmallestPositiveAmount() {
        BigDecimal outstandingBalance =
                new BigDecimal("1.0000");

        BigDecimal amount =
                new BigDecimal("0.0001");

        CreditFacility facility =
                activeFacility(
                        outstandingBalance
                );

        DecreaseCreditFacilityOutstandingBalanceCommand command =
                command(amount);

        when(
                creditFacilityCommandRepository.findById(
                        creditFacilityId
                )
        ).thenReturn(Optional.of(facility));

        handler.execute(command);

        assertEquals(
                new BigDecimal("0.9999"),
                facility.getOutstandingBalance()
        );

        verify(
                creditDailyBalanceService
        ).updateClosingBalance(
                creditFacilityId,
                businessDate,
                new BigDecimal("0.9999")
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
        DecreaseCreditFacilityOutstandingBalanceCommand command =
                new DecreaseCreditFacilityOutstandingBalanceCommand(
                        null,
                        new BigDecimal("1000000"),
                        currency,
                        businessDate
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
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
        DecreaseCreditFacilityOutstandingBalanceCommand command =
                new DecreaseCreditFacilityOutstandingBalanceCommand(
                        creditFacilityId,
                        null,
                        currency,
                        businessDate
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
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
        DecreaseCreditFacilityOutstandingBalanceCommand command =
                new DecreaseCreditFacilityOutstandingBalanceCommand(
                        creditFacilityId,
                        new BigDecimal("1000000"),
                        null,
                        businessDate
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
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
    void shouldNotThrowWhenBusinessDateIsNull() {
        /*
         * businessDate is not validated by validateCommand().
         * The current implementation allows null and passes it
         * to CreditDailyBalanceService.
         */
        CreditFacility facility =
                activeFacility(
                        new BigDecimal("10000000")
                );

        DecreaseCreditFacilityOutstandingBalanceCommand command =
                new DecreaseCreditFacilityOutstandingBalanceCommand(
                        creditFacilityId,
                        new BigDecimal("1000000"),
                        currency,
                        null
                );

        when(
                creditFacilityCommandRepository.findById(
                        creditFacilityId
                )
        ).thenReturn(Optional.of(facility));

        handler.execute(command);

        assertEquals(
                new BigDecimal("9000000"),
                facility.getOutstandingBalance()
        );

        verify(
                creditDailyBalanceService
        ).updateClosingBalance(
                creditFacilityId,
                null,
                new BigDecimal("9000000")
        );
    }

    @Test
    void shouldThrowWhenAmountIsZero() {
        DecreaseCreditFacilityOutstandingBalanceCommand command =
                command(BigDecimal.ZERO);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
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
        DecreaseCreditFacilityOutstandingBalanceCommand command =
                command(new BigDecimal("-1"));

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
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
        DecreaseCreditFacilityOutstandingBalanceCommand command =
                command(new BigDecimal("1000000"));

        when(
                creditFacilityCommandRepository.findById(
                        creditFacilityId
                )
        ).thenReturn(Optional.empty());

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
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
    void shouldThrowWhenCreditFacilityIsClosed() {
        CreditFacility facility =
                facility(
                        new BigDecimal("10000000"),
                        CreditFacilityStatus.CLOSED
                );

        DecreaseCreditFacilityOutstandingBalanceCommand command =
                command(new BigDecimal("1000000"));

        when(
                creditFacilityCommandRepository.findById(
                        creditFacilityId
                )
        ).thenReturn(Optional.of(facility));

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.CREDIT_FACILITY_NOT_ACTIVE,
                exception.getErrorCode()
        );

        assertEquals(
                new BigDecimal("10000000"),
                facility.getOutstandingBalance()
        );

        verifyNoInteractions(
                creditDailyBalanceService
        );
    }

    @Test
    void shouldThrowWhenCreditFacilityIsSuspended() {
        CreditFacility facility =
                facility(
                        new BigDecimal("10000000"),
                        CreditFacilityStatus.SUSPENDED
                );

        DecreaseCreditFacilityOutstandingBalanceCommand command =
                command(new BigDecimal("1000000"));

        when(
                creditFacilityCommandRepository.findById(
                        creditFacilityId
                )
        ).thenReturn(Optional.of(facility));

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.CREDIT_FACILITY_NOT_ACTIVE,
                exception.getErrorCode()
        );

        assertEquals(
                new BigDecimal("10000000"),
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

        CreditFacility facility =
                activeFacility(
                        new BigDecimal("10000000")
                );

        DecreaseCreditFacilityOutstandingBalanceCommand command =
                new DecreaseCreditFacilityOutstandingBalanceCommand(
                        creditFacilityId,
                        new BigDecimal("1000000"),
                        differentCurrency,
                        businessDate
                );

        when(
                creditFacilityCommandRepository.findById(
                        creditFacilityId
                )
        ).thenReturn(Optional.of(facility));

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.CREDIT_FACILITY_CURRENCY_MISMATCH,
                exception.getErrorCode()
        );

        assertEquals(
                new BigDecimal("10000000"),
                facility.getOutstandingBalance()
        );

        verifyNoInteractions(
                creditDailyBalanceService
        );
    }

    @Test
    void shouldThrowWhenOutstandingBalanceIsInsufficient() {
        BigDecimal outstandingBalance =
                new BigDecimal("5000000");

        BigDecimal amount =
                new BigDecimal("5000001");

        CreditFacility facility =
                activeFacility(
                        outstandingBalance
                );

        DecreaseCreditFacilityOutstandingBalanceCommand command =
                command(amount);

        when(
                creditFacilityCommandRepository.findById(
                        creditFacilityId
                )
        ).thenReturn(Optional.of(facility));

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.CREDIT_OUTSTANDING_BALANCE_INSUFFICIENT,
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
    void shouldNotDecreaseOutstandingBalanceWhenAmountExceedsBalance() {
        BigDecimal outstandingBalance =
                new BigDecimal("1000000");

        BigDecimal amount =
                new BigDecimal("1000001");

        CreditFacility facility =
                activeFacility(
                        outstandingBalance
                );

        DecreaseCreditFacilityOutstandingBalanceCommand command =
                command(amount);

        when(
                creditFacilityCommandRepository.findById(
                        creditFacilityId
                )
        ).thenReturn(Optional.of(facility));

        assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        assertEquals(
                outstandingBalance,
                facility.getOutstandingBalance()
        );

        verify(
                creditDailyBalanceService,
                never()
        ).updateClosingBalance(
                any(UUID.class),
                any(LocalDate.class),
                any(BigDecimal.class)
        );
    }

    @Test
    void shouldDecreaseOutstandingBalanceAndUpdateClosingBalance() {
        BigDecimal outstandingBalance =
                new BigDecimal("25000000");

        BigDecimal amount =
                new BigDecimal("7500000");

        BigDecimal expectedBalance =
                new BigDecimal("17500000");

        CreditFacility facility =
                activeFacility(
                        outstandingBalance
                );

        DecreaseCreditFacilityOutstandingBalanceCommand command =
                command(amount);

        when(
                creditFacilityCommandRepository.findById(
                        creditFacilityId
                )
        ).thenReturn(Optional.of(facility));

        handler.execute(command);

        assertEquals(
                expectedBalance,
                facility.getOutstandingBalance()
        );

        verify(
                creditDailyBalanceService
        ).updateClosingBalance(
                eq(creditFacilityId),
                eq(businessDate),
                eq(expectedBalance)
        );
    }

    private DecreaseCreditFacilityOutstandingBalanceCommand command(
            BigDecimal amount
    ) {
        return new DecreaseCreditFacilityOutstandingBalanceCommand(
                creditFacilityId,
                amount,
                currency,
                businessDate
        );
    }

    private CreditFacility activeFacility(
            BigDecimal outstandingBalance
    ) {
        return facility(
                outstandingBalance,
                CreditFacilityStatus.ACTIVE
        );
    }

    private CreditFacility facility(
            BigDecimal outstandingBalance,
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
                        outstandingBalance
                )
                .currency(currency)
                .status(status)
                .openedAt(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}