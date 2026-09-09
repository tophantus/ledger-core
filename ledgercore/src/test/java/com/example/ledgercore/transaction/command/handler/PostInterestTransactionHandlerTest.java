package com.example.ledgercore.transaction.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.transaction.command.dto.PostInterestTransactionCommand;
import com.example.ledgercore.transaction.command.port.outbound.AccountDepositPort;
import com.example.ledgercore.transaction.command.port.outbound.InterestLedgerPort;
import com.example.ledgercore.transaction.command.repository.TransactionCommandRepository;
import com.example.ledgercore.transaction.entity.MoneyTransaction;
import com.example.ledgercore.transaction.enums.TransactionStatus;
import com.example.ledgercore.transaction.enums.TransactionType;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostInterestTransactionHandlerTest {

    @Mock
    private TransactionCommandRepository
            transactionCommandRepository;

    @Mock
    private AccountDepositPort
            accountDepositPort;

    @Mock
    private InterestLedgerPort
            interestLedgerPort;

    private PostInterestTransactionHandler handler;

    private UUID accountId;
    private UUID transactionId;

    private static final LocalDate BUSINESS_DATE =
            LocalDate.of(2026, 8, 27);

    @BeforeEach
    void setUp() {
        handler = new PostInterestTransactionHandler(
                transactionCommandRepository,
                accountDepositPort,
                interestLedgerPort
        );

        accountId = UUID.randomUUID();
        transactionId = UUID.randomUUID();
    }

    @Test
    void shouldPostInterestSuccessfully() {

        PostInterestTransactionCommand command =
                command(
                        "100",
                        "VND"
                );

        mockDepositInfo("VND");
        mockSaveTransaction();

        TransactionResponse response =
                handler.execute(command);

        assertNotNull(response);

        assertEquals(
                transactionId,
                response.id()
        );

        assertEquals(
                "INTEREST-" + accountId + "-" + BUSINESS_DATE,
                response.reference()
        );

        assertEquals(
                TransactionType.INTEREST,
                response.type()
        );

        assertEquals(
                TransactionStatus.COMPLETED,
                response.status()
        );

        assertEquals(
                accountId,
                response.destinationAccountId()
        );

        assertNull(
                response.sourceAccountId()
        );

        assertEquals(
                new BigDecimal("100"),
                response.amount()
        );

        assertEquals(
                "VND",
                response.currency()
        );

        assertEquals(
                "Interest posting",
                response.description()
        );

        assertNotNull(
                response.completedAt()
        );

        ArgumentCaptor<MoneyTransaction> captor =
                ArgumentCaptor.forClass(
                        MoneyTransaction.class
                );

        verify(transactionCommandRepository)
                .save(captor.capture());

        MoneyTransaction transaction =
                captor.getValue();

        assertEquals(
                "INTEREST-" + accountId + "-" + BUSINESS_DATE,
                transaction.getReference()
        );

        assertEquals(
                TransactionType.INTEREST,
                transaction.getType()
        );

        assertEquals(
                TransactionStatus.COMPLETED,
                transaction.getStatus()
        );

        assertEquals(
                BUSINESS_DATE,
                transaction.getBusinessDate()
        );

        assertEquals(
                accountId,
                transaction.getDestinationAccountId()
        );

        assertEquals(
                new BigDecimal("100"),
                transaction.getAmount()
        );

        assertEquals(
                "VND",
                transaction.getCurrency()
        );

        assertEquals(
                "Interest posting",
                transaction.getDescription()
        );

        verify(accountDepositPort)
                .deposit(
                        accountId,
                        new BigDecimal("100"),
                        BUSINESS_DATE
                );

        verify(interestLedgerPort)
                .recordInterestPosting(
                        transactionId,
                        accountId,
                        new BigDecimal("100"),
                        "VND",
                        BUSINESS_DATE
                );
    }

    @Test
    void shouldCreatePendingTransactionBeforeCompletion() {

        PostInterestTransactionCommand command =
                command(
                        "100",
                        "VND"
                );

        mockDepositInfo("VND");

        doAnswer(invocation -> {

            MoneyTransaction transaction =
                    invocation.getArgument(0);

            assertEquals(
                    TransactionStatus.PENDING,
                    transaction.getStatus()
            );

            assertEquals(
                    TransactionType.INTEREST,
                    transaction.getType()
            );

            assertEquals(
                    "INTEREST-" + accountId + "-" + BUSINESS_DATE,
                    transaction.getReference()
            );

            assertEquals(
                    BUSINESS_DATE,
                    transaction.getBusinessDate()
            );

            assertEquals(
                    accountId,
                    transaction.getDestinationAccountId()
            );

            assertEquals(
                    new BigDecimal("100"),
                    transaction.getAmount()
            );

            assertEquals(
                    "VND",
                    transaction.getCurrency()
            );

            transaction.setId(transactionId);

            return transaction;

        }).when(transactionCommandRepository)
                .save(any(MoneyTransaction.class));

        handler.execute(command);

        verify(transactionCommandRepository)
                .save(any(MoneyTransaction.class));

        verify(accountDepositPort)
                .deposit(
                        accountId,
                        new BigDecimal("100"),
                        BUSINESS_DATE
                );

        verify(interestLedgerPort)
                .recordInterestPosting(
                        transactionId,
                        accountId,
                        new BigDecimal("100"),
                        "VND",
                        BUSINESS_DATE
                );
    }

    @Test
    void shouldUseCorrectInterestReference() {

        PostInterestTransactionCommand command =
                command(
                        "250.5000",
                        "VND"
                );

        mockDepositInfo("VND");
        mockSaveTransaction();

        handler.execute(command);

        ArgumentCaptor<MoneyTransaction> captor =
                ArgumentCaptor.forClass(
                        MoneyTransaction.class
                );

        verify(transactionCommandRepository)
                .save(captor.capture());

        assertEquals(
                "INTEREST-" + accountId + "-" + BUSINESS_DATE,
                captor.getValue().getReference()
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
                transactionCommandRepository,
                accountDepositPort,
                interestLedgerPort
        );
    }

    @Test
    void shouldThrowWhenAccountIdIsNull() {

        PostInterestTransactionCommand command =
                new PostInterestTransactionCommand(
                        null,
                        new BigDecimal("100"),
                        "VND",
                        BUSINESS_DATE
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
                transactionCommandRepository,
                accountDepositPort,
                interestLedgerPort
        );
    }

    @Test
    void shouldThrowWhenCurrencyIsNull() {

        PostInterestTransactionCommand command =
                new PostInterestTransactionCommand(
                        accountId,
                        new BigDecimal("100"),
                        null,
                        BUSINESS_DATE
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
                transactionCommandRepository,
                accountDepositPort,
                interestLedgerPort
        );
    }

    @Test
    void shouldThrowWhenCurrencyIsBlank() {

        PostInterestTransactionCommand command =
                new PostInterestTransactionCommand(
                        accountId,
                        new BigDecimal("100"),
                        " ",
                        BUSINESS_DATE
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
                transactionCommandRepository,
                accountDepositPort,
                interestLedgerPort
        );
    }

    @Test
    void shouldThrowWhenBusinessDateIsNull() {

        PostInterestTransactionCommand command =
                new PostInterestTransactionCommand(
                        accountId,
                        new BigDecimal("100"),
                        "VND",
                        null
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
                transactionCommandRepository,
                accountDepositPort,
                interestLedgerPort
        );
    }

    @Test
    void shouldThrowWhenAmountIsNull() {

        PostInterestTransactionCommand command =
                new PostInterestTransactionCommand(
                        accountId,
                        null,
                        "VND",
                        BUSINESS_DATE
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.INVALID_TRANSFER_AMOUNT,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                transactionCommandRepository,
                accountDepositPort,
                interestLedgerPort
        );
    }

    @Test
    void shouldThrowWhenAmountIsZero() {

        PostInterestTransactionCommand command =
                command(
                        "0",
                        "VND"
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.INVALID_TRANSFER_AMOUNT,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                transactionCommandRepository,
                accountDepositPort,
                interestLedgerPort
        );
    }

    @Test
    void shouldThrowWhenAmountIsNegative() {

        PostInterestTransactionCommand command =
                command(
                        "-100",
                        "VND"
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.INVALID_TRANSFER_AMOUNT,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                transactionCommandRepository,
                accountDepositPort,
                interestLedgerPort
        );
    }

    @Test
    void shouldThrowWhenCurrencyDoesNotMatch() {

        PostInterestTransactionCommand command =
                command(
                        "100",
                        "USD"
                );

        mockDepositInfo("VND");

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.TRANSACTION_CURRENCY_MISMATCH,
                exception.getErrorCode()
        );

        verify(accountDepositPort)
                .getDepositInfo(accountId);

        verify(transactionCommandRepository, never())
                .save(any());

        verify(accountDepositPort, never())
                .deposit(any(), any(), any());

        verifyNoInteractions(
                interestLedgerPort
        );
    }

    @Test
    void shouldUseAccountCurrencyForValidation() {

        PostInterestTransactionCommand command =
                command(
                        "100",
                        "USD"
                );

        mockDepositInfo("VND");

        assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        verify(accountDepositPort)
                .getDepositInfo(accountId);

        verify(transactionCommandRepository, never())
                .save(any());

        verify(accountDepositPort, never())
                .deposit(any(), any(), any());

        verifyNoInteractions(
                interestLedgerPort
        );
    }

    @Test
    void shouldDepositBeforeRecordingLedger() {

        PostInterestTransactionCommand command =
                command(
                        "100",
                        "VND"
                );

        mockDepositInfo("VND");
        mockSaveTransaction();

        var inOrder = inOrder(
                transactionCommandRepository,
                accountDepositPort,
                interestLedgerPort
        );

        handler.execute(command);

        inOrder.verify(transactionCommandRepository)
                .save(any(MoneyTransaction.class));

        inOrder.verify(accountDepositPort)
                .deposit(
                        accountId,
                        new BigDecimal("100"),
                        BUSINESS_DATE
                );

        inOrder.verify(interestLedgerPort)
                .recordInterestPosting(
                        transactionId,
                        accountId,
                        new BigDecimal("100"),
                        "VND",
                        BUSINESS_DATE
                );
    }

    private void mockDepositInfo(
            String currency
    ) {
        when(accountDepositPort.getDepositInfo(
                accountId
        )).thenReturn(
                new AccountDepositPort.DepositAccountInfo(
                        accountId,
                        currency
                )
        );
    }

    private void mockSaveTransaction() {

        doAnswer(invocation -> {

            MoneyTransaction transaction =
                    invocation.getArgument(0);

            transaction.setId(transactionId);

            return transaction;

        }).when(transactionCommandRepository)
                .save(any(MoneyTransaction.class));
    }

    private PostInterestTransactionCommand command(
            String amount,
            String currency
    ) {
        return new PostInterestTransactionCommand(
                accountId,
                new BigDecimal(amount),
                currency,
                BUSINESS_DATE
        );
    }
}