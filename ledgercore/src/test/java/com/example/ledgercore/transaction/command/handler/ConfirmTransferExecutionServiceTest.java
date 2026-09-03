package com.example.ledgercore.transaction.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.transaction.command.port.outbound.AccountTransferPort;
import com.example.ledgercore.transaction.command.port.outbound.BusinessDayPort;
import com.example.ledgercore.transaction.command.port.outbound.LedgerTransferPort;
import com.example.ledgercore.transaction.command.port.outbound.TransactionEventPort;
import com.example.ledgercore.transaction.command.repository.TransactionCommandRepository;
import com.example.ledgercore.transaction.command.repository.TransferIntentCommandRepository;
import com.example.ledgercore.transaction.entity.MoneyTransaction;
import com.example.ledgercore.transaction.entity.TransferIntent;
import com.example.ledgercore.transaction.enums.TransactionStatus;
import com.example.ledgercore.transaction.enums.TransactionType;
import com.example.ledgercore.transaction.enums.TransferIntentStatus;
import com.example.ledgercore.transaction.event.TransferCompletedEvent;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfirmTransferExecutionServiceTest {

    @Mock
    private TransactionCommandRepository transactionCommandRepository;

    @Mock
    private TransferIntentCommandRepository transferIntentCommandRepository;

    @Mock
    private AccountTransferPort accountTransferPort;

    @Mock
    private LedgerTransferPort ledgerTransferPort;

    @Mock
    private TransactionEventPort transactionEventPort;

    @Mock
    private BusinessDayPort businessDayPort;

    private ConfirmTransferExecutionService service;

    private Clock clock;

    private final Instant now =
            Instant.parse("2026-09-04T10:00:00Z");

    private static final LocalDate BUSINESS_DATE =
            LocalDate.of(2026, 9, 4);

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
                        accountTransferPort,
                        ledgerTransferPort,
                        transactionEventPort,
                        businessDayPort,
                        clock
                );
    }

    @Test
    void shouldExecuteTransferSuccessfully() {

        TransferIntent intent =
                createPendingIntent(
                        now.plusSeconds(300)
                );

        AccountTransferPort.TransferAccountInfo transferInfo =
                createTransferInfo(
                        new BigDecimal("1000.00"),
                        "VND"
                );

        mockIntent(intent);
        mockTransferInfo(transferInfo);
        mockBusinessDate();
        mockSaveTransaction();

        TransactionResponse response =
                service.execute(
                        userId,
                        intentId,
                        sourceAccountId,
                        destinationAccountId
                );

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
                new BigDecimal("100.00"),
                response.amount()
        );

        assertEquals(
                "VND",
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

        verify(accountTransferPort)
                .transfer(
                        sourceAccountId,
                        destinationAccountId,
                        new BigDecimal("100.00")
                );

        verify(ledgerTransferPort)
                .recordTransfer(
                        eq(transactionId),
                        eq(sourceAccountId),
                        eq(destinationAccountId),
                        eq(new BigDecimal("100.00")),
                        eq("VND"),
                        eq(BUSINESS_DATE)
                );

        verify(transactionEventPort)
                .publishTransferCompleted(
                        any(TransferCompletedEvent.class)
                );

        verify(businessDayPort)
                .getCurrentBusinessDate();
    }

    @Test
    void shouldThrowWhenIntentNotFound() {

        when(
                transferIntentCommandRepository
                        .findById(intentId)
        ).thenReturn(Optional.empty());

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.execute(
                                userId,
                                intentId,
                                sourceAccountId,
                                destinationAccountId
                        )
                );

        assertEquals(
                ErrorCode.TRANSFER_INTENT_NOT_FOUND,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                accountTransferPort,
                ledgerTransferPort,
                transactionEventPort,
                businessDayPort
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

        intent.setUserId(UUID.randomUUID());

        mockIntent(intent);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.execute(
                                userId,
                                intentId,
                                sourceAccountId,
                                destinationAccountId
                        )
                );

        assertEquals(
                ErrorCode.ACCESS_DENIED,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                accountTransferPort,
                ledgerTransferPort,
                transactionEventPort,
                businessDayPort
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

        UUID wrongSourceAccountId =
                UUID.randomUUID();

        mockIntent(intent);

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
                accountTransferPort,
                ledgerTransferPort,
                transactionEventPort,
                businessDayPort
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

        UUID wrongDestinationAccountId =
                UUID.randomUUID();

        mockIntent(intent);

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
                accountTransferPort,
                ledgerTransferPort,
                transactionEventPort,
                businessDayPort
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
                        () -> service.execute(
                                userId,
                                intentId,
                                sourceAccountId,
                                destinationAccountId
                        )
                );

        assertEquals(
                ErrorCode.INVALID_TRANSACTION_STATUS,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                accountTransferPort,
                ledgerTransferPort,
                transactionEventPort,
                businessDayPort
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
                        () -> service.execute(
                                userId,
                                intentId,
                                sourceAccountId,
                                destinationAccountId
                        )
                );

        assertEquals(
                ErrorCode.TRANSFER_INTENT_EXPIRED,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                accountTransferPort,
                ledgerTransferPort,
                transactionEventPort,
                businessDayPort
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

        AccountTransferPort.TransferAccountInfo transferInfo =
                createTransferInfo(
                        new BigDecimal("1000.00"),
                        "USD"
                );

        mockIntent(intent);
        mockTransferInfo(transferInfo);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.execute(
                                userId,
                                intentId,
                                sourceAccountId,
                                destinationAccountId
                        )
                );

        assertEquals(
                ErrorCode.TRANSACTION_CURRENCY_MISMATCH,
                exception.getErrorCode()
        );

        verify(
                transactionCommandRepository,
                never()
        ).save(any());

        verify(
                accountTransferPort,
                never()
        ).transfer(
                any(),
                any(),
                any()
        );

        verifyNoInteractions(
                ledgerTransferPort,
                transactionEventPort,
                businessDayPort
        );
    }

    @Test
    void shouldThrowWhenBalanceIsInsufficient() {

        TransferIntent intent =
                createPendingIntent(
                        now.plusSeconds(300)
                );

        AccountTransferPort.TransferAccountInfo transferInfo =
                createTransferInfo(
                        new BigDecimal("50.00"),
                        "VND"
                );

        mockIntent(intent);
        mockTransferInfo(transferInfo);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.execute(
                                userId,
                                intentId,
                                sourceAccountId,
                                destinationAccountId
                        )
                );

        assertEquals(
                ErrorCode.ACCOUNT_INSUFFICIENT_BALANCE,
                exception.getErrorCode()
        );

        verify(
                transactionCommandRepository,
                never()
        ).save(any());

        verify(
                accountTransferPort,
                never()
        ).transfer(
                any(),
                any(),
                any()
        );

        verifyNoInteractions(
                ledgerTransferPort,
                transactionEventPort,
                businessDayPort
        );
    }

    @Test
    void shouldCreateTransactionFromIntent() {

        TransferIntent intent =
                createPendingIntent(
                        now.plusSeconds(300)
                );

        AccountTransferPort.TransferAccountInfo transferInfo =
                createTransferInfo(
                        new BigDecimal("1000.00"),
                        "VND"
                );

        mockIntent(intent);
        mockTransferInfo(transferInfo);
        mockBusinessDate();
        mockSaveTransaction();

        service.execute(
                userId,
                intentId,
                sourceAccountId,
                destinationAccountId
        );

        ArgumentCaptor<MoneyTransaction> captor =
                ArgumentCaptor.forClass(
                        MoneyTransaction.class
                );

        verify(
                transactionCommandRepository
        ).save(captor.capture());

        MoneyTransaction transaction =
                captor.getValue();

        assertEquals(
                transactionId,
                transaction.getId()
        );

        assertEquals(
                intent.getReference(),
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
                new BigDecimal("100.00"),
                transaction.getAmount()
        );

        assertEquals(
                "VND",
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
    void shouldGetBusinessDateBeforeCreatingTransaction() {

        TransferIntent intent =
                createPendingIntent(
                        now.plusSeconds(300)
                );

        AccountTransferPort.TransferAccountInfo transferInfo =
                createTransferInfo(
                        new BigDecimal("1000.00"),
                        "VND"
                );

        mockIntent(intent);
        mockTransferInfo(transferInfo);
        mockBusinessDate();
        mockSaveTransaction();

        service.execute(
                userId,
                intentId,
                sourceAccountId,
                destinationAccountId
        );

        InOrder inOrder =
                inOrder(
                        businessDayPort,
                        transactionCommandRepository
                );

        inOrder.verify(businessDayPort)
                .getCurrentBusinessDate();

        inOrder.verify(transactionCommandRepository)
                .save(any(MoneyTransaction.class));
    }

    @Test
    void shouldRecordLedgerAfterAccountTransfer() {

        TransferIntent intent =
                createPendingIntent(
                        now.plusSeconds(300)
                );

        AccountTransferPort.TransferAccountInfo transferInfo =
                createTransferInfo(
                        new BigDecimal("1000.00"),
                        "VND"
                );

        mockIntent(intent);
        mockTransferInfo(transferInfo);
        mockBusinessDate();
        mockSaveTransaction();

        service.execute(
                userId,
                intentId,
                sourceAccountId,
                destinationAccountId
        );

        InOrder inOrder =
                inOrder(
                        accountTransferPort,
                        ledgerTransferPort
                );

        inOrder.verify(accountTransferPort)
                .transfer(
                        sourceAccountId,
                        destinationAccountId,
                        new BigDecimal("100.00")
                );

        inOrder.verify(ledgerTransferPort)
                .recordTransfer(
                        transactionId,
                        sourceAccountId,
                        destinationAccountId,
                        new BigDecimal("100.00"),
                        "VND",
                        BUSINESS_DATE
                );
    }

    @Test
    void shouldPublishCompletedEvent() {

        TransferIntent intent =
                createPendingIntent(
                        now.plusSeconds(300)
                );

        AccountTransferPort.TransferAccountInfo transferInfo =
                createTransferInfo(
                        new BigDecimal("1000.00"),
                        "VND"
                );

        mockIntent(intent);
        mockTransferInfo(transferInfo);
        mockBusinessDate();
        mockSaveTransaction();

        service.execute(
                userId,
                intentId,
                sourceAccountId,
                destinationAccountId
        );

        ArgumentCaptor<TransferCompletedEvent> captor =
                ArgumentCaptor.forClass(
                        TransferCompletedEvent.class
                );

        verify(
                transactionEventPort
        ).publishTransferCompleted(
                captor.capture()
        );

        TransferCompletedEvent event =
                captor.getValue();

        assertNotNull(event);

        assertEquals(
                transactionId,
                event.transactionId()
        );

        assertEquals(
                "REF-001",
                event.reference()
        );

        assertEquals(
                sourceAccountId,
                event.sourceAccountId()
        );

        assertEquals(
                destinationAccountId,
                event.destinationAccountId()
        );

        assertEquals(
                new BigDecimal("100.00"),
                event.amount()
        );

        assertEquals(
                "VND",
                event.currency()
        );

        assertEquals(
                now,
                event.completedAt()
        );
    }

    @Test
    void shouldUseClockForCompletionTime() {

        TransferIntent intent =
                createPendingIntent(
                        now.plusSeconds(300)
                );

        AccountTransferPort.TransferAccountInfo transferInfo =
                createTransferInfo(
                        new BigDecimal("1000.00"),
                        "VND"
                );

        mockIntent(intent);
        mockTransferInfo(transferInfo);
        mockBusinessDate();
        mockSaveTransaction();

        service.execute(
                userId,
                intentId,
                sourceAccountId,
                destinationAccountId
        );

        assertEquals(
                now,
                intent.getCompletedAt()
        );

        ArgumentCaptor<MoneyTransaction> captor =
                ArgumentCaptor.forClass(
                        MoneyTransaction.class
                );

        verify(
                transactionCommandRepository
        ).save(captor.capture());

        assertEquals(
                now,
                captor.getValue().getCompletedAt()
        );
    }

    @Test
    void shouldNotTransferWhenValidationFails() {

        TransferIntent intent =
                createPendingIntent(
                        now.plusSeconds(300)
                );

        AccountTransferPort.TransferAccountInfo transferInfo =
                createTransferInfo(
                        new BigDecimal("99.99"),
                        "VND"
                );

        mockIntent(intent);
        mockTransferInfo(transferInfo);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> service.execute(
                                userId,
                                intentId,
                                sourceAccountId,
                                destinationAccountId
                        )
                );

        assertEquals(
                ErrorCode.ACCOUNT_INSUFFICIENT_BALANCE,
                exception.getErrorCode()
        );

        verify(
                transactionCommandRepository,
                never()
        ).save(any());

        verify(
                accountTransferPort,
                never()
        ).transfer(
                any(),
                any(),
                any()
        );

        verifyNoInteractions(
                ledgerTransferPort,
                transactionEventPort,
                businessDayPort
        );
    }

    @Test
    void shouldNotPublishEventWhenAccountTransferFails() {

        TransferIntent intent =
                createPendingIntent(
                        now.plusSeconds(300)
                );

        AccountTransferPort.TransferAccountInfo transferInfo =
                createTransferInfo(
                        new BigDecimal("1000.00"),
                        "VND"
                );

        mockIntent(intent);
        mockTransferInfo(transferInfo);
        mockBusinessDate();
        mockSaveTransaction();

        doThrow(
                new RuntimeException("Transfer failed")
        ).when(accountTransferPort)
                .transfer(
                        sourceAccountId,
                        destinationAccountId,
                        new BigDecimal("100.00")
                );

        assertThrows(
                RuntimeException.class,
                () -> service.execute(
                        userId,
                        intentId,
                        sourceAccountId,
                        destinationAccountId
                )
        );

        verify(
                ledgerTransferPort,
                never()
        ).recordTransfer(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
        );

        verify(
                transactionEventPort,
                never()
        ).publishTransferCompleted(any());
    }

    @Test
    void shouldNotPublishEventWhenLedgerRecordingFails() {

        TransferIntent intent =
                createPendingIntent(
                        now.plusSeconds(300)
                );

        AccountTransferPort.TransferAccountInfo transferInfo =
                createTransferInfo(
                        new BigDecimal("1000.00"),
                        "VND"
                );

        mockIntent(intent);
        mockTransferInfo(transferInfo);
        mockBusinessDate();
        mockSaveTransaction();

        doThrow(
                new RuntimeException(
                        "Ledger recording failed"
                )
        ).when(ledgerTransferPort)
                .recordTransfer(
                        transactionId,
                        sourceAccountId,
                        destinationAccountId,
                        new BigDecimal("100.00"),
                        "VND",
                        BUSINESS_DATE
                );

        assertThrows(
                RuntimeException.class,
                () -> service.execute(
                        userId,
                        intentId,
                        sourceAccountId,
                        destinationAccountId
                )
        );

        verify(accountTransferPort)
                .transfer(
                        sourceAccountId,
                        destinationAccountId,
                        new BigDecimal("100.00")
                );

        verify(
                transactionEventPort,
                never()
        ).publishTransferCompleted(any());

        assertEquals(
                TransferIntentStatus.PENDING,
                intent.getStatus()
        );
    }

    @Test
    void shouldCompleteIntentAfterSuccessfulLedgerRecording() {

        TransferIntent intent =
                createPendingIntent(
                        now.plusSeconds(300)
                );

        AccountTransferPort.TransferAccountInfo transferInfo =
                createTransferInfo(
                        new BigDecimal("1000.00"),
                        "VND"
                );

        mockIntent(intent);
        mockTransferInfo(transferInfo);
        mockBusinessDate();
        mockSaveTransaction();

        service.execute(
                userId,
                intentId,
                sourceAccountId,
                destinationAccountId
        );

        InOrder inOrder =
                inOrder(
                        accountTransferPort,
                        ledgerTransferPort,
                        transactionEventPort
                );

        inOrder.verify(accountTransferPort)
                .transfer(
                        sourceAccountId,
                        destinationAccountId,
                        new BigDecimal("100.00")
                );

        inOrder.verify(ledgerTransferPort)
                .recordTransfer(
                        transactionId,
                        sourceAccountId,
                        destinationAccountId,
                        new BigDecimal("100.00"),
                        "VND",
                        BUSINESS_DATE
                );

        inOrder.verify(transactionEventPort)
                .publishTransferCompleted(
                        any(TransferCompletedEvent.class)
                );

        assertEquals(
                TransferIntentStatus.COMPLETED,
                intent.getStatus()
        );

        assertEquals(
                now,
                intent.getCompletedAt()
        );
    }

    @Test
    void shouldPassIntentAccountIdsToAccountTransferPort() {

        TransferIntent intent =
                createPendingIntent(
                        now.plusSeconds(300)
                );

        AccountTransferPort.TransferAccountInfo transferInfo =
                createTransferInfo(
                        new BigDecimal("1000.00"),
                        "VND"
                );

        mockIntent(intent);
        mockTransferInfo(transferInfo);
        mockBusinessDate();
        mockSaveTransaction();

        service.execute(
                userId,
                intentId,
                sourceAccountId,
                destinationAccountId
        );

        verify(accountTransferPort)
                .getTransferInfo(
                        userId,
                        sourceAccountId,
                        destinationAccountId
                );
    }

    @Test
    void shouldNotExecuteWhenSourceAndDestinationAreSame() {

        TransferIntent intent =
                createPendingIntent(
                        now.plusSeconds(300)
                );

        intent.setDestinationAccountId(
                sourceAccountId
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
                accountTransferPort,
                ledgerTransferPort,
                transactionEventPort,
                businessDayPort
        );

        verify(
                transactionCommandRepository,
                never()
        ).save(any());
    }

    private void mockIntent(
            TransferIntent intent
    ) {
        when(
                transferIntentCommandRepository
                        .findById(intentId)
        ).thenReturn(
                Optional.of(intent)
        );
    }

    private void mockTransferInfo(
            AccountTransferPort.TransferAccountInfo transferInfo
    ) {
        when(
                accountTransferPort.getTransferInfo(
                        userId,
                        sourceAccountId,
                        destinationAccountId
                )
        ).thenReturn(
                transferInfo
        );
    }

    private void mockBusinessDate() {
        when(
                businessDayPort.getCurrentBusinessDate()
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
                .amount(new BigDecimal("100.00"))
                .currency("VND")
                .reference("REF-001")
                .description("Test transfer")
                .status(TransferIntentStatus.PENDING)
                .expiresAt(expiresAt)
                .build();
    }

    private AccountTransferPort.TransferAccountInfo
    createTransferInfo(
            BigDecimal sourceBalance,
            String currency
    ) {
        return new AccountTransferPort.TransferAccountInfo(
                sourceAccountId,
                destinationAccountId,
                currency,
                sourceBalance
        );
    }
}