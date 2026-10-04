
package com.example.ledgercore.transaction.command.handler;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.transaction.command.dto.CreateCreditFacilityFeeCommand;
import com.example.ledgercore.transaction.command.port.outbound.CreditFacilityBalancePort;
import com.example.ledgercore.transaction.command.port.outbound.CreditFeeLedgerPort;
import com.example.ledgercore.transaction.command.port.outbound.TransactionBusinessDayPort;
import com.example.ledgercore.transaction.command.repository.TransactionCommandRepository;
import com.example.ledgercore.transaction.entity.MoneyTransaction;
import com.example.ledgercore.transaction.enums.TransactionStatus;
import com.example.ledgercore.transaction.enums.TransactionType;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateCreditFacilityFeeHandlerTest {

    @Mock
    private TransactionCommandRepository transactionCommandRepository;

    @Mock
    private CreditFacilityBalancePort creditFacilityBalancePort;

    @Mock
    private CreditFeeLedgerPort creditFeeLedgerPort;

    @Mock
    private TransactionBusinessDayPort transactionBusinessDayPort;

    @InjectMocks
    private CreateCreditFacilityFeeHandler handler;

    private UUID creditFacilityId;
    private LocalDate businessDate;
    private CreateCreditFacilityFeeCommand command;

    private static final BigDecimal AMOUNT =
            new BigDecimal("15000");

    private static final String REFERENCE = "FEE-2026-0001";
    private static final String DESCRIPTION = "Monthly credit facility fee";

    @BeforeEach
    void setUp() {
        creditFacilityId = UUID.randomUUID();
        businessDate = LocalDate.of(2026, 9, 30);

        command = new CreateCreditFacilityFeeCommand(
                creditFacilityId,
                AMOUNT,
                Currency.VND,
                REFERENCE,
                DESCRIPTION
        );
    }

    @Test
    void execute_shouldCreateCompletedFeeTransaction_whenCommandIsValid() {
        when(transactionBusinessDayPort.getCurrentBusinessDate())
                .thenReturn(businessDate);

        TransactionResponse response = handler.execute(command);

        ArgumentCaptor<MoneyTransaction> transactionCaptor =
                ArgumentCaptor.forClass(MoneyTransaction.class);

        verify(transactionCommandRepository)
                .save(transactionCaptor.capture());

        MoneyTransaction savedTransaction =
                transactionCaptor.getValue();

        assertEquals(REFERENCE, savedTransaction.getReference());
        assertEquals(TransactionType.FEE, savedTransaction.getType());
        assertEquals(TransactionStatus.COMPLETED, savedTransaction.getStatus());
        assertEquals(businessDate, savedTransaction.getBusinessDate());
        assertEquals(
                creditFacilityId,
                savedTransaction.getSourceCreditFacilityId()
        );
        assertEquals(0, AMOUNT.compareTo(savedTransaction.getAmount()));
        assertEquals(Currency.VND, savedTransaction.getCurrency());
        assertEquals(DESCRIPTION, savedTransaction.getDescription());
        assertNotNull(savedTransaction.getCompletedAt());

        verify(creditFacilityBalancePort)
                .increaseOutstandingBalance(
                        creditFacilityId,
                        AMOUNT,
                        Currency.VND,
                        businessDate
                );

        verify(creditFeeLedgerPort)
                .recordCreditFee(
                        savedTransaction.getId(),
                        creditFacilityId,
                        AMOUNT,
                        Currency.VND,
                        businessDate
                );

        assertNotNull(response);
        assertEquals(REFERENCE, response.reference());
        assertEquals(TransactionType.FEE, response.type());
        assertEquals(TransactionStatus.COMPLETED, response.status());
        assertEquals(0, AMOUNT.compareTo(response.amount()));
        assertEquals(Currency.VND, response.currency());
        assertEquals(DESCRIPTION, response.description());
        assertNotNull(response.completedAt());
    }

    @Test
    void execute_shouldUseCurrentBusinessDate_whenCreatingTransaction() {
        LocalDate expectedBusinessDate =
                LocalDate.of(2026, 10, 1);

        when(transactionBusinessDayPort.getCurrentBusinessDate())
                .thenReturn(expectedBusinessDate);

        handler.execute(command);

        ArgumentCaptor<MoneyTransaction> transactionCaptor =
                ArgumentCaptor.forClass(MoneyTransaction.class);

        verify(transactionCommandRepository)
                .save(transactionCaptor.capture());

        assertEquals(
                expectedBusinessDate,
                transactionCaptor.getValue().getBusinessDate()
        );

        verify(creditFacilityBalancePort)
                .increaseOutstandingBalance(
                        creditFacilityId,
                        AMOUNT,
                        Currency.VND,
                        expectedBusinessDate
                );

        verify(creditFeeLedgerPort)
                .recordCreditFee(
                        transactionCaptor.getValue().getId(),
                        creditFacilityId,
                        AMOUNT,
                        Currency.VND,
                        expectedBusinessDate
                );
    }

    @Test
    void execute_shouldReturnResponseWithNullSourceAndDestinationAccounts() {
        when(transactionBusinessDayPort.getCurrentBusinessDate())
                .thenReturn(businessDate);

        TransactionResponse response = handler.execute(command);

        assertNull(response.sourceAccountId());
        assertNull(response.destinationAccountId());
    }

    @Test
    void execute_shouldThrowInvalidRequest_whenCommandIsNull() {
        assertInvalidRequest(null);
    }

    @Test
    void execute_shouldThrowInvalidRequest_whenCreditFacilityIdIsNull() {
        CreateCreditFacilityFeeCommand invalidCommand =
                new CreateCreditFacilityFeeCommand(
                        null,
                        AMOUNT,
                        Currency.VND,
                        REFERENCE,
                        DESCRIPTION
                );

        assertInvalidRequest(invalidCommand);
    }

    @Test
    void execute_shouldThrowInvalidRequest_whenAmountIsNull() {
        CreateCreditFacilityFeeCommand invalidCommand =
                new CreateCreditFacilityFeeCommand(
                        creditFacilityId,
                        null,
                        Currency.VND,
                        REFERENCE,
                        DESCRIPTION
                );

        assertInvalidRequest(invalidCommand);
    }

    @Test
    void execute_shouldThrowInvalidRequest_whenCurrencyIsNull() {
        CreateCreditFacilityFeeCommand invalidCommand =
                new CreateCreditFacilityFeeCommand(
                        creditFacilityId,
                        AMOUNT,
                        null,
                        REFERENCE,
                        DESCRIPTION
                );

        assertInvalidRequest(invalidCommand);
    }

    @Test
    void execute_shouldThrowInvalidRequest_whenReferenceIsNull() {
        CreateCreditFacilityFeeCommand invalidCommand =
                new CreateCreditFacilityFeeCommand(
                        creditFacilityId,
                        AMOUNT,
                        Currency.VND,
                        null,
                        DESCRIPTION
                );

        assertInvalidRequest(invalidCommand);
    }

    @Test
    void execute_shouldThrowInvalidRequest_whenReferenceIsBlank() {
        CreateCreditFacilityFeeCommand invalidCommand =
                new CreateCreditFacilityFeeCommand(
                        creditFacilityId,
                        AMOUNT,
                        Currency.VND,
                        "   ",
                        DESCRIPTION
                );

        assertInvalidRequest(invalidCommand);
    }

    @Test
    void execute_shouldThrowInvalidAmount_whenAmountIsZero() {
        CreateCreditFacilityFeeCommand invalidCommand =
                new CreateCreditFacilityFeeCommand(
                        creditFacilityId,
                        BigDecimal.ZERO,
                        Currency.VND,
                        REFERENCE,
                        DESCRIPTION
                );

        assertInvalidAmount(invalidCommand);
    }

    @Test
    void execute_shouldThrowInvalidAmount_whenAmountIsNegative() {
        CreateCreditFacilityFeeCommand invalidCommand =
                new CreateCreditFacilityFeeCommand(
                        creditFacilityId,
                        new BigDecimal("-100"),
                        Currency.VND,
                        REFERENCE,
                        DESCRIPTION
                );

        assertInvalidAmount(invalidCommand);
    }

    @Test
    void execute_shouldNotCallDependencies_whenCommandIsInvalid() {
        CreateCreditFacilityFeeCommand invalidCommand =
                new CreateCreditFacilityFeeCommand(
                        null,
                        AMOUNT,
                        Currency.VND,
                        REFERENCE,
                        DESCRIPTION
                );

        assertThrows(
                BusinessException.class,
                () -> handler.execute(invalidCommand)
        );

        verifyNoInteractions(
                transactionCommandRepository,
                creditFacilityBalancePort,
                creditFeeLedgerPort,
                transactionBusinessDayPort
        );
    }

    @Test
    void execute_shouldPropagateException_whenGettingBusinessDateFails() {
        RuntimeException exception =
                new RuntimeException("Business date service failed");

        when(transactionBusinessDayPort.getCurrentBusinessDate())
                .thenThrow(exception);

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> handler.execute(command)
        );

        assertSame(exception, thrown);

        verify(transactionBusinessDayPort)
                .getCurrentBusinessDate();

        verifyNoInteractions(
                transactionCommandRepository,
                creditFacilityBalancePort,
                creditFeeLedgerPort
        );
    }

    @Test
    void execute_shouldPropagateException_whenSavingTransactionFails() {
        RuntimeException exception =
                new RuntimeException("Transaction save failed");

        when(transactionBusinessDayPort.getCurrentBusinessDate())
                .thenReturn(businessDate);

        when(transactionCommandRepository.save(any(MoneyTransaction.class)))
                .thenThrow(exception);

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> handler.execute(command)
        );

        assertSame(exception, thrown);

        verify(transactionCommandRepository)
                .save(any(MoneyTransaction.class));

        verifyNoInteractions(
                creditFacilityBalancePort,
                creditFeeLedgerPort
        );
    }

    @Test
    void execute_shouldPropagateException_whenIncreasingOutstandingBalanceFails() {
        RuntimeException exception =
                new RuntimeException("Balance update failed");

        when(transactionBusinessDayPort.getCurrentBusinessDate())
                .thenReturn(businessDate);

        when(transactionCommandRepository.save(any(MoneyTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        doThrow(exception)
                .when(creditFacilityBalancePort)
                .increaseOutstandingBalance(
                        creditFacilityId,
                        AMOUNT,
                        Currency.VND,
                        businessDate
                );

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> handler.execute(command)
        );

        assertSame(exception, thrown);

        verify(transactionCommandRepository)
                .save(any(MoneyTransaction.class));

        verify(creditFacilityBalancePort)
                .increaseOutstandingBalance(
                        creditFacilityId,
                        AMOUNT,
                        Currency.VND,
                        businessDate
                );

        verifyNoInteractions(creditFeeLedgerPort);
    }

    @Test
    void execute_shouldPropagateException_whenRecordingCreditFeeFails() {
        RuntimeException exception =
                new RuntimeException("Ledger recording failed");

        when(transactionBusinessDayPort.getCurrentBusinessDate())
                .thenReturn(businessDate);

        when(transactionCommandRepository.save(any(MoneyTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        doThrow(exception)
                .when(creditFeeLedgerPort)
                .recordCreditFee(
                        any(),
                        eq(creditFacilityId),
                        eq(AMOUNT),
                        eq(Currency.VND),
                        eq(businessDate)
                );

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> handler.execute(command)
        );

        assertSame(exception, thrown);

        verify(transactionCommandRepository)
                .save(any(MoneyTransaction.class));

        verify(creditFacilityBalancePort)
                .increaseOutstandingBalance(
                        creditFacilityId,
                        AMOUNT,
                        Currency.VND,
                        businessDate
                );

        verify(creditFeeLedgerPort)
                .recordCreditFee(
                        any(),
                        eq(creditFacilityId),
                        eq(AMOUNT),
                        eq(Currency.VND),
                        eq(businessDate)
                );
    }

    private void assertInvalidRequest(
            CreateCreditFacilityFeeCommand invalidCommand
    ) {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(invalidCommand)
        );

        assertEquals(ErrorCode.INVALID_REQUEST, exception.getErrorCode());

        verifyNoInteractions(
                transactionCommandRepository,
                creditFacilityBalancePort,
                creditFeeLedgerPort,
                transactionBusinessDayPort
        );
    }

    private void assertInvalidAmount(
            CreateCreditFacilityFeeCommand invalidCommand
    ) {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(invalidCommand)
        );

        assertEquals(ErrorCode.INVALID_AMOUNT, exception.getErrorCode());

        verifyNoInteractions(
                transactionCommandRepository,
                creditFacilityBalancePort,
                creditFeeLedgerPort,
                transactionBusinessDayPort
        );
    }
}