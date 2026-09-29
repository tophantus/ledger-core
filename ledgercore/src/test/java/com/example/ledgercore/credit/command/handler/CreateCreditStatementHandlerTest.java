package com.example.ledgercore.credit.command.handler;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.credit.command.dto.CreateCreditStatementCommand;
import com.example.ledgercore.credit.command.port.outbound.CreditStatementAmountsPort;
import com.example.ledgercore.credit.command.port.outbound.dto.CreditStatementAmounts;
import com.example.ledgercore.credit.command.repository.CreditDailyBalanceCommandRepository;
import com.example.ledgercore.credit.command.repository.CreditFacilityCommandRepository;
import com.example.ledgercore.credit.command.repository.CreditStatementCommandRepository;
import com.example.ledgercore.credit.command.service.CreditMinimumPaymentService;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateCreditStatementHandlerTest {

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

    @InjectMocks
    private CreateCreditStatementHandler handler;

    private UUID creditFacilityId;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private LocalDate statementDate;
    private LocalDate dueDate;

    private CreditFacility facility;
    private CreditDailyBalance dailyBalance;
    private CreditStatementAmounts amounts;

    @BeforeEach
    void setUp() {
        creditFacilityId = UUID.randomUUID();

        periodStart = LocalDate.of(2026, 9, 1);
        periodEnd = LocalDate.of(2026, 9, 30);
        statementDate = LocalDate.of(2026, 9, 30);
        dueDate = LocalDate.of(2026, 10, 20);

        facility = mock(CreditFacility.class);
        dailyBalance = mock(CreditDailyBalance.class);

        amounts = new CreditStatementAmounts(
                new BigDecimal("1000.00"), // purchases
                new BigDecimal("300.00"),  // payments
                new BigDecimal("50.00"),   // fees
                new BigDecimal("20.00")    // interest
        );
    }

    @Test
    void execute_shouldCreateStatement_whenCommandIsValid() {
        CreateCreditStatementCommand command = validCommand();

        when(facilityRepository.findById(creditFacilityId))
                .thenReturn(Optional.of(facility));

        when(facility.getStatus())
                .thenReturn(CreditFacilityStatus.ACTIVE);

        when(facility.getCurrency())
                .thenReturn(Currency.VND);

        when(statementRepository
                .existsByCreditFacilityIdAndPeriodStartAndPeriodEnd(
                        creditFacilityId,
                        periodStart,
                        periodEnd
                ))
                .thenReturn(false);

        when(statementRepository
                .findFirstByCreditFacilityIdAndPeriodEndLessThanOrderByPeriodEndDesc(
                        creditFacilityId,
                        periodStart
                ))
                .thenReturn(Optional.empty());

        when(dailyBalanceRepository
                .findFirstByCreditFacilityIdAndBusinessDateLessThanEqualOrderByBusinessDateDesc(
                        creditFacilityId,
                        periodEnd
                ))
                .thenReturn(Optional.of(dailyBalance));

        when(dailyBalance.getClosingBalance())
                .thenReturn(new BigDecimal("770.00"));

        when(amountsPort.getAmounts(
                creditFacilityId,
                periodStart,
                periodEnd
        )).thenReturn(amounts);

        when(minimumPaymentService.calculate(
                new BigDecimal("770.00"),
                Currency.VND
        )).thenReturn(new BigDecimal("50.00"));

        handler.execute(command);

        ArgumentCaptor<CreditStatement> captor =
                ArgumentCaptor.forClass(CreditStatement.class);

        verify(statementRepository).save(captor.capture());

        CreditStatement statement = captor.getValue();

        assertEquals(creditFacilityId, statement.getCreditFacilityId());
        assertEquals(periodStart, statement.getPeriodStart());
        assertEquals(periodEnd, statement.getPeriodEnd());
        assertEquals(statementDate, statement.getStatementDate());
        assertEquals(dueDate, statement.getDueDate());

        assertEquals(
                new BigDecimal("0"),
                statement.getOpeningBalance()
        );

        assertEquals(
                new BigDecimal("1000.00"),
                statement.getPurchasesAmount()
        );

        assertEquals(
                new BigDecimal("300.00"),
                statement.getPaymentsAmount()
        );

        assertEquals(
                new BigDecimal("50.00"),
                statement.getFeesAmount()
        );

        assertEquals(
                new BigDecimal("20.00"),
                statement.getInterestAmount()
        );

        assertEquals(
                new BigDecimal("770.00"),
                statement.getClosingBalance()
        );

        assertEquals(
                new BigDecimal("50.00"),
                statement.getMinimumPayment()
        );

        assertEquals(
                BigDecimal.ZERO,
                statement.getPaidAmount()
        );

        assertEquals(
                CreditStatementStatus.OPEN,
                statement.getStatus()
        );
    }

    @Test
    void execute_shouldUsePreviousStatementClosingBalanceAsOpeningBalance() {
        CreateCreditStatementCommand command = validCommand();

        CreditStatement previousStatement = mock(CreditStatement.class);

        when(facilityRepository.findById(creditFacilityId))
                .thenReturn(Optional.of(facility));
        when(facility.getStatus())
                .thenReturn(CreditFacilityStatus.ACTIVE);
        when(facility.getCurrency())
                .thenReturn(Currency.VND);

        when(statementRepository
                .existsByCreditFacilityIdAndPeriodStartAndPeriodEnd(
                        creditFacilityId, periodStart, periodEnd
                ))
                .thenReturn(false);

        when(statementRepository
                .findFirstByCreditFacilityIdAndPeriodEndLessThanOrderByPeriodEndDesc(
                        creditFacilityId, periodStart
                ))
                .thenReturn(Optional.of(previousStatement));

        when(previousStatement.getClosingBalance())
                .thenReturn(new BigDecimal("500.00"));

        mockValidDailyBalance(new BigDecimal("1270.00"));

        when(amountsPort.getAmounts(
                creditFacilityId, periodStart, periodEnd
        )).thenReturn(amounts);

        when(minimumPaymentService.calculate(
                new BigDecimal("1270.00"), Currency.VND
        )).thenReturn(new BigDecimal("63.50"));

        handler.execute(command);

        ArgumentCaptor<CreditStatement> captor =
                ArgumentCaptor.forClass(CreditStatement.class);

        verify(statementRepository).save(captor.capture());

        assertEquals(
                new BigDecimal("500.00"),
                captor.getValue().getOpeningBalance()
        );
    }

    @Test
    void execute_shouldUseZeroOpeningBalance_whenPreviousStatementDoesNotExist() {
        CreateCreditStatementCommand command = validCommand();

        mockValidFacility();
        mockFacilityCurrency();
        mockNoDuplicateStatement();

        when(statementRepository
                .findFirstByCreditFacilityIdAndPeriodEndLessThanOrderByPeriodEndDesc(
                        creditFacilityId, periodStart
                ))
                .thenReturn(Optional.empty());

        mockValidDailyBalance(new BigDecimal("770.00"));
        mockValidAmounts();
        mockMinimumPayment(new BigDecimal("770.00"));

        handler.execute(command);

        ArgumentCaptor<CreditStatement> captor =
                ArgumentCaptor.forClass(CreditStatement.class);

        verify(statementRepository).save(captor.capture());

        assertEquals(
                BigDecimal.ZERO,
                captor.getValue().getOpeningBalance()
        );
    }

    @Test
    void execute_shouldThrow_whenCommandIsNull() {
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
    void execute_shouldThrow_whenCommandHasMissingField() {
        CreateCreditStatementCommand command =
                new CreateCreditStatementCommand(
                        creditFacilityId,
                        null,
                        periodEnd,
                        statementDate,
                        dueDate
                );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
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
    void execute_shouldThrow_whenDatesAreInvalid() {
        CreateCreditStatementCommand command =
                new CreateCreditStatementCommand(
                        creditFacilityId,
                        periodEnd,
                        periodStart,
                        statementDate,
                        dueDate
                );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
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
    void execute_shouldThrow_whenFacilityDoesNotExist() {
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

        verify(statementRepository, never())
                .existsByCreditFacilityIdAndPeriodStartAndPeriodEnd(
                        any(), any(), any()
                );

        verifyNoInteractions(
                dailyBalanceRepository,
                amountsPort,
                minimumPaymentService
        );
    }

    @Test
    void execute_shouldThrow_whenFacilityIsNotActive() {
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

        verify(statementRepository, never())
                .existsByCreditFacilityIdAndPeriodStartAndPeriodEnd(
                        any(), any(), any()
                );

        verifyNoInteractions(
                dailyBalanceRepository,
                amountsPort,
                minimumPaymentService
        );
    }

    @Test
    void execute_shouldThrow_whenStatementAlreadyExists() {
        CreateCreditStatementCommand command = validCommand();

        mockValidFacility();

        when(statementRepository
                .existsByCreditFacilityIdAndPeriodStartAndPeriodEnd(
                        creditFacilityId,
                        periodStart,
                        periodEnd
                ))
                .thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        assertEquals(
                ErrorCode.CREDIT_STATEMENT_ALREADY_EXISTS,
                exception.getErrorCode()
        );

        verify(statementRepository, never())
                .save(any(CreditStatement.class));

        verifyNoInteractions(
                dailyBalanceRepository,
                amountsPort,
                minimumPaymentService
        );
    }

    @Test
    void execute_shouldThrow_whenDailyBalanceDoesNotExist() {
        CreateCreditStatementCommand command = validCommand();

        mockValidFacility();
        mockNoDuplicateStatement();

        when(statementRepository
                .findFirstByCreditFacilityIdAndPeriodEndLessThanOrderByPeriodEndDesc(
                        creditFacilityId, periodStart
                ))
                .thenReturn(Optional.empty());

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

        verify(statementRepository, never())
                .save(any(CreditStatement.class));
    }

    @Test
    void execute_shouldThrow_whenClosingBalanceDoesNotMatch() {
        CreateCreditStatementCommand command = validCommand();

        mockValidFacility();
        mockNoDuplicateStatement();

        when(statementRepository
                .findFirstByCreditFacilityIdAndPeriodEndLessThanOrderByPeriodEndDesc(
                        creditFacilityId, periodStart
                ))
                .thenReturn(Optional.empty());

        mockValidDailyBalance(new BigDecimal("800.00"));

        mockValidAmounts();

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> handler.execute(command)
        );

        assertEquals(
                "Expected closing balance does not match daily balance",
                exception.getMessage()
        );

        verify(minimumPaymentService, never())
                .calculate(any(), any());

        verify(statementRepository, never())
                .save(any(CreditStatement.class));
    }

    @Test
    void execute_shouldPropagateException_whenAmountsPortFails() {
        CreateCreditStatementCommand command = validCommand();

        mockValidFacility();
        mockNoDuplicateStatement();

        when(statementRepository
                .findFirstByCreditFacilityIdAndPeriodEndLessThanOrderByPeriodEndDesc(
                        creditFacilityId, periodStart
                ))
                .thenReturn(Optional.empty());

        // Không stub getClosingBalance() vì amountsPort fail trước khi value này được dùng.
        when(dailyBalanceRepository
                .findFirstByCreditFacilityIdAndBusinessDateLessThanEqualOrderByBusinessDateDesc(
                        creditFacilityId,
                        periodEnd
                ))
                .thenReturn(Optional.of(dailyBalance));

        RuntimeException expectedException =
                new RuntimeException("Amounts query failed");

        when(amountsPort.getAmounts(
                creditFacilityId,
                periodStart,
                periodEnd
        )).thenThrow(expectedException);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> handler.execute(command)
        );

        assertSame(expectedException, exception);

        verify(statementRepository, never())
                .save(any(CreditStatement.class));

        verifyNoInteractions(minimumPaymentService);
    }

    @Test
    void execute_shouldPropagateException_whenMinimumPaymentCalculationFails() {
        CreateCreditStatementCommand command = validCommand();

        mockValidFacility();
        mockFacilityCurrency();
        mockNoDuplicateStatement();

        when(statementRepository
                .findFirstByCreditFacilityIdAndPeriodEndLessThanOrderByPeriodEndDesc(
                        creditFacilityId, periodStart
                ))
                .thenReturn(Optional.empty());

        mockValidDailyBalance(new BigDecimal("770.00"));
        mockValidAmounts();

        RuntimeException expectedException =
                new RuntimeException("Minimum payment calculation failed");

        when(minimumPaymentService.calculate(
                new BigDecimal("770.00"),
                Currency.VND
        )).thenThrow(expectedException);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> handler.execute(command)
        );

        assertSame(expectedException, exception);

        verify(statementRepository, never())
                .save(any(CreditStatement.class));
    }

    @Test
    void execute_shouldNotSave_whenClosingBalanceMismatch() {
        CreateCreditStatementCommand command = validCommand();

        mockValidFacility();
        mockNoDuplicateStatement();

        when(statementRepository
                .findFirstByCreditFacilityIdAndPeriodEndLessThanOrderByPeriodEndDesc(
                        creditFacilityId, periodStart
                ))
                .thenReturn(Optional.empty());

        mockValidDailyBalance(new BigDecimal("700.00"));
        mockValidAmounts();

        assertThrows(
                IllegalStateException.class,
                () -> handler.execute(command)
        );

        verify(statementRepository, never())
                .save(any(CreditStatement.class));
    }

    private CreateCreditStatementCommand validCommand() {
        return new CreateCreditStatementCommand(
                creditFacilityId,
                periodStart,
                periodEnd,
                statementDate,
                dueDate
        );
    }

    private void mockValidFacility() {
        when(facilityRepository.findById(creditFacilityId))
                .thenReturn(Optional.of(facility));

        when(facility.getStatus())
                .thenReturn(CreditFacilityStatus.ACTIVE);

    }

    private void mockFacilityCurrency() {
        when(facility.getCurrency())
                .thenReturn(Currency.VND);
    }

    private void mockNoDuplicateStatement() {
        when(statementRepository
                .existsByCreditFacilityIdAndPeriodStartAndPeriodEnd(
                        creditFacilityId,
                        periodStart,
                        periodEnd
                ))
                .thenReturn(false);
    }

    private void mockValidDailyBalance(BigDecimal closingBalance) {
        when(dailyBalanceRepository
                .findFirstByCreditFacilityIdAndBusinessDateLessThanEqualOrderByBusinessDateDesc(
                        creditFacilityId,
                        periodEnd
                ))
                .thenReturn(Optional.of(dailyBalance));

        when(dailyBalance.getClosingBalance())
                .thenReturn(closingBalance);
    }

    private void mockValidAmounts() {
        when(amountsPort.getAmounts(
                creditFacilityId,
                periodStart,
                periodEnd
        )).thenReturn(amounts);
    }

    private void mockMinimumPayment(BigDecimal closingBalance) {
        when(minimumPaymentService.calculate(
                closingBalance,
                Currency.VND
        )).thenReturn(new BigDecimal("50.00"));
    }
}
