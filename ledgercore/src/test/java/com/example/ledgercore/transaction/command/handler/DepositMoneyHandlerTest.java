package com.example.ledgercore.transaction.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.transaction.command.dto.DepositMoneyCommand;
import com.example.ledgercore.transaction.command.port.outbound.AccountDepositPort;
import com.example.ledgercore.transaction.command.port.outbound.LedgerDepositPort;
import com.example.ledgercore.transaction.command.port.outbound.TransactionEventPort;
import com.example.ledgercore.transaction.command.repository.TransactionCommandRepository;
import com.example.ledgercore.transaction.entity.MoneyTransaction;
import com.example.ledgercore.transaction.enums.TransactionStatus;
import com.example.ledgercore.transaction.enums.TransactionType;
import com.example.ledgercore.transaction.event.DepositCompletedEvent;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepositMoneyHandlerTest {

    @Mock
    private TransactionCommandRepository transactionCommandRepository;

    @Mock
    private AccountDepositPort accountDepositPort;

    @Mock
    private LedgerDepositPort ledgerDepositPort;

    @Mock
    private TransactionEventPort transactionEventPort;

    private DepositMoneyHandler handler;

    private UUID adminUserId;
    private UUID destinationAccountId;
    private UUID transactionId;

    @BeforeEach
    void setUp() {
        handler = new DepositMoneyHandler(
                transactionCommandRepository,
                accountDepositPort,
                ledgerDepositPort,
                transactionEventPort
        );

        adminUserId = UUID.randomUUID();
        destinationAccountId = UUID.randomUUID();
        transactionId = UUID.randomUUID();
    }

    @Test
    void shouldDepositMoneySuccessfully() {
        DepositMoneyCommand command = command(
                destinationAccountId,
                "100.00",
                "VND",
                "DEP-001",
                "Cash deposit"
        );

        AccountDepositPort.DepositAccountInfo depositInfo =
                new AccountDepositPort.DepositAccountInfo(
                        destinationAccountId,
                        "VND"
                );

        when(transactionCommandRepository.findByReference("DEP-001"))
                .thenReturn(Optional.empty());

        when(accountDepositPort.getDepositInfo(destinationAccountId))
                .thenReturn(depositInfo);

        doAnswer(invocation -> {
            MoneyTransaction transaction =
                    invocation.getArgument(0);

            assertEquals(TransactionStatus.PENDING,
                    transaction.getStatus());

            assertEquals(TransactionType.DEPOSIT,
                    transaction.getType());

            transaction.setId(transactionId);

            return transaction;
        }).when(transactionCommandRepository)
                .save(any(MoneyTransaction.class));

        TransactionResponse response =
                handler.execute(adminUserId, command);

        assertNotNull(response);
        assertEquals(transactionId, response.id());
        assertEquals("DEP-001", response.reference());
        assertEquals(TransactionType.DEPOSIT, response.type());
        assertEquals(TransactionStatus.COMPLETED, response.status());
        assertEquals(destinationAccountId,
                response.destinationAccountId());
        assertEquals(new BigDecimal("100.00"),
                response.amount());
        assertEquals("VND", response.currency());
        assertEquals("Cash deposit", response.description());
        assertNotNull(response.completedAt());

        verify(transactionCommandRepository)
                .save(any(MoneyTransaction.class));

        verify(accountDepositPort)
                .deposit(destinationAccountId,
                        new BigDecimal("100.00"));

        verify(ledgerDepositPort)
                .recordDeposit(
                        transactionId,
                        destinationAccountId,
                        new BigDecimal("100.00"),
                        "VND"
                );

        verify(transactionEventPort)
                .publishDepositCompleted(any(DepositCompletedEvent.class));
    }

    @Test
    void shouldCreatePendingDepositTransactionBeforeCompletion() {
        DepositMoneyCommand command = command(
                destinationAccountId,
                "100.00",
                "VND",
                "DEP-002",
                null
        );

        when(transactionCommandRepository.findByReference("DEP-002"))
                .thenReturn(Optional.empty());

        when(accountDepositPort.getDepositInfo(destinationAccountId))
                .thenReturn(
                        new AccountDepositPort.DepositAccountInfo(
                                destinationAccountId,
                                "VND"
                        )
                );

        doAnswer(invocation -> {
            MoneyTransaction transaction =
                    invocation.getArgument(0);

            assertEquals(TransactionStatus.PENDING,
                    transaction.getStatus());

            assertEquals(TransactionType.DEPOSIT,
                    transaction.getType());

            assertEquals("DEP-002",
                    transaction.getReference());

            assertEquals(destinationAccountId,
                    transaction.getDestinationAccountId());

            assertEquals(new BigDecimal("100.00"),
                    transaction.getAmount());

            transaction.setId(transactionId);

            return transaction;
        }).when(transactionCommandRepository)
                .save(any(MoneyTransaction.class));

        handler.execute(adminUserId, command);

        verify(transactionCommandRepository)
                .save(any(MoneyTransaction.class));
    }

    @Test
    void shouldReturnExistingDepositTransaction() {
        MoneyTransaction existing = MoneyTransaction.builder()
                .id(transactionId)
                .reference("DEP-003")
                .type(TransactionType.DEPOSIT)
                .status(TransactionStatus.COMPLETED)
                .destinationAccountId(destinationAccountId)
                .amount(new BigDecimal("100"))
                .currency("VND")
                .build();

        when(transactionCommandRepository.findByReference("DEP-003"))
                .thenReturn(Optional.of(existing));

        DepositMoneyCommand command = command(
                destinationAccountId,
                "100.00",
                "VND",
                "DEP-003",
                "duplicate"
        );

        TransactionResponse response =
                handler.execute(adminUserId, command);

        assertEquals(transactionId, response.id());
        assertEquals(TransactionType.DEPOSIT, response.type());
        assertEquals(TransactionStatus.COMPLETED, response.status());

        verify(accountDepositPort, never())
                .getDepositInfo(any());

        verify(accountDepositPort, never())
                .deposit(any(), any());

        verify(ledgerDepositPort, never())
                .recordDeposit(any(), any(), any(), any());

        verify(transactionCommandRepository, never())
                .save(any());
    }

    @Test
    void shouldThrowWhenReferenceBelongsToAnotherTransactionType() {
        MoneyTransaction existing = MoneyTransaction.builder()
                .id(transactionId)
                .reference("REF-001")
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.COMPLETED)
                .build();

        when(transactionCommandRepository.findByReference("REF-001"))
                .thenReturn(Optional.of(existing));

        DepositMoneyCommand command = command(
                destinationAccountId,
                "100",
                "VND",
                "REF-001",
                null
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(adminUserId, command)
        );

        assertEquals(
                "TRANSACTION_REFERENCE_ALREADY_EXISTS",
                exception.getErrorCode().name()
        );

        verify(accountDepositPort, never())
                .deposit(any(), any());

        verify(ledgerDepositPort, never())
                .recordDeposit(any(), any(), any(), any());
    }

    @Test
    void shouldThrowWhenAmountIsZero() {
        DepositMoneyCommand command = command(
                destinationAccountId,
                "0",
                "VND",
                "DEP-004",
                null
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(adminUserId, command)
        );

        assertEquals(
                "INVALID_DEPOSIT_AMOUNT",
                exception.getErrorCode().name()
        );

        verifyNoInteractions(transactionCommandRepository);
        verifyNoInteractions(accountDepositPort);
        verifyNoInteractions(ledgerDepositPort);
        verifyNoInteractions(transactionEventPort);
    }

    @Test
    void shouldThrowWhenAmountIsNegative() {
        DepositMoneyCommand command = command(
                destinationAccountId,
                "-10",
                "VND",
                "DEP-005",
                null
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(adminUserId, command)
        );

        assertEquals(
                "INVALID_DEPOSIT_AMOUNT",
                exception.getErrorCode().name()
        );
    }

    @Test
    void shouldThrowWhenCurrencyDoesNotMatchAccount() {
        DepositMoneyCommand command = command(
                destinationAccountId,
                "100",
                "USD",
                "DEP-006",
                null
        );

        when(transactionCommandRepository.findByReference("DEP-006"))
                .thenReturn(Optional.empty());

        when(accountDepositPort.getDepositInfo(destinationAccountId))
                .thenReturn(
                        new AccountDepositPort.DepositAccountInfo(
                                destinationAccountId,
                                "VND"
                        )
                );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(adminUserId, command)
        );

        assertEquals(
                "TRANSACTION_CURRENCY_MISMATCH",
                exception.getErrorCode().name()
        );

        verify(transactionCommandRepository, never())
                .save(any());

        verify(accountDepositPort, never())
                .deposit(any(), any());

        verifyNoInteractions(ledgerDepositPort);
        verifyNoInteractions(transactionEventPort);
    }

    @Test
    void shouldPublishCompletedEventAfterDeposit() {
        DepositMoneyCommand command = command(
                destinationAccountId,
                "250",
                "VND",
                "DEP-007",
                "deposit"
        );

        when(transactionCommandRepository.findByReference("DEP-007"))
                .thenReturn(Optional.empty());

        when(accountDepositPort.getDepositInfo(destinationAccountId))
                .thenReturn(
                        new AccountDepositPort.DepositAccountInfo(
                                destinationAccountId,
                                "VND"
                        )
                );

        doAnswer(invocation -> {
            MoneyTransaction transaction =
                    invocation.getArgument(0);

            transaction.setId(transactionId);

            return transaction;
        }).when(transactionCommandRepository)
                .save(any(MoneyTransaction.class));

        handler.execute(adminUserId, command);

        ArgumentCaptor<DepositCompletedEvent> captor =
                ArgumentCaptor.forClass(
                        DepositCompletedEvent.class
                );

        verify(transactionEventPort)
                .publishDepositCompleted(captor.capture());

        DepositCompletedEvent event = captor.getValue();

        assertEquals(transactionId, event.transactionId());
        assertEquals("DEP-007", event.reference());
        assertEquals(destinationAccountId,
                event.accountId());
        assertEquals(new BigDecimal("250"),
                event.amount());
        assertEquals("VND", event.currency());
        assertNotNull(event.completedAt());
    }

    private DepositMoneyCommand command(
            UUID accountId,
            String amount,
            String currency,
            String reference,
            String description
    ) {
        return new DepositMoneyCommand(
                accountId,
                new BigDecimal(amount),
                currency,
                reference,
                description
        );
    }
}