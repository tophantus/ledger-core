
package com.example.ledgercore.credit.command.service.impl;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.credit.command.port.outbound.CreditStatementAmountsPort;
import com.example.ledgercore.credit.command.port.outbound.dto.CreditStatementAmounts;
import com.example.ledgercore.credit.command.repository.CreditDailyBalanceCommandRepository;
import com.example.ledgercore.credit.command.repository.CreditFacilityCommandRepository;
import com.example.ledgercore.credit.command.repository.CreditStatementCommandRepository;
import com.example.ledgercore.credit.command.service.CreditMinimumPaymentService;
import com.example.ledgercore.credit.command.service.dto.CreateCreditStatementCommand;
import com.example.ledgercore.credit.entity.CreditDailyBalance;
import com.example.ledgercore.credit.entity.CreditFacility;
import com.example.ledgercore.credit.entity.CreditStatement;
import com.example.ledgercore.credit.enums.CreditFacilityStatus;
import com.example.ledgercore.credit.enums.CreditStatementStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateCreditStatementServiceImplTest {

    @Mock
    private CreditFacilityCommandRepository facilityRepository;

    @Mock
    private CreditStatementCommandRepository statementRepository;

    @Mock
    private CreditDailyBalanceCommandRepository dailyBalanceRepository;

    @Mock
    private CreditStatementAmountsPort amountsPort;

    @Mock
    private CreditMinimumPaymentService minimumPaymentService;

    @Mock
    private CreditFacility facility;

    @Mock
    private CreditDailyBalance dailyBalance;

    @InjectMocks
    private CreateCreditStatementServiceImpl handler;

    private UUID runId;
    private UUID creditFacilityId;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private LocalDate statementDate;
    private LocalDate dueDate;
    private CreditStatementAmounts amounts;

    private static final BigDecimal PURCHASES = new BigDecimal("1000.00");
    private static final BigDecimal PAYMENTS = new BigDecimal("300.00");
    private static final BigDecimal FEES = new BigDecimal("50.00");
    private static final BigDecimal ACCRUED_INTEREST =
            new BigDecimal("20.00");
    private static final BigDecimal POSTED_INTEREST =
            new BigDecimal("20.00");

    private static final BigDecimal CLOSING_BALANCE =
            new BigDecimal("770.00");

    private static final BigDecimal MINIMUM_PAYMENT =
            new BigDecimal("50.00");

    @BeforeEach
    void setUp() {
        runId = UUID.randomUUID();
        creditFacilityId = UUID.randomUUID();

        periodStart = LocalDate.of(2026, 9, 1);
        periodEnd = LocalDate.of(2026, 9, 30);
        statementDate = LocalDate.of(2026, 9, 30);
        dueDate = LocalDate.of(2026, 10, 20);

        amounts = new CreditStatementAmounts(
                PURCHASES,
                PAYMENTS,
                FEES,
                ACCRUED_INTEREST,
                POSTED_INTEREST
        );
    }

    // -------------------------------------------------------------------------
    // Success cases
    // -------------------------------------------------------------------------

    @Test
    void execute_shouldCreateIssuedStatement_whenCommandIsValid() {
        CreateCreditStatementCommand command = validCommand();

        givenActiveFacility();
        givenFacilityCurrency();
        givenNoExistingStatement();
        givenNoPreviousStatement();
        givenDailyBalance(CLOSING_BALANCE);
        givenAmounts(amounts);
        givenMinimumPayment(CLOSING_BALANCE, MINIMUM_PAYMENT);

        assertDoesNotThrow(() -> handler.execute(command));

        CreditStatement statement = captureSavedStatement();

        assertNotNull(statement.getId());
        assertEquals(creditFacilityId, statement.getCreditFacilityId());
        assertEquals(periodStart, statement.getPeriodStart());
        assertEquals(periodEnd, statement.getPeriodEnd());
        assertEquals(statementDate, statement.getStatementDate());
        assertEquals(dueDate, statement.getDueDate());

        assertBigDecimalEquals(
                BigDecimal.ZERO,
                statement.getOpeningBalance()
        );
        assertBigDecimalEquals(
                PURCHASES,
                statement.getPurchasesAmount()
        );
        assertBigDecimalEquals(
                PAYMENTS,
                statement.getPaymentsAmount()
        );
        assertBigDecimalEquals(
                FEES,
                statement.getFeesAmount()
        );
        assertBigDecimalEquals(
                ACCRUED_INTEREST,
                statement.getInterestAmount()
        );
        assertBigDecimalEquals(
                POSTED_INTEREST,
                statement.getPostedInterestAmount()
        );
        assertBigDecimalEquals(
                CLOSING_BALANCE,
                statement.getClosingBalance()
        );
        assertBigDecimalEquals(
                MINIMUM_PAYMENT,
                statement.getMinimumPayment()
        );
        assertBigDecimalEquals(
                BigDecimal.ZERO,
                statement.getPaidAmount()
        );

        assertEquals(CreditStatementStatus.ISSUED, statement.getStatus());
        assertNotNull(statement.getCreatedAt());
        assertNotNull(statement.getUpdatedAt());
        assertEquals(statement.getCreatedAt(), statement.getUpdatedAt());

        verify(minimumPaymentService)
                .calculate(CLOSING_BALANCE, Currency.VND);
        verify(statementRepository).save(any(CreditStatement.class));
    }

    @Test
    void execute_shouldCreateNoPaymentDueStatement_whenClosingBalanceIsZero() {
        CreateCreditStatementCommand command = validCommand();

        CreditStatementAmounts zeroBalanceAmounts =
                new CreditStatementAmounts(
                        new BigDecimal("300.00"),
                        new BigDecimal("300.00"),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                );

        givenActiveFacility();
        givenFacilityCurrency();
        givenNoExistingStatement();
        givenNoPreviousStatement();
        givenDailyBalance(BigDecimal.ZERO);
        givenAmounts(zeroBalanceAmounts);
        givenMinimumPayment(BigDecimal.ZERO, BigDecimal.ZERO);

        handler.execute(command);

        CreditStatement statement = captureSavedStatement();

        assertBigDecimalEquals(
                BigDecimal.ZERO,
                statement.getOpeningBalance()
        );
        assertBigDecimalEquals(
                BigDecimal.ZERO,
                statement.getClosingBalance()
        );
        assertBigDecimalEquals(
                BigDecimal.ZERO,
                statement.getMinimumPayment()
        );
        assertEquals(
                CreditStatementStatus.NO_PAYMENT_DUE,
                statement.getStatus()
        );
        assertBigDecimalEquals(
                BigDecimal.ZERO,
                statement.getPostedInterestAmount()
        );

        verify(minimumPaymentService)
                .calculate(BigDecimal.ZERO, Currency.VND);
    }

    @Test
    void execute_shouldUsePreviousStatementClosingBalanceAsOpeningBalance() {
        CreateCreditStatementCommand command = validCommand();
        CreditStatement previousStatement = mock(CreditStatement.class);

        BigDecimal openingBalance = new BigDecimal("500.00");
        BigDecimal closingBalance = new BigDecimal("1270.00");
        BigDecimal minimumPayment = new BigDecimal("62.50");

        givenActiveFacility();
        givenFacilityCurrency();
        givenNoExistingStatement();

        when(statementRepository
                .findFirstByCreditFacilityIdAndPeriodEndLessThanOrderByPeriodEndDesc(
                        creditFacilityId,
                        periodStart
                ))
                .thenReturn(Optional.of(previousStatement));
        when(previousStatement.getClosingBalance())
                .thenReturn(openingBalance);

        givenDailyBalance(closingBalance);
        givenAmounts(amounts);
        givenMinimumPayment(closingBalance, minimumPayment);

        handler.execute(command);

        CreditStatement statement = captureSavedStatement();

        assertBigDecimalEquals(
                openingBalance,
                statement.getOpeningBalance()
        );
        assertBigDecimalEquals(
                closingBalance,
                statement.getClosingBalance()
        );
        assertBigDecimalEquals(
                POSTED_INTEREST,
                statement.getPostedInterestAmount()
        );
        assertBigDecimalEquals(
                minimumPayment,
                statement.getMinimumPayment()
        );

        verify(statementRepository)
                .findFirstByCreditFacilityIdAndPeriodEndLessThanOrderByPeriodEndDesc(
                        creditFacilityId,
                        periodStart
                );
    }

    @Test
    void execute_shouldUseZeroOpeningBalance_whenPreviousStatementDoesNotExist() {
        CreateCreditStatementCommand command = validCommand();

        givenActiveFacility();
        givenFacilityCurrency();
        givenNoExistingStatement();
        givenNoPreviousStatement();
        givenDailyBalance(CLOSING_BALANCE);
        givenAmounts(amounts);
        givenMinimumPayment(CLOSING_BALANCE, MINIMUM_PAYMENT);

        handler.execute(command);

        CreditStatement statement = captureSavedStatement();

        assertBigDecimalEquals(
                BigDecimal.ZERO,
                statement.getOpeningBalance()
        );
        assertBigDecimalEquals(
                CLOSING_BALANCE,
                statement.getClosingBalance()
        );
        assertBigDecimalEquals(
                POSTED_INTEREST,
                statement.getPostedInterestAmount()
        );
    }

    // -------------------------------------------------------------------------
    // Command validation
    // -------------------------------------------------------------------------

    @Test
    void execute_shouldThrowInvalidRequest_whenCommandIsNull() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(null)
        );

        assertEquals(ErrorCode.INVALID_REQUEST, exception.getErrorCode());

        verifyNoInteractions(
                facilityRepository,
                statementRepository,
                dailyBalanceRepository,
                amountsPort,
                minimumPaymentService
        );
    }

    @Test
    void execute_shouldThrowInvalidRequest_whenRunIdIsNull() {
        assertInvalidCommand(new CreateCreditStatementCommand(
                null,
                creditFacilityId,
                periodStart,
                periodEnd,
                statementDate,
                dueDate
        ));
    }

    @Test
    void execute_shouldThrowInvalidRequest_whenCreditFacilityIdIsNull() {
        assertInvalidCommand(new CreateCreditStatementCommand(
                runId,
                null,
                periodStart,
                periodEnd,
                statementDate,
                dueDate
        ));
    }

    @Test
    void execute_shouldThrowInvalidRequest_whenPeriodStartIsNull() {
        assertInvalidCommand(new CreateCreditStatementCommand(
                runId,
                creditFacilityId,
                null,
                periodEnd,
                statementDate,
                dueDate
        ));
    }

    @Test
    void execute_shouldThrowInvalidRequest_whenPeriodEndIsNull() {
        assertInvalidCommand(new CreateCreditStatementCommand(
                runId,
                creditFacilityId,
                periodStart,
                null,
                statementDate,
                dueDate
        ));
    }

    @Test
    void execute_shouldThrowInvalidRequest_whenStatementDateIsNull() {
        assertInvalidCommand(new CreateCreditStatementCommand(
                runId,
                creditFacilityId,
                periodStart,
                periodEnd,
                null,
                dueDate
        ));
    }

    @Test
    void execute_shouldThrowInvalidRequest_whenDueDateIsNull() {
        assertInvalidCommand(new CreateCreditStatementCommand(
                runId,
                creditFacilityId,
                periodStart,
                periodEnd,
                statementDate,
                null
        ));
    }

    @Test
    void execute_shouldThrowInvalidRequest_whenPeriodStartIsAfterPeriodEnd() {
        assertInvalidCommand(new CreateCreditStatementCommand(
                runId,
                creditFacilityId,
                periodEnd,
                periodStart,
                statementDate,
                dueDate
        ));
    }

    @Test
    void execute_shouldThrowInvalidRequest_whenPeriodEndIsAfterStatementDate() {
        assertInvalidCommand(new CreateCreditStatementCommand(
                runId,
                creditFacilityId,
                periodStart,
                periodEnd,
                periodStart,
                dueDate
        ));
    }

    @Test
    void execute_shouldThrowInvalidRequest_whenStatementDateIsAfterDueDate() {
        assertInvalidCommand(new CreateCreditStatementCommand(
                runId,
                creditFacilityId,
                periodStart,
                periodEnd,
                dueDate,
                statementDate
        ));
    }

    // -------------------------------------------------------------------------
    // Facility validation
    // -------------------------------------------------------------------------

    @Test
    void execute_shouldThrowFacilityNotFound_whenFacilityDoesNotExist() {
        CreateCreditStatementCommand command = validCommand();

        when(facilityRepository.findById(creditFacilityId))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        assertEquals(
                ErrorCode.CREDIT_FACILITY_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(facilityRepository).findById(creditFacilityId);
        verifyNoInteractions(
                statementRepository,
                dailyBalanceRepository,
                amountsPort,
                minimumPaymentService
        );
    }

    @Test
    void execute_shouldThrowFacilityNotActive_whenFacilityIsClosed() {
        CreateCreditStatementCommand command = validCommand();

        when(facilityRepository.findById(creditFacilityId))
                .thenReturn(Optional.of(facility));
        when(facility.getStatus())
                .thenReturn(CreditFacilityStatus.CLOSED);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        assertEquals(
                ErrorCode.CREDIT_FACILITY_NOT_ACTIVE,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                statementRepository,
                dailyBalanceRepository,
                amountsPort,
                minimumPaymentService
        );
    }

    @Test
    void execute_shouldThrowStatementAlreadyExists_whenDuplicatePeriodExists() {
        CreateCreditStatementCommand command = validCommand();

        givenActiveFacility();
        givenNoExistingStatement(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        assertEquals(
                ErrorCode.CREDIT_STATEMENT_ALREADY_EXISTS,
                exception.getErrorCode()
        );

        verify(statementRepository)
                .existsByCreditFacilityIdAndPeriodStartAndPeriodEnd(
                        creditFacilityId,
                        periodStart,
                        periodEnd
                );
        verify(statementRepository, never())
                .findFirstByCreditFacilityIdAndPeriodEndLessThanOrderByPeriodEndDesc(
                        any(),
                        any()
                );
        verifyNoInteractions(
                dailyBalanceRepository,
                amountsPort,
                minimumPaymentService
        );
        verify(statementRepository, never()).save(any(CreditStatement.class));
    }

    // -------------------------------------------------------------------------
    // Daily balance and amount validation
    // -------------------------------------------------------------------------

    @Test
    void execute_shouldThrowDailyBalanceNotFound_whenNoDailyBalanceExists() {
        CreateCreditStatementCommand command = validCommand();

        givenActiveFacility();
        givenNoExistingStatement();
        givenNoPreviousStatement();

        when(dailyBalanceRepository
                .findFirstByCreditFacilityIdAndBusinessDateLessThanEqualOrderByBusinessDateDesc(
                        creditFacilityId,
                        periodEnd
                ))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        assertEquals(
                ErrorCode.CREDIT_DAILY_BALANCE_NOT_FOUND,
                exception.getErrorCode()
        );

        verifyNoInteractions(amountsPort, minimumPaymentService);
        verify(statementRepository, never()).save(any(CreditStatement.class));
    }

    @Test
    void execute_shouldThrowIllegalState_whenExpectedClosingBalanceDoesNotMatch() {
        CreateCreditStatementCommand command = validCommand();

        givenActiveFacility();

        when(facility.getCurrency())
                .thenReturn(Currency.VND);

        givenNoExistingStatement();
        givenNoPreviousStatement();
        givenDailyBalance(new BigDecimal("800.00"));
        givenAmounts(amounts);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> handler.execute(command)
        );

        assertEquals(
                "Expected closing balance does not match daily balance",
                exception.getMessage()
        );

        verify(amountsPort)
                .getAmounts(creditFacilityId, periodStart, periodEnd);
        verifyNoInteractions(minimumPaymentService);
        verify(statementRepository, never()).save(any(CreditStatement.class));
    }

    @Test
    void execute_shouldAcceptNumericallyEqualBalancesWithDifferentScale() {
        CreateCreditStatementCommand command = validCommand();

        CreditStatementAmounts scaledAmounts =
                new CreditStatementAmounts(
                        new BigDecimal("1000.0"),
                        new BigDecimal("300.00"),
                        new BigDecimal("50"),
                        new BigDecimal("20.000"),
                        new BigDecimal("20.000")
                );

        BigDecimal scaledClosingBalance = new BigDecimal("770.0000");

        givenActiveFacility();
        givenFacilityCurrency();
        givenNoExistingStatement();
        givenNoPreviousStatement();
        givenDailyBalance(scaledClosingBalance);
        givenAmounts(scaledAmounts);
        givenMinimumPayment(
                scaledClosingBalance,
                MINIMUM_PAYMENT
        );

        assertDoesNotThrow(() -> handler.execute(command));

        CreditStatement statement = captureSavedStatement();

        assertBigDecimalEquals(
                scaledClosingBalance,
                statement.getClosingBalance()
        );
        assertBigDecimalEquals(
                scaledAmounts.accruedInterestAmount(),
                statement.getInterestAmount()
        );
        assertBigDecimalEquals(
                scaledAmounts.postedInterestAmount(),
                statement.getPostedInterestAmount()
        );
    }

    @Test
    void execute_shouldPropagateAmountsPortException_whenAmountsLookupFails() {
        CreateCreditStatementCommand command = validCommand();

        givenActiveFacility();
        givenNoExistingStatement();
        givenNoPreviousStatement();
        givenDailyBalanceExists();

        RuntimeException expected =
                new RuntimeException("Amounts lookup failed");

        when(amountsPort.getAmounts(
                creditFacilityId,
                periodStart,
                periodEnd
        )).thenThrow(expected);

        RuntimeException actual = assertThrows(
                RuntimeException.class,
                () -> handler.execute(command)
        );

        assertSame(expected, actual);
        verifyNoInteractions(minimumPaymentService);
        verify(statementRepository, never()).save(any(CreditStatement.class));
    }

    @Test
    void execute_shouldPropagateMinimumPaymentException_whenCalculationFails() {
        CreateCreditStatementCommand command = validCommand();

        givenActiveFacility();
        givenFacilityCurrency();
        givenNoExistingStatement();
        givenNoPreviousStatement();
        givenDailyBalance(CLOSING_BALANCE);
        givenAmounts(amounts);

        RuntimeException expected =
                new RuntimeException("Minimum payment calculation failed");

        when(minimumPaymentService.calculate(
                CLOSING_BALANCE,
                Currency.VND
        )).thenThrow(expected);

        RuntimeException actual = assertThrows(
                RuntimeException.class,
                () -> handler.execute(command)
        );

        assertSame(expected, actual);
        verify(statementRepository, never()).save(any(CreditStatement.class));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private CreateCreditStatementCommand validCommand() {
        return new CreateCreditStatementCommand(
                runId,
                creditFacilityId,
                periodStart,
                periodEnd,
                statementDate,
                dueDate
        );
    }

    private void assertInvalidCommand(
            CreateCreditStatementCommand command
    ) {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                facilityRepository,
                statementRepository,
                dailyBalanceRepository,
                amountsPort,
                minimumPaymentService
        );
    }

    private void givenActiveFacility() {
        when(facilityRepository.findById(creditFacilityId))
                .thenReturn(Optional.of(facility));
        when(facility.getStatus())
                .thenReturn(CreditFacilityStatus.ACTIVE);
    }

    private void givenFacilityCurrency() {
        when(facility.getCurrency())
                .thenReturn(Currency.VND);
    }

    private void givenNoExistingStatement() {
        givenNoExistingStatement(false);
    }

    private void givenNoExistingStatement(boolean exists) {
        when(statementRepository
                .existsByCreditFacilityIdAndPeriodStartAndPeriodEnd(
                        creditFacilityId,
                        periodStart,
                        periodEnd
                ))
                .thenReturn(exists);
    }

    private void givenNoPreviousStatement() {
        when(statementRepository
                .findFirstByCreditFacilityIdAndPeriodEndLessThanOrderByPeriodEndDesc(
                        creditFacilityId,
                        periodStart
                ))
                .thenReturn(Optional.empty());
    }

    private void givenDailyBalanceExists() {
        when(dailyBalanceRepository
                .findFirstByCreditFacilityIdAndBusinessDateLessThanEqualOrderByBusinessDateDesc(
                        creditFacilityId,
                        periodEnd
                ))
                .thenReturn(Optional.of(dailyBalance));
    }

    private void givenDailyBalance(BigDecimal closingBalance) {
        givenDailyBalanceExists();
        when(dailyBalance.getClosingBalance())
                .thenReturn(closingBalance);
    }

    private void givenAmounts(CreditStatementAmounts value) {
        when(amountsPort.getAmounts(
                creditFacilityId,
                periodStart,
                periodEnd
        )).thenReturn(value);
    }

    private void givenMinimumPayment(
            BigDecimal closingBalance,
            BigDecimal minimumPayment
    ) {
        when(minimumPaymentService.calculate(
                closingBalance,
                Currency.VND
        )).thenReturn(minimumPayment);
    }

    private CreditStatement captureSavedStatement() {
        ArgumentCaptor<CreditStatement> captor =
                ArgumentCaptor.forClass(CreditStatement.class);

        verify(statementRepository).save(captor.capture());
        return captor.getValue();
    }

    private void assertBigDecimalEquals(
            BigDecimal expected,
            BigDecimal actual
    ) {
        assertNotNull(actual);
        assertEquals(
                0,
                expected.compareTo(actual),
                () -> "Expected " + expected + " but was " + actual
        );
    }
}