package com.example.ledgercore.transfer.command.handler;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.transaction.command.dto.TransferMoneyCommand;
import com.example.ledgercore.transaction.command.port.outbound.TransferUserAccountPort;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;
import com.example.ledgercore.transfer.command.port.outbound.TransferTransactionPort;
import com.example.ledgercore.transfer.command.repository.TransferIntentCommandRepository;
import com.example.ledgercore.transfer.entity.TransferIntent;
import com.example.ledgercore.transfer.enums.TransferIntentStatus;
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
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfirmTransferExecutionServiceTest {

    @Mock
    private TransferIntentCommandRepository
            transferIntentCommandRepository;

    @Mock
    private TransferUserAccountPort
            transferUserAccountPort;

    @Mock
    private TransferTransactionPort
            transferTransactionPort;

    private ConfirmTransferExecutionService service;

    private Clock clock;

    private static final Instant NOW =
            Instant.parse("2026-09-04T10:00:00Z");

    private static final BigDecimal TRANSFER_AMOUNT =
            new BigDecimal("100.00");

    private static final BigDecimal SOURCE_BALANCE =
            new BigDecimal("1000.00");

    private static final String REFERENCE =
            "REF-001";

    private static final String DESCRIPTION =
            "Test transfer";

    private UUID userId;
    private UUID intentId;
    private UUID sourceAccountId;
    private UUID destinationAccountId;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(
                NOW,
                ZoneOffset.UTC
        );

        userId = UUID.randomUUID();
        intentId = UUID.randomUUID();
        sourceAccountId = UUID.randomUUID();
        destinationAccountId = UUID.randomUUID();

        service =
                new ConfirmTransferExecutionService(
                        transferIntentCommandRepository,
                        transferUserAccountPort,
                        transferTransactionPort,
                        clock
                );
    }

    @Test
    void shouldExecuteTransferSuccessfully() {
        TransferIntent intent =
                createPendingIntent(
                        NOW.plusSeconds(300)
                );

        TransactionResponse expectedResponse =
                org.mockito.Mockito.mock(
                        TransactionResponse.class
                );

        mockValidTransfer(intent);

        when(
                transferTransactionPort.transfer(
                        any(TransferMoneyCommand.class)
                )
        ).thenReturn(expectedResponse);

        TransactionResponse actualResponse =
                execute();

        assertSame(
                expectedResponse,
                actualResponse
        );

        assertEquals(
                TransferIntentStatus.COMPLETED,
                intent.getStatus()
        );

        assertEquals(
                NOW,
                intent.getCompletedAt()
        );

        verify(
                transferTransactionPort
        ).transfer(
                any(TransferMoneyCommand.class)
        );
    }

    @Test
    void shouldCreateTransferCommandCorrectly() {
        TransferIntent intent =
                createPendingIntent(
                        NOW.plusSeconds(300)
                );

        mockValidTransfer(intent);

        TransactionResponse response =
                org.mockito.Mockito.mock(
                        TransactionResponse.class
                );

        when(
                transferTransactionPort.transfer(
                        any(TransferMoneyCommand.class)
                )
        ).thenReturn(response);

        ArgumentCaptor<TransferMoneyCommand> captor =
                ArgumentCaptor.forClass(
                        TransferMoneyCommand.class
                );

        execute();

        verify(
                transferTransactionPort
        ).transfer(
                captor.capture()
        );

        TransferMoneyCommand command =
                captor.getValue();

        assertEquals(
                sourceAccountId,
                command.sourceAccountId()
        );

        assertEquals(
                destinationAccountId,
                command.destinationAccountId()
        );

        assertEquals(
                TRANSFER_AMOUNT,
                command.amount()
        );

        assertEquals(
                Currency.VND,
                command.currency()
        );

        assertEquals(
                REFERENCE,
                command.reference()
        );

        assertEquals(
                DESCRIPTION,
                command.description()
        );
    }

    @Test
    void shouldGetTransferInfoWithCorrectArguments() {
        TransferIntent intent =
                createPendingIntent(
                        NOW.plusSeconds(300)
                );

        mockValidTransfer(intent);

        when(
                transferTransactionPort.transfer(
                        any(TransferMoneyCommand.class)
                )
        ).thenReturn(
                org.mockito.Mockito.mock(
                        TransactionResponse.class
                )
        );

        execute();

        verify(
                transferUserAccountPort
        ).getTransferInfo(
                userId,
                sourceAccountId,
                destinationAccountId
        );
    }

    @Test
    void shouldCompleteIntentAfterTransferSucceeds() {
        TransferIntent intent =
                createPendingIntent(
                        NOW.plusSeconds(300)
                );

        mockValidTransfer(intent);

        when(
                transferTransactionPort.transfer(
                        any(TransferMoneyCommand.class)
                )
        ).thenReturn(
                org.mockito.Mockito.mock(
                        TransactionResponse.class
                )
        );

        execute();

        assertEquals(
                TransferIntentStatus.COMPLETED,
                intent.getStatus()
        );

        assertEquals(
                NOW,
                intent.getCompletedAt()
        );
    }

    @Test
    void shouldCompleteIntentAfterTransactionTransfer() {
        TransferIntent intent =
                createPendingIntent(
                        NOW.plusSeconds(300)
                );

        mockValidTransfer(intent);

        when(
                transferTransactionPort.transfer(
                        any(TransferMoneyCommand.class)
                )
        ).thenReturn(
                org.mockito.Mockito.mock(
                        TransactionResponse.class
                )
        );

        InOrder inOrder =
                org.mockito.Mockito.inOrder(
                        transferTransactionPort
                );

        execute();

        inOrder.verify(
                transferTransactionPort
        ).transfer(
                any(TransferMoneyCommand.class)
        );

        assertEquals(
                TransferIntentStatus.COMPLETED,
                intent.getStatus()
        );

        assertEquals(
                NOW,
                intent.getCompletedAt()
        );
    }

    @Test
    void shouldThrowWhenIntentNotFound() {
        when(
                transferIntentCommandRepository
                        .findById(intentId)
        ).thenReturn(
                Optional.empty()
        );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        this::execute
                );

        assertEquals(
                ErrorCode.TRANSFER_INTENT_NOT_FOUND,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                transferUserAccountPort,
                transferTransactionPort
        );
    }

    @Test
    void shouldThrowWhenUserIsNotOwner() {
        TransferIntent intent =
                createPendingIntent(
                        NOW.plusSeconds(300),
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
                transferTransactionPort
        );
    }

    @Test
    void shouldThrowWhenSourceAccountDoesNotMatchIntent() {
        TransferIntent intent =
                createPendingIntent(
                        NOW.plusSeconds(300)
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
                transferTransactionPort
        );
    }

    @Test
    void shouldThrowWhenDestinationAccountDoesNotMatchIntent() {
        TransferIntent intent =
                createPendingIntent(
                        NOW.plusSeconds(300)
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
                transferTransactionPort
        );
    }

    @Test
    void shouldThrowWhenSourceAndDestinationAreSame() {
        TransferIntent intent =
                createPendingIntent(
                        NOW.plusSeconds(300)
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
                transferTransactionPort
        );
    }

    @Test
    void shouldThrowWhenIntentIsNotPending() {
        TransferIntent intent =
                TransferIntent.builder()
                        .id(intentId)
                        .userId(userId)
                        .sourceAccountId(sourceAccountId)
                        .destinationAccountId(destinationAccountId)
                        .amount(TRANSFER_AMOUNT)
                        .currency(Currency.VND)
                        .reference(REFERENCE)
                        .description(DESCRIPTION)
                        .status(TransferIntentStatus.COMPLETED)
                        .expiresAt(NOW.plusSeconds(300))
                        .build();

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
                transferTransactionPort
        );
    }

    @Test
    void shouldThrowWhenIntentIsExpired() {
        TransferIntent intent =
                createPendingIntent(
                        NOW.minusSeconds(1)
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
                transferTransactionPort
        );
    }

    @Test
    void shouldThrowWhenIntentExpiresAtNow() {
        TransferIntent intent =
                createPendingIntent(
                        NOW
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
                transferTransactionPort
        );
    }

    @Test
    void shouldThrowWhenCurrencyDoesNotMatch() {
        TransferIntent intent =
                createPendingIntent(
                        NOW.plusSeconds(300)
                );

        mockIntent(intent);

        when(
                transferUserAccountPort.getTransferInfo(
                        userId,
                        sourceAccountId,
                        destinationAccountId
                )
        ).thenReturn(
                createTransferInfo(
                        SOURCE_BALANCE,
                        Currency.USD
                )
        );

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

        verifyNoInteractions(
                transferTransactionPort
        );
    }

    @Test
    void shouldThrowWhenBalanceIsInsufficient() {
        TransferIntent intent =
                createPendingIntent(
                        NOW.plusSeconds(300)
                );

        mockIntent(intent);

        when(
                transferUserAccountPort.getTransferInfo(
                        userId,
                        sourceAccountId,
                        destinationAccountId
                )
        ).thenReturn(
                createTransferInfo(
                        new BigDecimal("50.00"),
                        Currency.VND
                )
        );

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

        verifyNoInteractions(
                transferTransactionPort
        );
    }

    @Test
    void shouldReturnTransactionResponseFromTransferPort() {
        TransferIntent intent =
                createPendingIntent(
                        NOW.plusSeconds(300)
                );

        TransactionResponse expectedResponse =
                org.mockito.Mockito.mock(
                        TransactionResponse.class
                );

        mockValidTransfer(intent);

        when(
                transferTransactionPort.transfer(
                        any(TransferMoneyCommand.class)
                )
        ).thenReturn(expectedResponse);

        TransactionResponse actualResponse =
                execute();

        assertSame(
                expectedResponse,
                actualResponse
        );
    }

    @Test
    void shouldPropagateTransferFailure() {
        TransferIntent intent =
                createPendingIntent(
                        NOW.plusSeconds(300)
                );

        mockValidTransfer(intent);

        RuntimeException exception =
                new RuntimeException(
                        "Transfer failed"
                );

        when(
                transferTransactionPort.transfer(
                        any(TransferMoneyCommand.class)
                )
        ).thenThrow(exception);

        RuntimeException actualException =
                assertThrows(
                        RuntimeException.class,
                        this::execute
                );

        assertSame(
                exception,
                actualException
        );

        assertEquals(
                TransferIntentStatus.PENDING,
                intent.getStatus()
        );

        assertNull(
                intent.getCompletedAt()
        );
    }

    @Test
    void shouldNotCompleteIntentWhenTransferFails() {
        TransferIntent intent =
                createPendingIntent(
                        NOW.plusSeconds(300)
                );

        mockValidTransfer(intent);

        when(
                transferTransactionPort.transfer(
                        any(TransferMoneyCommand.class)
                )
        ).thenThrow(
                new RuntimeException("Transfer failed")
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

    @Test
    void shouldPropagateTransferBusinessException() {
        TransferIntent intent =
                createPendingIntent(
                        NOW.plusSeconds(300)
                );

        mockValidTransfer(intent);

        BusinessException exception =
                new BusinessException(
                        ErrorCode.ACCOUNT_INSUFFICIENT_BALANCE
                );

        when(
                transferTransactionPort.transfer(
                        any(TransferMoneyCommand.class)
                )
        ).thenThrow(exception);

        BusinessException actualException =
                assertThrows(
                        BusinessException.class,
                        this::execute
                );

        assertSame(
                exception,
                actualException
        );

        assertEquals(
                TransferIntentStatus.PENDING,
                intent.getStatus()
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
    }

    private void mockIntent(
            TransferIntent intent
    ) {
        when(
                transferIntentCommandRepository.findById(
                        intentId
                )
        ).thenReturn(
                Optional.of(intent)
        );
    }

    private void mockTransferInfo() {
        when(
                transferUserAccountPort.getTransferInfo(
                        userId,
                        sourceAccountId,
                        destinationAccountId
                )
        ).thenReturn(
                createTransferInfo(
                        SOURCE_BALANCE,
                        Currency.VND
                )
        );
    }

    private TransferIntent createPendingIntent(
            Instant expiresAt
    ) {
        return createPendingIntent(
                expiresAt,
                userId
        );
    }

    private TransferIntent createPendingIntent(
            Instant expiresAt,
            UUID ownerId
    ) {
        return TransferIntent.builder()
                .id(intentId)
                .userId(ownerId)
                .sourceAccountId(sourceAccountId)
                .destinationAccountId(destinationAccountId)
                .amount(TRANSFER_AMOUNT)
                .currency(Currency.VND)
                .reference(REFERENCE)
                .description(DESCRIPTION)
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