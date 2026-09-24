package com.example.ledgercore.transfer.command.handler;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.transaction.command.port.outbound.TransferOtpPort;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;
import com.example.ledgercore.transfer.command.dto.ConfirmTransferCommand;
import com.example.ledgercore.transfer.command.repository.TransferIntentCommandRepository;
import com.example.ledgercore.transfer.entity.TransferIntent;
import com.example.ledgercore.transfer.enums.TransferIntentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfirmTransferHandlerTest {

    @Mock
    private TransferIntentCommandRepository
            transferIntentCommandRepository;

    @Mock
    private TransferOtpPort transferOtpPort;

    @Mock
    private ConfirmTransferExecutionService
            confirmTransferExecutionService;

    private Clock clock;

    private ConfirmTransferHandler handler;

    private static final Instant NOW =
            Instant.parse("2026-08-27T10:00:00Z");

    private static final String OTP =
            "123456";

    private static final String REFERENCE =
            "REF-001";

    private static final String DESCRIPTION =
            "Test transfer";

    private static final BigDecimal AMOUNT =
            new BigDecimal("100");

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

        handler = new ConfirmTransferHandler(
                transferIntentCommandRepository,
                transferOtpPort,
                confirmTransferExecutionService,
                clock
        );

        userId = UUID.randomUUID();
        intentId = UUID.randomUUID();
        sourceAccountId = UUID.randomUUID();
        destinationAccountId = UUID.randomUUID();
    }

    @Test
    void shouldConfirmTransferSuccessfully() {
        TransferIntent intent =
                createPendingIntent(
                        NOW.plusSeconds(300)
                );

        ConfirmTransferCommand command =
                createCommand();

        TransactionResponse expected =
                org.mockito.Mockito.mock(
                        TransactionResponse.class
                );

        mockIntent(intent);

        when(
                confirmTransferExecutionService.execute(
                        userId,
                        intentId,
                        sourceAccountId,
                        destinationAccountId
                )
        ).thenReturn(expected);

        TransactionResponse actual =
                handler.execute(
                        userId,
                        command
                );

        assertSame(
                expected,
                actual
        );

        verify(
                transferOtpPort
        ).verifyConfirmationOtp(
                userId,
                intentId,
                OTP
        );

        verify(
                confirmTransferExecutionService
        ).execute(
                userId,
                intentId,
                sourceAccountId,
                destinationAccountId
        );
    }

    @Test
    void shouldThrowWhenIntentNotFound() {
        ConfirmTransferCommand command =
                createCommand();

        when(
                transferIntentCommandRepository
                        .findById(intentId)
        ).thenReturn(Optional.empty());

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                userId,
                                command
                        )
                );

        assertEquals(
                ErrorCode.TRANSFER_INTENT_NOT_FOUND,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                transferOtpPort,
                confirmTransferExecutionService
        );
    }

    @Test
    void shouldThrowAccessDeniedWhenUserIsNotOwner() {
        UUID ownerId =
                UUID.randomUUID();

        TransferIntent intent =
                createPendingIntent(
                        NOW.plusSeconds(300),
                        ownerId
                );

        ConfirmTransferCommand command =
                createCommand();

        mockIntent(intent);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                userId,
                                command
                        )
                );

        assertEquals(
                ErrorCode.ACCESS_DENIED,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                transferOtpPort,
                confirmTransferExecutionService
        );
    }

    @Test
    void shouldThrowInvalidCurrencyAmountWhenAmountScaleExceedsCurrencyScale() {
        TransferIntent intent =
                createPendingIntent(
                        NOW.plusSeconds(300)
                );

        intent.setAmount(
                new BigDecimal("100.1")
        );

        ConfirmTransferCommand command =
                createCommand();

        mockIntent(intent);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                userId,
                                command
                        )
                );

        assertEquals(
                ErrorCode.INVALID_CURRENCY_AMOUNT,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                transferOtpPort,
                confirmTransferExecutionService
        );
    }

    @Test
    void shouldThrowInvalidTransactionStatusWhenIntentIsNotPending() {
        TransferIntent intent =
                TransferIntent.builder()
                        .id(intentId)
                        .userId(userId)
                        .sourceAccountId(sourceAccountId)
                        .destinationAccountId(destinationAccountId)
                        .amount(AMOUNT)
                        .currency(Currency.VND)
                        .reference(REFERENCE)
                        .description(DESCRIPTION)
                        .status(TransferIntentStatus.COMPLETED)
                        .expiresAt(NOW.plusSeconds(300))
                        .build();

        ConfirmTransferCommand command =
                createCommand();

        mockIntent(intent);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                userId,
                                command
                        )
                );

        assertEquals(
                ErrorCode.INVALID_TRANSACTION_STATUS,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                transferOtpPort,
                confirmTransferExecutionService
        );
    }

    @Test
    void shouldThrowTransferIntentExpiredWhenIntentIsExpired() {
        TransferIntent intent =
                createPendingIntent(
                        NOW.minusSeconds(1)
                );

        ConfirmTransferCommand command =
                createCommand();

        mockIntent(intent);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                userId,
                                command
                        )
                );

        assertEquals(
                ErrorCode.TRANSFER_INTENT_EXPIRED,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                transferOtpPort,
                confirmTransferExecutionService
        );
    }

    @Test
    void shouldThrowTransferIntentExpiredWhenIntentExpiresAtNow() {
        TransferIntent intent =
                createPendingIntent(
                        NOW
                );

        ConfirmTransferCommand command =
                createCommand();

        mockIntent(intent);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                userId,
                                command
                        )
                );

        assertEquals(
                ErrorCode.TRANSFER_INTENT_EXPIRED,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                transferOtpPort,
                confirmTransferExecutionService
        );
    }

    @Test
    void shouldVerifyOtpBeforeExecutingTransfer() {
        TransferIntent intent =
                createPendingIntent(
                        NOW.plusSeconds(300)
                );

        ConfirmTransferCommand command =
                createCommand();

        TransactionResponse expected =
                org.mockito.Mockito.mock(
                        TransactionResponse.class
                );

        mockIntent(intent);

        when(
                confirmTransferExecutionService.execute(
                        userId,
                        intentId,
                        sourceAccountId,
                        destinationAccountId
                )
        ).thenReturn(expected);

        handler.execute(
                userId,
                command
        );

        var inOrder =
                org.mockito.Mockito.inOrder(
                        transferOtpPort,
                        confirmTransferExecutionService
                );

        inOrder.verify(
                transferOtpPort
        ).verifyConfirmationOtp(
                userId,
                intentId,
                OTP
        );

        inOrder.verify(
                confirmTransferExecutionService
        ).execute(
                userId,
                intentId,
                sourceAccountId,
                destinationAccountId
        );
    }

    @Test
    void shouldNotCallExecutionWhenOtpVerificationFails() {
        TransferIntent intent =
                createPendingIntent(
                        NOW.plusSeconds(300)
                );

        ConfirmTransferCommand command =
                createCommand();

        mockIntent(intent);

        doThrow(
                new BusinessException(
                        ErrorCode.INVALID_VERIFICATION_CODE
                )
        ).when(transferOtpPort)
                .verifyConfirmationOtp(
                        userId,
                        intentId,
                        OTP
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                userId,
                                command
                        )
                );

        assertEquals(
                ErrorCode.INVALID_VERIFICATION_CODE,
                exception.getErrorCode()
        );

        verify(
                transferOtpPort
        ).verifyConfirmationOtp(
                userId,
                intentId,
                OTP
        );

        verifyNoInteractions(
                confirmTransferExecutionService
        );
    }

    @Test
    void shouldNotCallExecutionWhenOtpVerificationThrowsUnexpectedException() {
        TransferIntent intent =
                createPendingIntent(
                        NOW.plusSeconds(300)
                );

        ConfirmTransferCommand command =
                createCommand();

        mockIntent(intent);

        RuntimeException exception =
                new RuntimeException("OTP service unavailable");

        doThrow(exception)
                .when(transferOtpPort)
                .verifyConfirmationOtp(
                        userId,
                        intentId,
                        OTP
                );

        RuntimeException actual =
                assertThrows(
                        RuntimeException.class,
                        () -> handler.execute(
                                userId,
                                command
                        )
                );

        assertSame(
                exception,
                actual
        );

        verifyNoInteractions(
                confirmTransferExecutionService
        );
    }

    @Test
    void shouldPropagateExecutionServiceException() {
        TransferIntent intent =
                createPendingIntent(
                        NOW.plusSeconds(300)
                );

        ConfirmTransferCommand command =
                createCommand();

        mockIntent(intent);

        RuntimeException exception =
                new RuntimeException(
                        "Transfer execution failed"
                );

        doThrow(exception)
                .when(confirmTransferExecutionService)
                .execute(
                        userId,
                        intentId,
                        sourceAccountId,
                        destinationAccountId
                );

        RuntimeException actual =
                assertThrows(
                        RuntimeException.class,
                        () -> handler.execute(
                                userId,
                                command
                        )
                );

        assertSame(
                exception,
                actual
        );

        verify(
                transferOtpPort
        ).verifyConfirmationOtp(
                userId,
                intentId,
                OTP
        );

        verify(
                confirmTransferExecutionService
        ).execute(
                userId,
                intentId,
                sourceAccountId,
                destinationAccountId
        );
    }

    @Test
    void shouldNotCallExecutionWhenIntentIsExpired() {
        TransferIntent intent =
                createPendingIntent(
                        NOW.minusSeconds(1)
                );

        ConfirmTransferCommand command =
                createCommand();

        mockIntent(intent);

        assertThrows(
                BusinessException.class,
                () -> handler.execute(
                        userId,
                        command
                )
        );

        verify(
                transferOtpPort,
                never()
        ).verifyConfirmationOtp(
                anyUuid(),
                anyUuid(),
                anyString()
        );

        verifyNoInteractions(
                confirmTransferExecutionService
        );
    }

    @Test
    void shouldNotCallExecutionWhenIntentIsNotPending() {
        TransferIntent intent =
                TransferIntent.builder()
                        .id(intentId)
                        .userId(userId)
                        .sourceAccountId(sourceAccountId)
                        .destinationAccountId(destinationAccountId)
                        .amount(AMOUNT)
                        .currency(Currency.VND)
                        .reference(REFERENCE)
                        .description(DESCRIPTION)
                        .status(TransferIntentStatus.COMPLETED)
                        .expiresAt(NOW.plusSeconds(300))
                        .build();

        ConfirmTransferCommand command =
                createCommand();

        mockIntent(intent);

        assertThrows(
                BusinessException.class,
                () -> handler.execute(
                        userId,
                        command
                )
        );

        verify(
                transferOtpPort,
                never()
        ).verifyConfirmationOtp(
                anyUuid(),
                anyUuid(),
                anyString()
        );

        verifyNoInteractions(
                confirmTransferExecutionService
        );
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
                .amount(AMOUNT)
                .currency(Currency.VND)
                .reference(REFERENCE)
                .description(DESCRIPTION)
                .status(TransferIntentStatus.PENDING)
                .expiresAt(expiresAt)
                .build();
    }

    private ConfirmTransferCommand createCommand() {
        return new ConfirmTransferCommand(
                intentId,
                OTP
        );
    }

    private static UUID anyUuid() {
        return UUID.randomUUID();
    }

    private static String anyString() {
        return "any";
    }
}