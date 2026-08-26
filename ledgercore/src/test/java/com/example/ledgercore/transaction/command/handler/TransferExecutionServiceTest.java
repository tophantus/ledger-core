package com.example.ledgercore.transaction.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.transaction.command.dto.TransferMoneyCommand;
import com.example.ledgercore.transaction.command.port.outbound.AccountTransferPort;
import com.example.ledgercore.transaction.command.port.outbound.LedgerTransferPort;
import com.example.ledgercore.transaction.command.port.outbound.TransactionEventPort;
import com.example.ledgercore.transaction.command.repository.TransactionCommandRepository;
import com.example.ledgercore.transaction.entity.MoneyTransaction;
import com.example.ledgercore.transaction.enums.TransactionStatus;
import com.example.ledgercore.transaction.enums.TransactionType;
import com.example.ledgercore.transaction.event.TransferCompletedEvent;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferExecutionServiceTest {

    @Mock
    private TransactionCommandRepository transactionCommandRepository;

    @Mock
    private AccountTransferPort accountTransferPort;

    @Mock
    private LedgerTransferPort ledgerTransferPort;

    @Mock
    private TransactionEventPort transactionEventPort;

    private TransferExecutionService service;

    private UUID userId;
    private UUID sourceAccountId;
    private UUID destinationAccountId;
    private UUID transactionId;

    @BeforeEach
    void setUp() {
        service = new TransferExecutionService(
                transactionCommandRepository,
                accountTransferPort,
                ledgerTransferPort,
                transactionEventPort
        );

        userId = UUID.randomUUID();
        sourceAccountId = UUID.randomUUID();
        destinationAccountId = UUID.randomUUID();
        transactionId = UUID.randomUUID();
    }

    @Test
    void shouldExecuteTransferSuccessfully() {
        TransferMoneyCommand command = command(
                "100",
                "VND",
                "TRF-001",
                "Transfer"
        );

        when(accountTransferPort.getTransferInfo(
                userId,
                sourceAccountId,
                destinationAccountId
        )).thenReturn(
                new AccountTransferPort.TransferAccountInfo(
                        sourceAccountId,
                        destinationAccountId,
                        "VND",
                        new BigDecimal("1000")
                )
        );

        doAnswer(invocation -> {
            MoneyTransaction transaction =
                    invocation.getArgument(0);

            assertEquals(TransactionStatus.PENDING,
                    transaction.getStatus());

            assertEquals(TransactionType.TRANSFER,
                    transaction.getType());

            transaction.setId(transactionId);

            return transaction;
        }).when(transactionCommandRepository)
                .save(any(MoneyTransaction.class));

        TransactionResponse response =
                service.execute(
                        userId,
                        command,
                        destinationAccountId
                );

        assertNotNull(response);
        assertEquals(transactionId, response.id());
        assertEquals(TransactionType.TRANSFER,
                response.type());
        assertEquals(TransactionStatus.COMPLETED,
                response.status());
        assertEquals(sourceAccountId,
                response.sourceAccountId());
        assertEquals(destinationAccountId,
                response.destinationAccountId());
        assertEquals(new BigDecimal("100"),
                response.amount());
        assertEquals("VND", response.currency());
        assertNotNull(response.completedAt());

        verify(accountTransferPort)
                .transfer(
                        sourceAccountId,
                        destinationAccountId,
                        new BigDecimal("100")
                );

        verify(ledgerTransferPort)
                .recordTransfer(
                        transactionId,
                        sourceAccountId,
                        destinationAccountId,
                        new BigDecimal("100"),
                        "VND"
                );

        verify(transactionEventPort)
                .publishTransferCompleted(
                        any(TransferCompletedEvent.class)
                );
    }

    @Test
    void shouldCreatePendingTransferTransactionBeforeCompletion() {
        TransferMoneyCommand command = command(
                "100",
                "VND",
                "TRF-002",
                null
        );

        when(accountTransferPort.getTransferInfo(
                userId,
                sourceAccountId,
                destinationAccountId
        )).thenReturn(
                new AccountTransferPort.TransferAccountInfo(
                        sourceAccountId,
                        destinationAccountId,
                        "VND",
                        new BigDecimal("1000")
                )
        );

        doAnswer(invocation -> {
            MoneyTransaction transaction =
                    invocation.getArgument(0);

            assertEquals(TransactionStatus.PENDING,
                    transaction.getStatus());

            assertEquals(TransactionType.TRANSFER,
                    transaction.getType());

            assertEquals(sourceAccountId,
                    transaction.getSourceAccountId());

            assertEquals(destinationAccountId,
                    transaction.getDestinationAccountId());

            assertEquals(new BigDecimal("100"),
                    transaction.getAmount());

            transaction.setId(transactionId);

            return transaction;
        }).when(transactionCommandRepository)
                .save(any(MoneyTransaction.class));

        service.execute(
                userId,
                command,
                destinationAccountId
        );

        verify(transactionCommandRepository)
                .save(any(MoneyTransaction.class));
    }

    @Test
    void shouldThrowWhenCurrencyDoesNotMatch() {
        TransferMoneyCommand command = command(
                "100",
                "USD",
                "TRF-003",
                null
        );

        when(accountTransferPort.getTransferInfo(
                userId,
                sourceAccountId,
                destinationAccountId
        )).thenReturn(
                new AccountTransferPort.TransferAccountInfo(
                        sourceAccountId,
                        destinationAccountId,
                        "VND",
                        new BigDecimal("1000")
                )
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.execute(
                        userId,
                        command,
                        destinationAccountId
                )
        );

        assertEquals(
                "TRANSACTION_CURRENCY_MISMATCH",
                exception.getErrorCode().name()
        );

        verify(transactionCommandRepository, never())
                .save(any());

        verify(accountTransferPort, never())
                .transfer(any(), any(), any());

        verifyNoInteractions(ledgerTransferPort);
        verifyNoInteractions(transactionEventPort);
    }

    @Test
    void shouldThrowWhenBalanceIsInsufficient() {
        TransferMoneyCommand command = command(
                "1000",
                "VND",
                "TRF-004",
                null
        );

        when(accountTransferPort.getTransferInfo(
                userId,
                sourceAccountId,
                destinationAccountId
        )).thenReturn(
                new AccountTransferPort.TransferAccountInfo(
                        sourceAccountId,
                        destinationAccountId,
                        "VND",
                        new BigDecimal("500")
                )
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.execute(
                        userId,
                        command,
                        destinationAccountId
                )
        );

        assertEquals(
                "ACCOUNT_INSUFFICIENT_BALANCE",
                exception.getErrorCode().name()
        );

        verify(transactionCommandRepository, never())
                .save(any());

        verify(accountTransferPort, never())
                .transfer(any(), any(), any());

        verifyNoInteractions(ledgerTransferPort);
        verifyNoInteractions(transactionEventPort);
    }

    @Test
    void shouldPublishCompletedEvent() {
        TransferMoneyCommand command = command(
                "100",
                "VND",
                "TRF-006",
                "transfer"
        );

        when(accountTransferPort.getTransferInfo(
                userId,
                sourceAccountId,
                destinationAccountId
        )).thenReturn(
                new AccountTransferPort.TransferAccountInfo(
                        sourceAccountId,
                        destinationAccountId,
                        "VND",
                        new BigDecimal("1000")
                )
        );

        doAnswer(invocation -> {
            MoneyTransaction transaction =
                    invocation.getArgument(0);

            transaction.setId(transactionId);

            return transaction;
        }).when(transactionCommandRepository)
                .save(any(MoneyTransaction.class));

        service.execute(
                userId,
                command,
                destinationAccountId
        );

        ArgumentCaptor<TransferCompletedEvent> captor =
                ArgumentCaptor.forClass(
                        TransferCompletedEvent.class
                );

        verify(transactionEventPort)
                .publishTransferCompleted(
                        captor.capture()
                );

        TransferCompletedEvent event =
                captor.getValue();

        assertEquals(transactionId,
                event.transactionId());

        assertEquals("TRF-006",
                event.reference());

        assertEquals(sourceAccountId,
                event.sourceAccountId());

        assertEquals(destinationAccountId,
                event.destinationAccountId());

        assertEquals(new BigDecimal("100"),
                event.amount());

        assertEquals("VND",
                event.currency());

        assertNotNull(event.completedAt());
    }

    private TransferMoneyCommand command(
            String amount,
            String currency,
            String reference,
            String description
    ) {
        return new TransferMoneyCommand(
                sourceAccountId,
                "DEST-001",
                new BigDecimal(amount),
                currency,
                reference,
                description
        );
    }
}