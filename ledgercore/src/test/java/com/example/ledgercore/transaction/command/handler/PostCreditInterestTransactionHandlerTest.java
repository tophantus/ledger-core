
package com.example.ledgercore.transaction.command.handler;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.transaction.command.dto.PostCreditInterestTransactionCommand;
import com.example.ledgercore.transaction.command.port.outbound.CreditFacilityBalancePort;
import com.example.ledgercore.transaction.command.port.outbound.CreditInterestLedgerPort;
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
class PostCreditInterestTransactionHandlerTest {

    @Mock
    private TransactionCommandRepository transactionCommandRepository;

    @Mock
    private CreditFacilityBalancePort creditFacilityBalancePort;

    @Mock
    private CreditInterestLedgerPort creditInterestLedgerPort;

    @InjectMocks
    private PostCreditInterestTransactionHandler handler;

    private UUID creditFacilityId;
    private LocalDate businessDate;
    private BigDecimal amount;
    private PostCreditInterestTransactionCommand command;

    private static final String DESCRIPTION = "Credit interest posting";

    @BeforeEach
    void setUp() {
        creditFacilityId = UUID.randomUUID();
        businessDate = LocalDate.of(2026, 9, 30);
        amount = new BigDecimal("15000");

        command = new PostCreditInterestTransactionCommand(
                creditFacilityId,
                amount,
                Currency.VND,
                businessDate
        );
    }

