package com.example.ledgercore.transaction.command.handler;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.transaction.command.port.outbound.TransactionBusinessDayPort;
import com.example.ledgercore.transaction.command.port.outbound.TransactionEventPort;
import com.example.ledgercore.transaction.command.port.outbound.TransferLedgerPort;
import com.example.ledgercore.transaction.command.port.outbound.TransferUserAccountPort;
import com.example.ledgercore.transaction.command.repository.TransactionCommandRepository;
import com.example.ledgercore.transaction.command.repository.TransferIntentCommandRepository;
import com.example.ledgercore.transaction.entity.MoneyTransaction;
import com.example.ledgercore.transaction.entity.TransferIntent;
import com.example.ledgercore.transaction.enums.TransactionStatus;
import com.example.ledgercore.transaction.enums.TransactionType;
import com.example.ledgercore.transaction.enums.TransferIntentStatus;
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
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfirmTransferExecutionServiceTest {

    @Mock
    private TransactionCommandRepository transactionCommandRepository;

    @Mock
    private TransferIntentCommandRepository
            transferIntentCommandRepository;

    @Mock
    private TransferUserAccountPort transferUserAccountPort;

    @Mock
    private TransferLedgerPort transferLedgerPort;

    @Mock
    private TransactionEventPort transactionEventPort;

    @Mock
    private TransactionBusinessDayPort transactionBusinessDayPort;

    private ConfirmTransferExecutionService service;

    private Clock clock;

    private final Instant now =
            Instant.parse("2026-09-04T10:00:00Z");

    private static final LocalDate BUSINESS_DATE =
            LocalDate.of(2026, 9, 4);

    private static final BigDecimal TRANSFER_AMOUNT =
            new BigDecimal("100.00");

    private static final BigDecimal SOURCE_BALANCE =
            new BigDecimal("1000.00");

    private UUID userId;
    private UUID intentId;
    private UUID sourceAccountId;
    private UUID destinationAccountId;
    private UUID transactionId;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(
                now,
                ZoneOffset.UTC
        );

        userId = UUID.randomUUID();
        intentId = UUID.randomUUID();
        sourceAccountId = UUID.randomUUID();
        destinationAccountId = UUID.randomUUID();
        transactionId = UUID.randomUUID();

        service =
                new ConfirmTransferExecutionService(
                        transactionCommandRepository,
                        transferIntentCommandRepository,
                        transferUserAccountPort,
                        transferLedgerPort,
                        transactionEventPort,
                        transactionBusinessDayPort,
                        clock
                );
    }

    @Test
    void shouldExecuteTransferSuccessfully() {
        TransferIntent intent =
                createPendingIntent(
                        now.plusSeconds(300)
                );

        mockValidTransfer(intent);

        TransactionResponse response =
                execute();

        assertNotNull(response);

        assertEquals(
                transactionId,
                response.id()
        );

        assertEquals(
                "REF-001",
                response.reference()
        );

        assertEquals(
                TransactionType.TRANSFER,
                response.type()
        );

        assertEquals(
                TransactionStatus.COMPLETED,
                response.status()
        );

        assertEquals(
                sourceAccountId,
                response.sourceAccountId()
        );

        assertEquals(
                destinationAccountId,
                response.destinationAccountId()
        );

        assertEquals(
                TRANSFER_AMOUNT,
                response.amount()
        );

        assertEquals(
                Currency.VND,
                response.currency()
        );

        assertEquals(
                "Test transfer",
                response.description()
        );

        assertEquals(
                now,
                response.completedAt()
        );

        assertEquals(
                TransferIntentStatus.COMPLETED,
                intent.getStatus()
        );

        assertEquals(
                now,
                intent.getCompletedAt()
        );

        verify(transactionCommandRepository)
                .save(any(MoneyTransaction.class));

        verify(transferUserAccountPort)
                .transfer(
                        sourceAccountId,
                        destinationAccountId,
                        TRANSFER_AMOUNT,
                        BUSINESS_DATE
                );

        verify(transferLedgerPort)
                .recordTransfer(
                        transactionId,
                        sourceAccountId,
                        destinationAccountId,
                        TRANSFER_AMOUNT,
                        Currency.VND,
                        BUSINESS_DATE
                );

        verify(transactionEventPort, times(2))
                .publishAccountBalanceChanged(
                        any(AccountBalanceChangedEvent.class)
                );

        verify(transactionBusinessDayPort)
                .getCurrentBusinessDate();
    }

    @Test
    void shouldCreateTransactionCorrectly() {
        TransferIntent intent =
                createPendingIntent(
                        now.plusSeconds(300)
                );

        mockValidTransfer(intent);

        execute();

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
                "REF-001",
                transaction.getReference()
        );

        assertEquals(
                TransactionType.TRANSFER,
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
                sourceAccountId,
                transaction.getSourceAccountId()
        );

        assertEquals(
                destinationAccountId,
                transaction.getDestinationAccountId()
        );

        assertEquals(
                TRANSFER_AMOUNT,
                transaction.getAmount()
        );

        assertEquals(
                Currency.VND,
                transaction.getCurrency()
        );

        assertEquals(
                "Test transfer",
                transaction.getDescription()
        );

        assertEquals(
                now,
                transaction.getCompletedAt()
        );
    }

    @Test
    void shouldGetTransferInfoWithCorrectArguments() {
        TransferIntent intent =
                createPendingIntent(
                        now.plusSeconds(300)
                );

        mockValidTransfer(intent);

        execute();

        verify(transferUserAccountPort)
                .getTransferInfo(
                        userId,
                        sourceAccountId,
                        destinationAccountId
                );
    }

    @Test
    void shouldGetBusinessDateBeforeSavingTransaction() {
        TransferIntent intent =
                createPendingIntent(
                        now.plusSeconds(300)
                );

        mockValidTransfer(intent);

        InOrder inOrder =
                inOrder(
                        transactionBusinessDayPort,
                        transactionCommandRepository
                );

        execute();

        inOrder.verify(transactionBusinessDayPort)
                .getCurrentBusinessDate();

        inOrder.verify(transactionCommandRepository)
                .save(any(MoneyTransaction.class));
    }

    @Test
    void shouldTransferAccountBeforeRecordingLedger() {
        TransferIntent intent =
                createPendingIntent(
                        now.plusSeconds(300)
                );

        mockValidTransfer(intent);

        InOrder inOrder =
                inOrder(
                        transferUserAccountPort,
                        transferLedgerPort
                );

        execute();

        inOrder.verify(transferUserAccountPort)
                .transfer(
                        sourceAccountId,
                        destinationAccountId,
                        TRANSFER_AMOUNT,
                        BUSINESS_DATE
                );

        inOrder.verify(transferLedgerPort)
                .recordTransfer(
                        transactionId,
                        sourceAccountId,
                        destinationAccountId,
                        TRANSFER_AMOUNT,
                        Currency.VND,
                        BUSINESS_DATE
                );
    }

    @Test
    void shouldPublishSourceAccountBalanceDecreaseEvent() {
        TransferIntent intent =
                createPendingIntent(
                        now.plusSeconds(300)
                );

        mockValidTransfer(intent);

        ArgumentCaptor<AccountBalanceChangedEvent> captor =
                ArgumentCaptor.forClass(
                        AccountBalanceChangedEvent.class
                );

        execute();

        verify(transactionEventPort, times(2))
                .publishAccountBalanceChanged(
                        captor.capture()
                );

        AccountBalanceChangedEvent sourceEvent =
                captor.getAllValues().getFirst();

        assertEquals(
                transactionId,
                sourceEvent.transactionId()
        );

        assertEquals(
                sourceAccountId,
                sourceEvent.accountId()
        );

        assertEquals(
                TRANSFER_AMOUNT.negate(),
                sourceEvent.balanceDelta()
        );

        assertEquals(
                Currency.VND,
                sourceEvent.currency()
        );

        assertEquals(
                now,
                sourceEvent.changedAt()
        );
    }

    @Test
    void shouldPublishDestinationAccountBalanceIncreaseEvent() {
        TransferIntent intent =
                createPendingIntent(
                        now.plusSeconds(300)
                );

        mockValidTransfer(intent);

        ArgumentCaptor<AccountBalanceChangedEvent> captor =
                ArgumentCaptor.forClass(
                        AccountBalanceChangedEvent.class
                );

        execute();

        verify(transactionEventPort, times(2))
                .publishAccountBalanceChanged(
                        captor.capture()
                );

        AccountBalanceChangedEvent destinationEvent =
                captor.getAllValues().get(1);

        assertEquals(
                transactionId,
                destinationEvent.transactionId()
        );

        assertEquals(
                destinationAccountId,
                destinationEvent.accountId()
        );

        assertEquals(
                TRANSFER_AMOUNT,
                destinationEvent.balanceDelta()
        );

        assertEquals(
                Currency.VND,
                destinationEvent.currency()
        );

        assertEquals(
                now,
                destinationEvent.changedAt()
        );
    }

    @Test
    void shouldPublishSourceEventBeforeDestinationEvent() {
        TransferIntent intent =
                createPendingIntent(
                        now.plusSeconds(300)
                );

        mockValidTransfer(intent);

        ArgumentCaptor<AccountBalanceChangedEvent> captor =
                ArgumentCaptor.forClass(
                        AccountBalanceChangedEvent.class
                );

        execute();

        verify(transactionEventPort, times(2))
                .publishAccountBalanceChanged(
                        captor.capture()
                );

        var events = captor.getAllValues();

        assertEquals(
                sourceAccountId,
                events.get(0).accountId()
        );

        assertEquals(
                TRANSFER_AMOUNT.negate(),
                events.get(0).balanceDelta()
        );

        assertEquals(
                destinationAccountId,
                events.get(1).accountId()
        );

        assertEquals(
                TRANSFER_AMOUNT,
                events.get(1).balanceDelta()
        );
    }

    @Test
    void shouldCompleteTransactionAndIntentBeforePublishingEvents() {
        TransferIntent intent =
                createPendingIntent(
                        now.plusSeconds(300)
                );

        mockValidTransfer(intent);

        ArgumentCaptor<MoneyTransaction> transactionCaptor =
                ArgumentCaptor.forClass(
                        MoneyTransaction.class
                );

        ArgumentCaptor<AccountBalanceChangedEvent> eventCaptor =
                ArgumentCaptor.forClass(
                        AccountBalanceChangedEvent.class
                );

        execute();

        verify(transactionCommandRepository)
                .save(transactionCaptor.capture());

        verify(transactionEventPort, times(2))
                .publishAccountBalanceChanged(
                        eventCaptor.capture()
                );

        MoneyTransaction transaction =
                transactionCaptor.getValue();

        assertEquals(
                TransactionStatus.COMPLETED,
                transaction.getStatus()
        );

        assertEquals(
                now,
                transaction.getCompletedAt()
        );

        assertEquals(
                TransferIntentStatus.COMPLETED,
                intent.getStatus()
        );

        assertEquals(
                now,
                intent.getCompletedAt()
        );

        assertEquals(
                transaction.getId(),
                eventCaptor.getAllValues()
                        .get(0)
                        .transactionId()
        );

        assertEquals(
                transaction.getId(),
                eventCaptor.getAllValues()
                        .get(1)
                        .transactionId()
        );

        assertEquals(
                now,
                eventCaptor.getAllValues()
                        .get(0)
                        .changedAt()
        );

        assertEquals(
                now,
                eventCaptor.getAllValues()
                        .get(1)
                        .changedAt()
        );
    }

    @Test
    void shouldThrowWhenIntentNotFound() {
        when(
                transferIntentCommandRepository.findById(intentId)
        ).thenReturn(Optional.empty());

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        this::execute
                );

        assertEquals(
                ErrorCode.TRANSFER_INTENT_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(
                transferIntentCommandRepository
        ).findById(intentId);

        verifyNoInteractions(
                transferUserAccountPort,
                transferLedgerPort,
                transactionEventPort,
                transactionBusinessDayPort
        );

        verify(
                transactionCommandRepository,
                never()
        ).save(any());
    }

    @Test
    void shouldThrowWhenUserIsNotOwner() {
        TransferIntent intent =
                createPendingIntent(
                        now.plusSeconds(300)
                );

        intent.setUserId(
                UUID.randomUUID()
        );

        mockIntent(intent);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        this::execute
                );

        assertEquals(
                ErrorCode.ACCESS_DENIED,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                transferUserAccountPort,
                transferLedgerPort,
                transactionEventPort,
                transactionBusinessDayPort
        );

        verify(
                transactionCommandRepository,
                never()
        ).save(any());
    }

    @Test
    void shouldThrowWhenSourceAccountDoesNotMatchIntent() {
        TransferIntent intent =
                createPendingIntent(
                        now.plusSeconds(300)
                );

        mockIntent(intent);

        UUID wrongSourceAccountId =
                UUID.randomUUID();

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.execute(
                                userId,
                                intentId,
                                wrongSourceAccountId,
                                destinationAccountId
                        )
                );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                transferUserAccountPort,
                transferLedgerPort,
                transactionEventPort,
                transactionBusinessDayPort
        );

        verify(
                transactionCommandRepository,
                never()
        ).save(any());
    }

    @Test
    void shouldThrowWhenDestinationAccountDoesNotMatchIntent() {
        TransferIntent intent =
                createPendingIntent(
                        now.plusSeconds(300)
                );

        mockIntent(intent);

        UUID wrongDestinationAccountId =
                UUID.randomUUID();

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.execute(
                                userId,
                                intentId,
                                sourceAccountId,
                                wrongDestinationAccountId
                        )
                );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                transferUserAccountPort,
                transferLedgerPort,
                transactionEventPort,
                transactionBusinessDayPort
        );

        verify(
                transactionCommandRepository,
                never()
        ).save(any());
    }

    @Test
    void shouldThrowWhenSourceAndDestinationAreSame() {
        TransferIntent intent =
                createPendingIntent(
                        now.plusSeconds(300)
                );

        mockIntent(intent);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.execute(
                                userId,
                                intentId,
                                sourceAccountId,
                                sourceAccountId
                        )
                );

        assertEquals(
                ErrorCode.SAME_ACCOUNT_TRANSFER,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                transferUserAccountPort,
                transferLedgerPort,
                transactionEventPort,
                transactionBusinessDayPort
        );

        verify(
                transactionCommandRepository,
                never()
        ).save(any());
    }

    @Test
    void shouldThrowWhenIntentIsNotPending() {
        TransferIntent intent =
                createPendingIntent(
                        now.plusSeconds(300)
                );

        intent.setStatus(
                TransferIntentStatus.COMPLETED
        );

        mockIntent(intent);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        this::execute
                );

        assertEquals(
                ErrorCode.INVALID_TRANSACTION_STATUS,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                transferUserAccountPort,
                transferLedgerPort,
                transactionEventPort,
                transactionBusinessDayPort
        );

        verify(
                transactionCommandRepository,
                never()
        ).save(any());
    }

    @Test
    void shouldThrowWhenIntentIsExpired() {
        TransferIntent intent =
                createPendingIntent(
                        now.minusSeconds(1)
                );

        mockIntent(intent);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        this::execute
                );

        assertEquals(
                ErrorCode.TRANSFER_INTENT_EXPIRED,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                transferUserAccountPort,
                transferLedgerPort,
                transactionEventPort,
                transactionBusinessDayPort
        );

        verify(
                transactionCommandRepository,
                never()
        ).save(any());
    }

    @Test
    void shouldThrowWhenCurrencyDoesNotMatch() {
        TransferIntent intent =
                createPendingIntent(
                        now.plusSeconds(300)
                );

        mockIntent(intent);

        TransferUserAccountPort.TransferAccountInfo transferInfo =
                createTransferInfo(
                        SOURCE_BALANCE,
                        Currency.USD
                );

        when(
                transferUserAccountPort.getTransferInfo(
                        userId,
                        sourceAccountId,
                        destinationAccountId
                )
        ).thenReturn(transferInfo);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        this::execute
                );

        assertEquals(
                ErrorCode.TRANSACTION_CURRENCY_MISMATCH,
                exception.getErrorCode()
        );

        verify(
                transferUserAccountPort
        ).getTransferInfo(
                userId,
                sourceAccountId,
                destinationAccountId
        );

        verify(
                transactionCommandRepository,
                never()
        ).save(any());

        verify(
                transactionBusinessDayPort,
                never()
        ).getCurrentBusinessDate();

        verify(
                transferUserAccountPort,
                never()
        ).transfer(
                any(),
                any(),
                any(),
                any()
        );

        verifyNoInteractions(
                transferLedgerPort,
                transactionEventPort
        );
    }

    @Test
    void shouldThrowWhenBalanceIsInsufficient() {
        TransferIntent intent =
                createPendingIntent(
                        now.plusSeconds(300)
                );

        mockIntent(intent);

        TransferUserAccountPort.TransferAccountInfo transferInfo =
                createTransferInfo(
                        new BigDecimal("50.00"),
                        Currency.VND
                );

        when(
                transferUserAccountPort.getTransferInfo(
                        userId,
                        sourceAccountId,
                        destinationAccountId
                )
        ).thenReturn(transferInfo);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        this::execute
                );

        assertEquals(
                ErrorCode.ACCOUNT_INSUFFICIENT_BALANCE,
                exception.getErrorCode()
        );

        verify(
                transferUserAccountPort
        ).getTransferInfo(
                userId,
                sourceAccountId,
                destinationAccountId
        );

        verify(
                transactionCommandRepository,
                never()
        ).save(any());

        verify(
                transactionBusinessDayPort,
                never()
        ).getCurrentBusinessDate();

        verify(
                transferUserAccountPort,
                never()
        ).transfer(
                any(),
                any(),
                any(),
                any()
        );

        verifyNoInteractions(
                transferLedgerPort,
                transactionEventPort
        );
    }

    @Test
    void shouldPropagateBusinessDateFailure() {
        TransferIntent intent =
                createPendingIntent(
                        now.plusSeconds(300)
                );

        mockIntent(intent);

        mockTransferInfo();

        RuntimeException exception =
                new RuntimeException(
                        "Business date unavailable"
                );

        when(
                transactionBusinessDayPort
                        .getCurrentBusinessDate()
        ).thenThrow(exception);

        RuntimeException actual =
                assertThrows(
                        RuntimeException.class,
                        this::execute
                );

        assertSame(
                exception,
                actual
        );

        verify(
                transactionBusinessDayPort
        ).getCurrentBusinessDate();

        verify(
                transactionCommandRepository,
                never()
        ).save(any());

        verify(
                transferUserAccountPort,
                never()
        ).transfer(
                any(),
                any(),
                any(),
                any()
        );

        verifyNoInteractions(
                transferLedgerPort,
                transactionEventPort
        );
    }

    @Test
    void shouldPropagateTransactionSaveFailure() {
        TransferIntent intent =
                createPendingIntent(
                        now.plusSeconds(300)
                );

        mockIntent(intent);

        mockTransferInfo();
        mockBusinessDate();

        RuntimeException exception =
                new RuntimeException(
                        "Transaction save failed"
                );

        doThrow(exception)
                .when(transactionCommandRepository)
                .save(any(MoneyTransaction.class));

        RuntimeException actual =
                assertThrows(
                        RuntimeException.class,
                        this::execute
                );

        assertSame(
                exception,
                actual
        );

        verify(
                transactionCommandRepository
        ).save(any(MoneyTransaction.class));

        verify(
                transferUserAccountPort,
                never()
        ).transfer(
                any(),
                any(),
                any(),
                any()
        );

        verifyNoInteractions(
                transferLedgerPort,
                transactionEventPort
        );
    }

    @Test
    void shouldPropagateAccountTransferFailure() {
        TransferIntent intent =
                createPendingIntent(
                        now.plusSeconds(300)
                );

        mockIntent(intent);

        mockTransferInfo();
        mockBusinessDate();
        mockSaveTransaction();

        RuntimeException exception =
                new RuntimeException(
                        "Account transfer failed"
                );

        doThrow(exception)
                .when(transferUserAccountPort)
                .transfer(
                        sourceAccountId,
                        destinationAccountId,
                        TRANSFER_AMOUNT,
                        BUSINESS_DATE
                );

        RuntimeException actual =
                assertThrows(
                        RuntimeException.class,
                        this::execute
                );

        assertSame(
                exception,
                actual
        );

        verify(
                transferUserAccountPort
        ).transfer(
                sourceAccountId,
                destinationAccountId,
                TRANSFER_AMOUNT,
                BUSINESS_DATE
        );

        verify(
                transferLedgerPort,
                never()
        ).recordTransfer(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
        );

        verifyNoInteractions(
                transactionEventPort
        );
    }

    @Test
    void shouldPropagateLedgerFailure() {
        TransferIntent intent =
                createPendingIntent(
                        now.plusSeconds(300)
                );

        mockIntent(intent);

        mockTransferInfo();
        mockBusinessDate();
        mockSaveTransaction();

        RuntimeException exception =
                new RuntimeException(
                        "Ledger recording failed"
                );

        doThrow(exception)
                .when(transferLedgerPort)
                .recordTransfer(
                        transactionId,
                        sourceAccountId,
                        destinationAccountId,
                        TRANSFER_AMOUNT,
                        Currency.VND,
                        BUSINESS_DATE
                );

        RuntimeException actual =
                assertThrows(
                        RuntimeException.class,
                        this::execute
                );

        assertSame(
                exception,
                actual
        );

        verify(
                transferUserAccountPort
        ).transfer(
                sourceAccountId,
                destinationAccountId,
                TRANSFER_AMOUNT,
                BUSINESS_DATE
        );

        verify(
                transferLedgerPort
        ).recordTransfer(
                transactionId,
                sourceAccountId,
                destinationAccountId,
                TRANSFER_AMOUNT,
                Currency.VND,
                BUSINESS_DATE
        );

        verify(
                transactionEventPort,
                never()
        ).publishAccountBalanceChanged(
                any(AccountBalanceChangedEvent.class)
        );

        assertEquals(
                TransferIntentStatus.PENDING,
                intent.getStatus()
        );
    }

    @Test
    void shouldPropagateSourceBalanceEventFailure() {
        TransferIntent intent =
                createPendingIntent(
                        now.plusSeconds(300)
                );

        mockIntent(intent);

        mockTransferInfo();
        mockBusinessDate();
        mockSaveTransaction();

        RuntimeException exception =
                new RuntimeException(
                        "Source balance event failed"
                );

        doThrow(exception)
                .when(transactionEventPort)
                .publishAccountBalanceChanged(
                        any(AccountBalanceChangedEvent.class)
                );

        RuntimeException actual =
                assertThrows(
                        RuntimeException.class,
                        this::execute
                );

        assertSame(
                exception,
                actual
        );

        verify(
                transactionEventPort,
                times(1)
        ).publishAccountBalanceChanged(
                any(AccountBalanceChangedEvent.class)
        );
    }

    @Test
    void shouldPropagateDestinationBalanceEventFailure() {
        TransferIntent intent =
                createPendingIntent(
                        now.plusSeconds(300)
                );

        mockIntent(intent);

        mockTransferInfo();
        mockBusinessDate();
        mockSaveTransaction();

        RuntimeException exception =
                new RuntimeException(
                        "Destination balance event failed"
                );

        doAnswer(invocation -> {
            AccountBalanceChangedEvent event =
                    invocation.getArgument(0);

            if (event.accountId()
                    .equals(destinationAccountId)) {

                throw exception;
            }

            return null;
        }).when(transactionEventPort)
                .publishAccountBalanceChanged(
                        any(AccountBalanceChangedEvent.class)
                );

        RuntimeException actual =
                assertThrows(
                        RuntimeException.class,
                        this::execute
                );

        assertSame(
                exception,
                actual
        );

        verify(
                transactionEventPort,
                times(2)
        ).publishAccountBalanceChanged(
                any(AccountBalanceChangedEvent.class)
        );
    }

    @Test
    void shouldKeepIntentPendingWhenLedgerFails() {
        TransferIntent intent =
                createPendingIntent(
                        now.plusSeconds(300)
                );

        mockIntent(intent);
        mockTransferInfo();
        mockBusinessDate();
        mockSaveTransaction();

        doThrow(
                new RuntimeException(
                        "Ledger failed"
                )
        ).when(transferLedgerPort)
                .recordTransfer(
                        transactionId,
                        sourceAccountId,
                        destinationAccountId,
                        TRANSFER_AMOUNT,
                        Currency.VND,
                        BUSINESS_DATE
                );

        assertThrows(
                RuntimeException.class,
                this::execute
        );

        assertEquals(
                TransferIntentStatus.PENDING,
                intent.getStatus()
        );

        assertNull(
                intent.getCompletedAt()
        );
    }

    private TransactionResponse execute() {
        return service.execute(
                userId,
                intentId,
                sourceAccountId,
                destinationAccountId
        );
    }

    private void mockValidTransfer(
            TransferIntent intent
    ) {
        mockIntent(intent);
        mockTransferInfo();
        mockBusinessDate();
        mockSaveTransaction();
    }

    private void mockIntent(
            TransferIntent intent
    ) {
        when(
                transferIntentCommandRepository.findById(intentId)
        ).thenReturn(
                Optional.of(intent)
        );
    }

    private void mockTransferInfo() {
        TransferUserAccountPort.TransferAccountInfo transferInfo =
                createTransferInfo(
                        SOURCE_BALANCE,
                        Currency.VND
                );

        when(
                transferUserAccountPort.getTransferInfo(
                        userId,
                        sourceAccountId,
                        destinationAccountId
                )
        ).thenReturn(transferInfo);
    }

    private void mockBusinessDate() {
        when(
                transactionBusinessDayPort
                        .getCurrentBusinessDate()
        ).thenReturn(
                BUSINESS_DATE
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

    private TransferIntent createPendingIntent(
            Instant expiresAt
    ) {
        return TransferIntent.builder()
                .id(intentId)
                .userId(userId)
                .sourceAccountId(sourceAccountId)
                .destinationAccountId(destinationAccountId)
                .amount(TRANSFER_AMOUNT)
                .currency(Currency.VND)
                .reference("REF-001")
                .description("Test transfer")
                .status(TransferIntentStatus.PENDING)
                .expiresAt(expiresAt)
                .build();
    }

    private TransferUserAccountPort.TransferAccountInfo
    createTransferInfo(
            BigDecimal sourceAvailableBalance,
            Currency currency
    ) {
        return new TransferUserAccountPort.TransferAccountInfo(
                sourceAccountId,
                destinationAccountId,
                currency,
                sourceAvailableBalance
        );
    }
}