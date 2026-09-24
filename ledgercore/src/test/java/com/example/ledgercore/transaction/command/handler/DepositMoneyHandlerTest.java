package com.example.ledgercore.transaction.command.handler;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.transaction.command.dto.DepositMoneyCommand;
import com.example.ledgercore.transaction.command.port.outbound.DepositLedgerPort;
import com.example.ledgercore.transaction.command.port.outbound.DepositUserAccountPort;
import com.example.ledgercore.transaction.command.port.outbound.TransactionBusinessDayPort;
import com.example.ledgercore.transaction.command.port.outbound.TransactionEventPort;
import com.example.ledgercore.transaction.command.repository.TransactionCommandRepository;
import com.example.ledgercore.transaction.entity.MoneyTransaction;
import com.example.ledgercore.transaction.enums.TransactionStatus;
import com.example.ledgercore.transaction.enums.TransactionType;
import com.example.ledgercore.transaction.event.AccountBalanceChangedEvent;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
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
class DepositMoneyHandlerTest {

    @Mock
    private TransactionCommandRepository transactionCommandRepository;

    @Mock
    private DepositUserAccountPort depositUserAccountPort;

    @Mock
    private DepositLedgerPort depositLedgerPort;

    @Mock
    private TransactionEventPort transactionEventPort;

    @Mock
    private TransactionBusinessDayPort transactionBusinessDayPort;

    private DepositMoneyHandler handler;

    private UUID adminUserId;
    private UUID destinationAccountId;
    private UUID transactionId;

    private static final LocalDate BUSINESS_DATE =
            LocalDate.of(2026, 8, 27);

    @BeforeEach
    void setUp() {
        handler = new DepositMoneyHandler(
                transactionCommandRepository,
                depositUserAccountPort,
                depositLedgerPort,
                transactionEventPort,
                transactionBusinessDayPort
        );

        adminUserId = UUID.randomUUID();
        destinationAccountId = UUID.randomUUID();
        transactionId = UUID.randomUUID();
    }

    @Test
    void shouldDepositMoneySuccessfully() {

        DepositMoneyCommand command =
                command(
                        destinationAccountId,
                        "100",
                        Currency.VND,
                        "DEP-001",
                        "Cash deposit"
                );

        mockValidDeposit(command);
        mockSaveTransaction();

        TransactionResponse response =
                handler.execute(
                        adminUserId,
                        command
                );

        assertNotNull(response);

        assertEquals(
                transactionId,
                response.id()
        );

        assertEquals(
                "DEP-001",
                response.reference()
        );

        assertEquals(
                TransactionType.DEPOSIT,
                response.type()
        );

        assertEquals(
                TransactionStatus.COMPLETED,
                response.status()
        );

        assertEquals(
                destinationAccountId,
                response.destinationAccountId()
        );

        assertEquals(
                new BigDecimal("100"),
                response.amount()
        );

        assertEquals(
                Currency.VND,
                response.currency()
        );

        assertEquals(
                "Cash deposit",
                response.description()
        );

        assertNotNull(response.completedAt());

        verify(transactionCommandRepository)
                .save(any(MoneyTransaction.class));

        verify(depositUserAccountPort)
                .deposit(
                        destinationAccountId,
                        new BigDecimal("100"),
                        BUSINESS_DATE
                );

        verify(depositLedgerPort)
                .recordDeposit(
                        transactionId,
                        destinationAccountId,
                        new BigDecimal("100"),
                        Currency.VND,
                        BUSINESS_DATE
                );

        verify(transactionEventPort)
                .publishAccountBalanceChanged(
                        any(AccountBalanceChangedEvent.class)
                );

        verify(transactionBusinessDayPort)
                .getCurrentBusinessDate();
    }