    @Test
    void execute_shouldCreateCompletedInterestTransaction_whenCommandIsValid() {
        when(transactionCommandRepository.save(any(MoneyTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponse response = handler.execute(command);

        ArgumentCaptor<MoneyTransaction> transactionCaptor =
                ArgumentCaptor.forClass(MoneyTransaction.class);

        verify(transactionCommandRepository)
                .save(transactionCaptor.capture());

        MoneyTransaction transaction = transactionCaptor.getValue();

        String expectedReference = buildExpectedReference();

        assertEquals(expectedReference, transaction.getReference());
        assertEquals(TransactionType.INTEREST, transaction.getType());
        assertEquals(TransactionStatus.COMPLETED, transaction.getStatus());
        assertEquals(businessDate, transaction.getBusinessDate());
        assertEquals(
                creditFacilityId,
                transaction.getSourceCreditFacilityId()
        );
        assertBigDecimalEquals(amount, transaction.getAmount());
        assertEquals(Currency.VND, transaction.getCurrency());
        assertEquals(DESCRIPTION, transaction.getDescription());
        assertNotNull(transaction.getCompletedAt());

        verify(creditFacilityBalancePort)
                .increaseOutstandingBalance(
                        creditFacilityId,
                        amount,
                        Currency.VND,
                        businessDate
                );

        verify(creditInterestLedgerPort)
                .recordInterestPosting(
                        transaction.getId(),
                        creditFacilityId,
                        amount,
                        Currency.VND,
                        businessDate
                );

        assertNotNull(response);
        assertEquals(transaction.getId(), response.id());
        assertEquals(expectedReference, response.reference());
        assertEquals(TransactionType.INTEREST, response.type());
        assertEquals(TransactionStatus.COMPLETED, response.status());
        assertBigDecimalEquals(amount, response.amount());
        assertEquals(Currency.VND, response.currency());
        assertEquals(DESCRIPTION, response.description());
        assertNotNull(response.completedAt());
    }

    @Test
    void execute_shouldBuildReferenceFromFacilityIdAndBusinessDate() {
        when(transactionCommandRepository.save(any(MoneyTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        handler.execute(command);

        ArgumentCaptor<MoneyTransaction> transactionCaptor =
                ArgumentCaptor.forClass(MoneyTransaction.class);

        verify(transactionCommandRepository)
                .save(transactionCaptor.capture());

        assertEquals(
                "CREDIT-INTEREST-" + creditFacilityId + "-" + businessDate,
                transactionCaptor.getValue().getReference()
        );
    }

    @Test
    void execute_shouldUseCommandCurrency_whenUpdatingBalanceAndLedger() {
        PostCreditInterestTransactionCommand usdCommand =
                new PostCreditInterestTransactionCommand(
                        creditFacilityId,
                        amount,
                        Currency.USD,
                        businessDate
                );

        when(transactionCommandRepository.save(any(MoneyTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        handler.execute(usdCommand);

        ArgumentCaptor<MoneyTransaction> transactionCaptor =
                ArgumentCaptor.forClass(MoneyTransaction.class);

        verify(transactionCommandRepository)
                .save(transactionCaptor.capture());

        MoneyTransaction transaction = transactionCaptor.getValue();

        assertEquals(Currency.USD, transaction.getCurrency());

        verify(creditFacilityBalancePort)
                .increaseOutstandingBalance(
                        creditFacilityId,
                        amount,
                        Currency.USD,
                        businessDate
                );

        verify(creditInterestLedgerPort)
                .recordInterestPosting(
                        transaction.getId(),
                        creditFacilityId,
                        amount,
                        Currency.USD,
                        businessDate
                );
    }

    @Test
    void execute_shouldReturnResponseWithNullAccountIds() {
        when(transactionCommandRepository.save(any(MoneyTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

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
        assertInvalidRequest(
                new PostCreditInterestTransactionCommand(
                        null,
                        amount,
                        Currency.VND,
                        businessDate
                )
        );
    }

    @Test
    void execute_shouldThrowInvalidRequest_whenCurrencyIsNull() {
        assertInvalidRequest(
                new PostCreditInterestTransactionCommand(
                        creditFacilityId,
                        amount,
                        null,
                        businessDate
                )
        );
    }

    @Test
    void execute_shouldThrowInvalidRequest_whenBusinessDateIsNull() {
        assertInvalidRequest(
                new PostCreditInterestTransactionCommand(
                        creditFacilityId,
                        amount,
                        Currency.VND,
                        null
                )
        );
    }

    @Test
    void execute_shouldThrowInvalidTransferAmount_whenAmountIsNull() {
        assertInvalidTransferAmount(
                new PostCreditInterestTransactionCommand(
                        creditFacilityId,
                        null,
                        Currency.VND,
                        businessDate
                )
        );
    }

    @Test
    void execute_shouldThrowInvalidTransferAmount_whenAmountIsZero() {
        assertInvalidTransferAmount(
                new PostCreditInterestTransactionCommand(
                        creditFacilityId,
                        BigDecimal.ZERO,
                        Currency.VND,
                        businessDate
                )
        );
    }

    @Test
    void execute_shouldThrowInvalidTransferAmount_whenAmountIsNegative() {
        assertInvalidTransferAmount(
                new PostCreditInterestTransactionCommand(
                        creditFacilityId,
                        new BigDecimal("-100"),
                        Currency.VND,
                        businessDate
                )
        );
    }

    @Test
    void execute_shouldNotCallDependencies_whenCommandIsInvalid() {
        PostCreditInterestTransactionCommand invalidCommand =
                new PostCreditInterestTransactionCommand(
                        null,
                        amount,
                        Currency.VND,
                        businessDate
                );

        assertThrows(
                BusinessException.class,
                () -> handler.execute(invalidCommand)
        );

        verifyNoInteractions(
                transactionCommandRepository,
                creditFacilityBalancePort,
                creditInterestLedgerPort
        );
    }

    @Test
    void execute_shouldPropagateException_whenSavingTransactionFails() {
        RuntimeException exception =
                new RuntimeException("Transaction save failed");

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
                creditInterestLedgerPort
        );
    }

    @Test
    void execute_shouldPropagateException_whenIncreasingOutstandingBalanceFails() {
        RuntimeException exception =
                new RuntimeException("Outstanding balance update failed");

        when(transactionCommandRepository.save(any(MoneyTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        doThrow(exception)
                .when(creditFacilityBalancePort)
                .increaseOutstandingBalance(
                        creditFacilityId,
                        amount,
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
                        amount,
                        Currency.VND,
                        businessDate
                );

        verifyNoInteractions(creditInterestLedgerPort);
    }

    @Test
    void execute_shouldPropagateException_whenRecordingInterestPostingFails() {
        RuntimeException exception =
                new RuntimeException("Interest ledger posting failed");

        when(transactionCommandRepository.save(any(MoneyTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        doThrow(exception)
                .when(creditInterestLedgerPort)
                .recordInterestPosting(
                        any(),
                        eq(creditFacilityId),
                        eq(amount),
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
                        amount,
                        Currency.VND,
                        businessDate
                );

        verify(creditInterestLedgerPort)
                .recordInterestPosting(
                        any(),
                        eq(creditFacilityId),
                        eq(amount),
                        eq(Currency.VND),
                        eq(businessDate)
                );
    }

    private void assertInvalidRequest(
            PostCreditInterestTransactionCommand invalidCommand
    ) {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(invalidCommand)
        );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                transactionCommandRepository,
                creditFacilityBalancePort,
                creditInterestLedgerPort
        );
    }

    private void assertInvalidTransferAmount(
            PostCreditInterestTransactionCommand invalidCommand
    ) {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(invalidCommand)
        );

        assertEquals(
                ErrorCode.INVALID_TRANSFER_AMOUNT,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                transactionCommandRepository,
                creditFacilityBalancePort,
                creditInterestLedgerPort
        );
    }

    private String buildExpectedReference() {
        return "CREDIT-INTEREST-"
                + creditFacilityId
                + "-"
                + businessDate;
    }

    private void assertBigDecimalEquals(
            BigDecimal expected,
            BigDecimal actual
    ) {
        assertNotNull(actual);
        assertEquals(0, expected.compareTo(actual));
    }
}