
package com.example.ledgercore.credit.command.service.impl;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.credit.command.port.outbound.CreditBusinessDatePort;
import com.example.ledgercore.credit.command.port.outbound.CreditFacilityFeePort;
import com.example.ledgercore.credit.command.port.outbound.CreditInterestTransactionPort;
import com.example.ledgercore.credit.command.repository.CreditFacilityCommandRepository;
import com.example.ledgercore.credit.command.repository.CreditStatementCommandRepository;
import com.example.ledgercore.credit.command.service.CreditStatementFeeCalculationService;
import com.example.ledgercore.credit.entity.CreditFacility;
import com.example.ledgercore.credit.entity.CreditStatement;
import com.example.ledgercore.credit.enums.CreditStatementStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessOverdueCreditStatementHandlerTest {

    @Mock
    private CreditStatementCommandRepository creditStatementCommandRepository;

    @Mock
    private CreditFacilityCommandRepository creditFacilityCommandRepository;

    @Mock
    private CreditBusinessDatePort creditBusinessDatePort;

    @Mock
    private CreditStatementFeeCalculationService feeCalculationService;

    @Mock
    private CreditFacilityFeePort creditFacilityFeePort;

    @Mock
    private CreditInterestTransactionPort creditInterestTransactionPort;

    @Mock
    private CreditStatement statement;

    @Mock
    private CreditFacility facility;

    @InjectMocks
    private ProcessOverdueCreditStatementHandler handler;

    private UUID statementId;
    private UUID facilityId;
    private LocalDate businessDate;
    private Currency currency;
    private BigDecimal overdueFee;
    private BigDecimal interestAmount;

    @BeforeEach
    void setUp() {
        statementId = UUID.randomUUID();
        facilityId = UUID.randomUUID();
        businessDate = LocalDate.of(2026, 9, 30);
        currency = Currency.VND;
        overdueFee = new BigDecimal("50000");
        interestAmount = new BigDecimal("10000");
    }

    @Test
    void execute_shouldApplyFeeAndPostInterest_whenStatementIsIssued() {
        when(creditStatementCommandRepository.findByIdForUpdate(statementId))
                .thenReturn(Optional.of(statement));
        when(statement.getStatus()).thenReturn(CreditStatementStatus.ISSUED);
        when(statement.getCreditFacilityId()).thenReturn(facilityId);
        when(statement.getId()).thenReturn(statementId);
        when(creditFacilityCommandRepository.findById(facilityId))
                .thenReturn(Optional.of(facility));
        when(facility.getCurrency()).thenReturn(currency);
        when(creditBusinessDatePort.getCurrentBusinessDate())
                .thenReturn(businessDate);
        when(statement.isMinimumPaymentSatisfied()).thenReturn(false);
        when(feeCalculationService.calculateOverdueFee(statement, currency))
                .thenReturn(overdueFee);
        when(statement.isPaid()).thenReturn(false);
        when(statement.getInterestAmount()).thenReturn(interestAmount);

        handler.execute(statementId);

        verify(creditFacilityFeePort).createFee(
                facilityId,
                overdueFee,
                currency,
                "OVERDUE-FEE-" + statementId,
                "Credit statement overdue fee"
        );
        verify(creditInterestTransactionPort).postInterest(
                facilityId, interestAmount, currency, businessDate
        );
        verify(statement).markOverdue();
    }

    @Test
    void execute_shouldProcess_whenStatusIsPartiallyPaid() {
        when(creditStatementCommandRepository.findByIdForUpdate(statementId))
                .thenReturn(Optional.of(statement));
        when(statement.getStatus())
                .thenReturn(CreditStatementStatus.PARTIALLY_PAID);
        when(statement.getCreditFacilityId()).thenReturn(facilityId);
        when(creditFacilityCommandRepository.findById(facilityId))
                .thenReturn(Optional.of(facility));
        when(facility.getCurrency()).thenReturn(currency);
        when(creditBusinessDatePort.getCurrentBusinessDate())
                .thenReturn(businessDate);
        when(statement.isMinimumPaymentSatisfied()).thenReturn(true);
        when(statement.isPaid()).thenReturn(false);
        when(statement.getInterestAmount()).thenReturn(interestAmount);

        handler.execute(statementId);

        verify(feeCalculationService, never())
                .calculateOverdueFee(any(), any());
        verify(creditFacilityFeePort, never())
                .createFee(any(), any(), any(), any(), any());
        verify(creditInterestTransactionPort).postInterest(
                facilityId, interestAmount, currency, businessDate
        );
        verify(statement).markOverdue();
    }

    @Test
    void execute_shouldNotApplyFee_whenMinimumPaymentIsSatisfied() {
        when(creditStatementCommandRepository.findByIdForUpdate(statementId))
                .thenReturn(Optional.of(statement));
        when(statement.getStatus()).thenReturn(CreditStatementStatus.ISSUED);
        when(statement.getCreditFacilityId()).thenReturn(facilityId);
        when(creditFacilityCommandRepository.findById(facilityId))
                .thenReturn(Optional.of(facility));
        when(facility.getCurrency()).thenReturn(currency);
        when(creditBusinessDatePort.getCurrentBusinessDate())
                .thenReturn(businessDate);
        when(statement.isMinimumPaymentSatisfied()).thenReturn(true);
        when(statement.isPaid()).thenReturn(false);
        when(statement.getInterestAmount()).thenReturn(interestAmount);

        handler.execute(statementId);

        verify(feeCalculationService, never())
                .calculateOverdueFee(any(), any());
        verify(creditFacilityFeePort, never())
                .createFee(any(), any(), any(), any(), any());
        verify(creditInterestTransactionPort).postInterest(
                facilityId, interestAmount, currency, businessDate
        );
        verify(statement).markOverdue();
    }

    @Test
    void execute_shouldNotPostInterest_whenStatementIsPaid() {
        when(creditStatementCommandRepository.findByIdForUpdate(statementId))
                .thenReturn(Optional.of(statement));
        when(statement.getStatus()).thenReturn(CreditStatementStatus.ISSUED);
        when(statement.getCreditFacilityId()).thenReturn(facilityId);
        when(creditFacilityCommandRepository.findById(facilityId))
                .thenReturn(Optional.of(facility));
        when(facility.getCurrency()).thenReturn(currency);
        when(creditBusinessDatePort.getCurrentBusinessDate())
                .thenReturn(businessDate);
        when(statement.isMinimumPaymentSatisfied()).thenReturn(true);
        when(statement.isPaid()).thenReturn(true);

        handler.execute(statementId);

        verify(creditInterestTransactionPort, never())
                .postInterest(any(), any(), any(), any());
        verify(feeCalculationService, never())
                .calculateOverdueFee(any(), any());
        verify(creditFacilityFeePort, never())
                .createFee(any(), any(), any(), any(), any());
        verify(statement).markOverdue();
    }

    @Test
    void execute_shouldNotApplyFee_whenCalculatedFeeIsZero() {
        when(creditStatementCommandRepository.findByIdForUpdate(statementId))
                .thenReturn(Optional.of(statement));
        when(statement.getStatus()).thenReturn(CreditStatementStatus.ISSUED);
        when(statement.getCreditFacilityId()).thenReturn(facilityId);
        when(creditFacilityCommandRepository.findById(facilityId))
                .thenReturn(Optional.of(facility));
        when(facility.getCurrency()).thenReturn(currency);
        when(creditBusinessDatePort.getCurrentBusinessDate())
                .thenReturn(businessDate);
        when(statement.isMinimumPaymentSatisfied()).thenReturn(false);
        when(feeCalculationService.calculateOverdueFee(statement, currency))
                .thenReturn(BigDecimal.ZERO);
        when(statement.isPaid()).thenReturn(false);
        when(statement.getInterestAmount()).thenReturn(BigDecimal.ZERO);

        handler.execute(statementId);

        verify(creditFacilityFeePort, never())
                .createFee(any(), any(), any(), any(), any());
        verify(creditInterestTransactionPort, never())
                .postInterest(any(), any(), any(), any());
        verify(statement).markOverdue();
    }

    @Test
    void execute_shouldNotApplyFee_whenCalculatedFeeIsNegative() {
        when(creditStatementCommandRepository.findByIdForUpdate(statementId))
                .thenReturn(Optional.of(statement));
        when(statement.getStatus()).thenReturn(CreditStatementStatus.ISSUED);
        when(statement.getCreditFacilityId()).thenReturn(facilityId);
        when(creditFacilityCommandRepository.findById(facilityId))
                .thenReturn(Optional.of(facility));
        when(facility.getCurrency()).thenReturn(currency);
        when(creditBusinessDatePort.getCurrentBusinessDate())
                .thenReturn(businessDate);
        when(statement.isMinimumPaymentSatisfied()).thenReturn(false);
        when(feeCalculationService.calculateOverdueFee(statement, currency))
                .thenReturn(new BigDecimal("-100"));
        when(statement.isPaid()).thenReturn(false);
        when(statement.getInterestAmount()).thenReturn(BigDecimal.ZERO);

        handler.execute(statementId);

        verify(creditFacilityFeePort, never())
                .createFee(any(), any(), any(), any(), any());
        verify(creditInterestTransactionPort, never())
                .postInterest(any(), any(), any(), any());
        verify(statement).markOverdue();
    }

    @Test
    void execute_shouldNotPostInterest_whenInterestAmountIsZero() {
        when(creditStatementCommandRepository.findByIdForUpdate(statementId))
                .thenReturn(Optional.of(statement));
        when(statement.getStatus()).thenReturn(CreditStatementStatus.ISSUED);
        when(statement.getCreditFacilityId()).thenReturn(facilityId);
        when(creditFacilityCommandRepository.findById(facilityId))
                .thenReturn(Optional.of(facility));
        when(facility.getCurrency()).thenReturn(currency);
        when(creditBusinessDatePort.getCurrentBusinessDate())
                .thenReturn(businessDate);
        when(statement.isMinimumPaymentSatisfied()).thenReturn(true);
        when(statement.isPaid()).thenReturn(false);
        when(statement.getInterestAmount()).thenReturn(BigDecimal.ZERO);

        handler.execute(statementId);

        verify(creditInterestTransactionPort, never())
                .postInterest(any(), any(), any(), any());
        verify(feeCalculationService, never())
                .calculateOverdueFee(any(), any());
        verify(creditFacilityFeePort, never())
                .createFee(any(), any(), any(), any(), any());
        verify(statement).markOverdue();
    }

    @Test
    void execute_shouldNotPostInterest_whenInterestAmountIsNegative() {
        when(creditStatementCommandRepository.findByIdForUpdate(statementId))
                .thenReturn(Optional.of(statement));
        when(statement.getStatus()).thenReturn(CreditStatementStatus.ISSUED);
        when(statement.getCreditFacilityId()).thenReturn(facilityId);
        when(creditFacilityCommandRepository.findById(facilityId))
                .thenReturn(Optional.of(facility));
        when(facility.getCurrency()).thenReturn(currency);
        when(creditBusinessDatePort.getCurrentBusinessDate())
                .thenReturn(businessDate);
        when(statement.isMinimumPaymentSatisfied()).thenReturn(true);
        when(statement.isPaid()).thenReturn(false);
        when(statement.getInterestAmount()).thenReturn(new BigDecimal("-1"));

        handler.execute(statementId);

        verify(creditInterestTransactionPort, never())
                .postInterest(any(), any(), any(), any());
        verify(feeCalculationService, never())
                .calculateOverdueFee(any(), any());
        verify(creditFacilityFeePort, never())
                .createFee(any(), any(), any(), any(), any());
        verify(statement).markOverdue();
    }

    @Test
    void execute_shouldReturnWithoutProcessing_whenStatusIsNotEligible() {
        when(creditStatementCommandRepository.findByIdForUpdate(statementId))
                .thenReturn(Optional.of(statement));
        when(statement.getStatus()).thenReturn(null);

        assertDoesNotThrow(() -> handler.execute(statementId));

        verify(creditFacilityCommandRepository, never()).findById(any());
        verifyNoInteractions(
                creditBusinessDatePort,
                feeCalculationService,
                creditFacilityFeePort,
                creditInterestTransactionPort
        );
        verify(statement, never()).markOverdue();
    }

    @Test
    void execute_shouldThrowException_whenStatementDoesNotExist() {
        when(creditStatementCommandRepository.findByIdForUpdate(statementId))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> handler.execute(statementId)
        );

        assertEquals(
                "Credit statement not found: " + statementId,
                exception.getMessage()
        );

        verifyNoInteractions(
                creditFacilityCommandRepository,
                creditBusinessDatePort,
                feeCalculationService,
                creditFacilityFeePort,
                creditInterestTransactionPort
        );
    }

    @Test
    void execute_shouldThrowException_whenFacilityDoesNotExist() {
        when(creditStatementCommandRepository.findByIdForUpdate(statementId))
                .thenReturn(Optional.of(statement));
        when(statement.getStatus()).thenReturn(CreditStatementStatus.ISSUED);
        when(statement.getCreditFacilityId()).thenReturn(facilityId);
        when(creditFacilityCommandRepository.findById(facilityId))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> handler.execute(statementId)
        );

        assertEquals(
                "Credit facility not found: " + facilityId,
                exception.getMessage()
        );

        verifyNoInteractions(
                creditBusinessDatePort,
                feeCalculationService,
                creditFacilityFeePort,
                creditInterestTransactionPort
        );
        verify(statement, never()).markOverdue();
    }

    @Test
    void execute_shouldPropagateException_whenLoadingStatementFails() {
        RuntimeException expected = new RuntimeException("Database error");

        when(creditStatementCommandRepository.findByIdForUpdate(statementId))
                .thenThrow(expected);

        RuntimeException actual = assertThrows(
                RuntimeException.class,
                () -> handler.execute(statementId)
        );

        assertSame(expected, actual);

        verifyNoInteractions(
                creditFacilityCommandRepository,
                creditBusinessDatePort,
                feeCalculationService,
                creditFacilityFeePort,
                creditInterestTransactionPort
        );
    }

    @Test
    void execute_shouldPropagateException_whenLoadingFacilityFails() {
        when(creditStatementCommandRepository.findByIdForUpdate(statementId))
                .thenReturn(Optional.of(statement));
        when(statement.getStatus()).thenReturn(CreditStatementStatus.ISSUED);
        when(statement.getCreditFacilityId()).thenReturn(facilityId);

        RuntimeException expected =
                new RuntimeException("Facility repository error");

        when(creditFacilityCommandRepository.findById(facilityId))
                .thenThrow(expected);

        RuntimeException actual = assertThrows(
                RuntimeException.class,
                () -> handler.execute(statementId)
        );

        assertSame(expected, actual);

        verifyNoInteractions(
                creditBusinessDatePort,
                feeCalculationService,
                creditFacilityFeePort,
                creditInterestTransactionPort
        );
        verify(statement, never()).markOverdue();
    }

    @Test
    void execute_shouldPropagateException_whenGettingBusinessDateFails() {
        when(creditStatementCommandRepository.findByIdForUpdate(statementId))
                .thenReturn(Optional.of(statement));
        when(statement.getStatus()).thenReturn(CreditStatementStatus.ISSUED);
        when(statement.getCreditFacilityId()).thenReturn(facilityId);
        when(creditFacilityCommandRepository.findById(facilityId))
                .thenReturn(Optional.of(facility));

        RuntimeException expected =
                new RuntimeException("Business date unavailable");

        when(creditBusinessDatePort.getCurrentBusinessDate())
                .thenThrow(expected);

        RuntimeException actual = assertThrows(
                RuntimeException.class,
                () -> handler.execute(statementId)
        );

        assertSame(expected, actual);

        verify(facility, never()).getCurrency();
        verifyNoInteractions(
                feeCalculationService,
                creditFacilityFeePort,
                creditInterestTransactionPort
        );
        verify(statement, never()).markOverdue();
    }

    @Test
    void execute_shouldPropagateException_whenCalculatingOverdueFeeFails() {
        when(creditStatementCommandRepository.findByIdForUpdate(statementId))
                .thenReturn(Optional.of(statement));
        when(statement.getStatus()).thenReturn(CreditStatementStatus.ISSUED);
        when(statement.getCreditFacilityId()).thenReturn(facilityId);
        when(creditFacilityCommandRepository.findById(facilityId))
                .thenReturn(Optional.of(facility));
        when(facility.getCurrency()).thenReturn(currency);
        when(creditBusinessDatePort.getCurrentBusinessDate())
                .thenReturn(businessDate);
        when(statement.isMinimumPaymentSatisfied()).thenReturn(false);

        RuntimeException expected =
                new RuntimeException("Fee calculation failed");

        when(feeCalculationService.calculateOverdueFee(statement, currency))
                .thenThrow(expected);

        RuntimeException actual = assertThrows(
                RuntimeException.class,
                () -> handler.execute(statementId)
        );

        assertSame(expected, actual);

        verify(creditFacilityFeePort, never())
                .createFee(any(), any(), any(), any(), any());
        verify(creditInterestTransactionPort, never())
                .postInterest(any(), any(), any(), any());
        verify(statement, never()).markOverdue();
    }

    @Test
    void execute_shouldPropagateException_whenCreatingOverdueFeeFails() {
        when(creditStatementCommandRepository.findByIdForUpdate(statementId))
                .thenReturn(Optional.of(statement));
        when(statement.getStatus()).thenReturn(CreditStatementStatus.ISSUED);
        when(statement.getCreditFacilityId()).thenReturn(facilityId);
        when(statement.getId()).thenReturn(statementId);
        when(creditFacilityCommandRepository.findById(facilityId))
                .thenReturn(Optional.of(facility));
        when(facility.getCurrency()).thenReturn(currency);
        when(creditBusinessDatePort.getCurrentBusinessDate())
                .thenReturn(businessDate);
        when(statement.isMinimumPaymentSatisfied()).thenReturn(false);
        when(feeCalculationService.calculateOverdueFee(statement, currency))
                .thenReturn(overdueFee);

        RuntimeException expected = new RuntimeException("Fee creation failed");

        doThrow(expected).when(creditFacilityFeePort).createFee(
                facilityId,
                overdueFee,
                currency,
                "OVERDUE-FEE-" + statementId,
                "Credit statement overdue fee"
        );

        RuntimeException actual = assertThrows(
                RuntimeException.class,
                () -> handler.execute(statementId)
        );

        assertSame(expected, actual);

        verify(creditInterestTransactionPort, never())
                .postInterest(any(), any(), any(), any());
        verify(statement, never()).markOverdue();
    }

    @Test
    void execute_shouldPropagateException_whenPostingInterestFails() {
        when(creditStatementCommandRepository.findByIdForUpdate(statementId))
                .thenReturn(Optional.of(statement));
        when(statement.getStatus()).thenReturn(CreditStatementStatus.ISSUED);
        when(statement.getCreditFacilityId()).thenReturn(facilityId);
        when(creditFacilityCommandRepository.findById(facilityId))
                .thenReturn(Optional.of(facility));
        when(facility.getCurrency()).thenReturn(currency);
        when(creditBusinessDatePort.getCurrentBusinessDate())
                .thenReturn(businessDate);
        when(statement.isMinimumPaymentSatisfied()).thenReturn(true);
        when(statement.isPaid()).thenReturn(false);
        when(statement.getInterestAmount()).thenReturn(interestAmount);

        RuntimeException expected =
                new RuntimeException("Interest posting failed");

        doThrow(expected).when(creditInterestTransactionPort).postInterest(
                facilityId,
                interestAmount,
                currency,
                businessDate
        );

        RuntimeException actual = assertThrows(
                RuntimeException.class,
                () -> handler.execute(statementId)
        );

        assertSame(expected, actual);

        verify(feeCalculationService, never())
                .calculateOverdueFee(any(), any());
        verify(creditFacilityFeePort, never())
                .createFee(any(), any(), any(), any(), any());
        verify(statement, never()).markOverdue();
    }
}