    @Test
    void shouldCreatePendingDepositTransactionBeforeCompletion() {

        DepositMoneyCommand command =
                command(
                        destinationAccountId,
                        "100",
                        Currency.VND,
                        "DEP-002",
                        null
                );

        when(transactionCommandRepository.findByReference(
                "DEP-002"
        )).thenReturn(Optional.empty());

        when(depositUserAccountPort.getDepositInfo(
                destinationAccountId
        )).thenReturn(
                new DepositUserAccountPort.DepositAccountInfo(
                        destinationAccountId,
                        Currency.VND
                )
        );

        when(transactionBusinessDayPort.getCurrentBusinessDate())
                .thenReturn(BUSINESS_DATE);

        doAnswer(invocation -> {

            MoneyTransaction transaction =
                    invocation.getArgument(0);

            assertEquals(
                    TransactionStatus.PENDING,
                    transaction.getStatus()
            );

            assertEquals(
                    TransactionType.DEPOSIT,
                    transaction.getType()
            );

            assertEquals(
                    "DEP-002",
                    transaction.getReference()
            );

            assertEquals(
                    destinationAccountId,
                    transaction.getDestinationAccountId()
            );

            assertEquals(
                    new BigDecimal("100"),
                    transaction.getAmount()
            );

            assertEquals(
                    Currency.VND,
                    transaction.getCurrency()
            );

            assertNull(
                    transaction.getDescription()
            );

            assertEquals(
                    BUSINESS_DATE,
                    transaction.getBusinessDate()
            );

            transaction.setId(transactionId);

            return transaction;

        }).when(transactionCommandRepository)
                .save(any(MoneyTransaction.class));

        handler.execute(
                adminUserId,
                command
        );

        verify(transactionCommandRepository)
                .save(any(MoneyTransaction.class));
    }

    @Test
    void shouldReturnExistingDepositTransaction() {

        MoneyTransaction existing =
                MoneyTransaction.builder()
                        .id(transactionId)
                        .reference("DEP-003")
                        .type(TransactionType.DEPOSIT)
                        .status(TransactionStatus.COMPLETED)
                        .destinationAccountId(
                                destinationAccountId
                        )
                        .amount(
                                new BigDecimal("100")
                        )
                        .currency(Currency.VND)
                        .build();

        when(transactionCommandRepository.findByReference(
                "DEP-003"
        )).thenReturn(Optional.of(existing));

        DepositMoneyCommand command =
                command(
                        destinationAccountId,
                        "100",
                        Currency.VND,
                        "DEP-003",
                        "duplicate"
                );

        TransactionResponse response =
                handler.execute(
                        adminUserId,
                        command
                );

        assertNotNull(response);

        assertEquals(
                transactionId,
                response.id()
        );

        assertEquals(
                "DEP-003",
                response.reference()
        );

        assertEquals(
                TransactionType.DEPOSIT,
                response.type()
        );

        assertEquals(
                TransactionStatus.COMPLETED,
                response.status()
        );

        verify(transactionCommandRepository)
                .findByReference("DEP-003");

        verify(transactionCommandRepository, never())
                .save(any());

        verify(depositUserAccountPort, never())
                .getDepositInfo(any());

        verify(depositUserAccountPort, never())
                .deposit(any(), any(), any());

        verify(depositLedgerPort, never())
                .recordDeposit(
                        any(),
                        any(),
                        any(),
                        any(),
                        any()
                );

        verifyNoInteractions(
                transactionBusinessDayPort,
                transactionEventPort
        );
    }

    @Test
    void shouldThrowWhenReferenceBelongsToAnotherTransactionType() {

        MoneyTransaction existing =
                MoneyTransaction.builder()
                        .id(transactionId)
                        .reference("REF-001")
                        .type(TransactionType.TRANSFER)
                        .status(TransactionStatus.COMPLETED)
                        .build();

        when(transactionCommandRepository.findByReference(
                "REF-001"
        )).thenReturn(Optional.of(existing));

        DepositMoneyCommand command =
                command(
                        destinationAccountId,
                        "100",
                        Currency.VND,
                        "REF-001",
                        null
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                adminUserId,
                                command
                        )
                );

        assertEquals(
                ErrorCode.TRANSACTION_REFERENCE_ALREADY_EXISTS,
                exception.getErrorCode()
        );

        verify(transactionCommandRepository)
                .findByReference("REF-001");

        verify(transactionCommandRepository, never())
                .save(any());

        verifyNoInteractions(
                depositUserAccountPort,
                depositLedgerPort,
                transactionBusinessDayPort,
                transactionEventPort
        );
    }

    @Test
    void shouldThrowWhenAmountIsZero() {

        DepositMoneyCommand command =
                command(
                        destinationAccountId,
                        "0",
                        Currency.VND,
                        "DEP-004",
                        null
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                adminUserId,
                                command
                        )
                );

        assertEquals(
                ErrorCode.INVALID_DEPOSIT_AMOUNT,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                transactionCommandRepository,
                depositUserAccountPort,
                depositLedgerPort,
                transactionEventPort,
                transactionBusinessDayPort
        );
    }

    @Test
    void shouldThrowWhenAmountIsNegative() {

        DepositMoneyCommand command =
                command(
                        destinationAccountId,
                        "-10",
                        Currency.VND,
                        "DEP-005",
                        null
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                adminUserId,
                                command
                        )
                );

        assertEquals(
                ErrorCode.INVALID_DEPOSIT_AMOUNT,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                transactionCommandRepository,
                depositUserAccountPort,
                depositLedgerPort,
                transactionEventPort,
                transactionBusinessDayPort
        );
    }

    @Test
    void shouldThrowWhenCurrencyAmountScaleIsInvalid() {

        DepositMoneyCommand command =
                command(
                        destinationAccountId,
                        "100.1",
                        Currency.VND,
                        "DEP-006",
                        null
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                adminUserId,
                                command
                        )
                );

        assertEquals(
                ErrorCode.INVALID_CURRENCY_AMOUNT,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                transactionCommandRepository,
                depositUserAccountPort,
                depositLedgerPort,
                transactionEventPort,
                transactionBusinessDayPort
        );
    }

    @Test
    void shouldThrowWhenCurrencyDoesNotMatchAccount() {

        DepositMoneyCommand command =
                command(
                        destinationAccountId,
                        "100",
                        Currency.USD,
                        "DEP-007",
                        null
                );

        when(transactionCommandRepository.findByReference(
                "DEP-007"
        )).thenReturn(Optional.empty());

        when(depositUserAccountPort.getDepositInfo(
                destinationAccountId
        )).thenReturn(
                new DepositUserAccountPort.DepositAccountInfo(
                        destinationAccountId,
                        Currency.VND
                )
        );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                adminUserId,
                                command
                        )
                );

        assertEquals(
                ErrorCode.TRANSACTION_CURRENCY_MISMATCH,
                exception.getErrorCode()
        );

        verify(transactionCommandRepository)
                .findByReference("DEP-007");

        verify(depositUserAccountPort)
                .getDepositInfo(destinationAccountId);

        verify(transactionCommandRepository, never())
                .save(any());

        verify(depositUserAccountPort, never())
                .deposit(any(), any(), any());

        verifyNoInteractions(
                depositLedgerPort,
                transactionBusinessDayPort,
                transactionEventPort
        );
    }

    @Test
    void shouldUseCurrentBusinessDateForTransactionAndLedger() {

        DepositMoneyCommand command =
                command(
                        destinationAccountId,
                        "250",
                        Currency.VND,
                        "DEP-008",
                        "deposit"
                );

        mockValidDeposit(command);
        mockSaveTransaction();

        handler.execute(
                adminUserId,
                command
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
                BUSINESS_DATE,
                transaction.getBusinessDate()
        );

        verify(depositUserAccountPort)
                .deposit(
                        destinationAccountId,
                        new BigDecimal("250"),
                        BUSINESS_DATE
                );

        verify(depositLedgerPort)
                .recordDeposit(
                        transactionId,
                        destinationAccountId,
                        new BigDecimal("250"),
                        Currency.VND,
                        BUSINESS_DATE
                );
    }

    @Test
    void shouldCreateDepositTransactionWithCorrectFields() {

        DepositMoneyCommand command =
                command(
                        destinationAccountId,
                        "500",
                        Currency.VND,
                        "DEP-009",
                        "Initial cash deposit"
                );

        mockValidDeposit(command);
        mockSaveTransaction();

        handler.execute(
                adminUserId,
                command
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
                transactionId,
                transaction.getId()
        );

        assertEquals(
                "DEP-009",
                transaction.getReference()
        );

        assertEquals(
                TransactionType.DEPOSIT,
                transaction.getType()
        );

        assertEquals(
                TransactionStatus.COMPLETED,
                transaction.getStatus()
        );

        assertEquals(
                destinationAccountId,
                transaction.getDestinationAccountId()
        );

        assertEquals(
                new BigDecimal("500"),
                transaction.getAmount()
        );

        assertEquals(
                Currency.VND,
                transaction.getCurrency()
        );

        assertEquals(
                "Initial cash deposit",
                transaction.getDescription()
        );

        assertEquals(
                BUSINESS_DATE,
                transaction.getBusinessDate()
        );
    }

    @Test
    void shouldPublishAccountBalanceChangedEventAfterDeposit() {

        DepositMoneyCommand command =
                command(
                        destinationAccountId,
                        "250",
                        Currency.VND,
                        "DEP-010",
                        "deposit"
                );

        mockValidDeposit(command);
        mockSaveTransaction();

        handler.execute(
                adminUserId,
                command
        );

        ArgumentCaptor<AccountBalanceChangedEvent> captor =
                ArgumentCaptor.forClass(
                        AccountBalanceChangedEvent.class
                );

        verify(transactionEventPort)
                .publishAccountBalanceChanged(
                        captor.capture()
                );

        AccountBalanceChangedEvent event =
                captor.getValue();

        assertEquals(
                transactionId,
                event.transactionId()
        );

        assertEquals(
                destinationAccountId,
                event.accountId()
        );

        assertEquals(
                new BigDecimal("250"),
                event.balanceDelta()
        );

        assertEquals(
                Currency.VND,
                event.currency()
        );

        assertNotNull(
                event.changedAt()
        );
    }

    @Test
    void shouldCompleteTransactionBeforePublishingEvent() {

        DepositMoneyCommand command =
                command(
                        destinationAccountId,
                        "250",
                        Currency.VND,
                        "DEP-011",
                        "deposit"
                );

        mockValidDeposit(command);
        mockSaveTransaction();

        ArgumentCaptor<MoneyTransaction> transactionCaptor =
                ArgumentCaptor.forClass(
                        MoneyTransaction.class
                );

        InOrder inOrder = inOrder(
                transactionCommandRepository,
                depositUserAccountPort,
                depositLedgerPort,
                transactionEventPort
        );

        handler.execute(
                adminUserId,
                command
        );

        inOrder.verify(transactionCommandRepository)
                .save(transactionCaptor.capture());

        inOrder.verify(depositUserAccountPort)
                .deposit(
                        destinationAccountId,
                        new BigDecimal("250"),
                        BUSINESS_DATE
                );

        inOrder.verify(depositLedgerPort)
                .recordDeposit(
                        transactionId,
                        destinationAccountId,
                        new BigDecimal("250"),
                        Currency.VND,
                        BUSINESS_DATE
                );

        inOrder.verify(transactionEventPort)
                .publishAccountBalanceChanged(
                        any(AccountBalanceChangedEvent.class)
                );

        MoneyTransaction transaction =
                transactionCaptor.getValue();

        assertEquals(
                TransactionStatus.COMPLETED,
                transaction.getStatus()
        );

        assertNotNull(
                transaction.getCompletedAt()
        );
    }

    @Test
    void shouldNotDepositWhenTransactionSaveFails() {

        DepositMoneyCommand command =
                command(
                        destinationAccountId,
                        "100",
                        Currency.VND,
                        "DEP-012",
                        null
                );

        when(transactionCommandRepository.findByReference(
                "DEP-012"
        )).thenReturn(Optional.empty());

        when(depositUserAccountPort.getDepositInfo(
                destinationAccountId
        )).thenReturn(
                new DepositUserAccountPort.DepositAccountInfo(
                        destinationAccountId,
                        Currency.VND
                )
        );

        when(transactionBusinessDayPort.getCurrentBusinessDate())
                .thenReturn(BUSINESS_DATE);

        doThrow(new RuntimeException("Save failed"))
                .when(transactionCommandRepository)
                .save(any(MoneyTransaction.class));

        assertThrows(
                RuntimeException.class,
                () -> handler.execute(
                        adminUserId,
                        command
                )
        );

        verify(transactionCommandRepository)
                .save(any(MoneyTransaction.class));

        verify(depositUserAccountPort, never())
                .deposit(any(), any(), any());

        verifyNoInteractions(
                depositLedgerPort,
                transactionEventPort
        );
    }

    @Test
    void shouldNotRecordLedgerWhenDepositFails() {

        DepositMoneyCommand command =
                command(
                        destinationAccountId,
                        "100",
                        Currency.VND,
                        "DEP-013",
                        null
                );

        mockValidDeposit(command);
        mockSaveTransaction();

        doThrow(new RuntimeException("Deposit failed"))
                .when(depositUserAccountPort)
                .deposit(
                        destinationAccountId,
                        new BigDecimal("100"),
                        BUSINESS_DATE
                );

        assertThrows(
                RuntimeException.class,
                () -> handler.execute(
                        adminUserId,
                        command
                )
        );

        verify(transactionCommandRepository)
                .save(any(MoneyTransaction.class));

        verify(depositUserAccountPort)
                .deposit(
                        destinationAccountId,
                        new BigDecimal("100"),
                        BUSINESS_DATE
                );

        verifyNoInteractions(
                depositLedgerPort,
                transactionEventPort
        );
    }

    @Test
    void shouldNotPublishEventWhenLedgerRecordingFails() {

        DepositMoneyCommand command =
                command(
                        destinationAccountId,
                        "100",
                        Currency.VND,
                        "DEP-014",
                        null
                );

        mockValidDeposit(command);
        mockSaveTransaction();

        doThrow(new RuntimeException("Ledger failed"))
                .when(depositLedgerPort)
                .recordDeposit(
                        transactionId,
                        destinationAccountId,
                        new BigDecimal("100"),
                        Currency.VND,
                        BUSINESS_DATE
                );

        assertThrows(
                RuntimeException.class,
                () -> handler.execute(
                        adminUserId,
                        command
                )
        );

        verify(depositUserAccountPort)
                .deposit(
                        destinationAccountId,
                        new BigDecimal("100"),
                        BUSINESS_DATE
                );

        verify(depositLedgerPort)
                .recordDeposit(
                        transactionId,
                        destinationAccountId,
                        new BigDecimal("100"),
                        Currency.VND,
                        BUSINESS_DATE
                );

        verifyNoInteractions(
                transactionEventPort
        );
    }

    @Test
    void shouldPropagateWhenPublishingBalanceChangedEventFails() {

        DepositMoneyCommand command =
                command(
                        destinationAccountId,
                        "100",
                        Currency.VND,
                        "DEP-015",
                        null
                );

        mockValidDeposit(command);
        mockSaveTransaction();

        doThrow(new RuntimeException("Event publishing failed"))
                .when(transactionEventPort)
                .publishAccountBalanceChanged(
                        any(AccountBalanceChangedEvent.class)
                );

        assertThrows(
                RuntimeException.class,
                () -> handler.execute(
                        adminUserId,
                        command
                )
        );

        verify(transactionEventPort)
                .publishAccountBalanceChanged(
                        any(AccountBalanceChangedEvent.class)
                );
    }

    private void mockValidDeposit(
            DepositMoneyCommand command
    ) {
        when(transactionCommandRepository.findByReference(
                command.reference()
        )).thenReturn(Optional.empty());

        when(depositUserAccountPort.getDepositInfo(
                command.destinationAccountId()
        )).thenReturn(
                new DepositUserAccountPort.DepositAccountInfo(
                        command.destinationAccountId(),
                        command.currency()
                )
        );

        when(transactionBusinessDayPort.getCurrentBusinessDate())
                .thenReturn(BUSINESS_DATE);
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

    private DepositMoneyCommand command(
            UUID accountId,
            String amount,
            Currency currency,
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