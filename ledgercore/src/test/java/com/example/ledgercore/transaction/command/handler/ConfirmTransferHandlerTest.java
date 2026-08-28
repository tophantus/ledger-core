package com.example.ledgercore.transaction.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.transaction.command.dto.ConfirmTransferCommand;
import com.example.ledgercore.transaction.command.port.outbound.TransferOtpPort;
import com.example.ledgercore.transaction.command.repository.TransferIntentCommandRepository;
import com.example.ledgercore.transaction.entity.TransferIntent;
import com.example.ledgercore.transaction.enums.TransferIntentStatus;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    private final Instant now =
            Instant.parse("2026-08-27T10:00:00Z");

    private UUID userId;
    private UUID intentId;
    private UUID sourceAccountId;
    private UUID destinationAccountId;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(
                now,
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
                        now.plusSeconds(300)
                );

        ConfirmTransferCommand command =
                new ConfirmTransferCommand(
                        intentId,
                        "123456"
                );

        TransactionResponse expected =
                mock(TransactionResponse.class);

        when(
                transferIntentCommandRepository
                        .findById(intentId)
        ).thenReturn(Optional.of(intent));

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

        assertSame(expected, actual);

        verify(
                transferOtpPort
        ).verifyConfirmationOtp(
                userId,
                intentId,
                "123456"
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
                new ConfirmTransferCommand(
                        intentId,
                        "123456"
                );

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
    void shouldThrowWhenUserIsNotOwner() {
        UUID ownerId = UUID.randomUUID();

        TransferIntent intent =
                createPendingIntent(
                        now.plusSeconds(300)
                );

        intent.setUserId(ownerId);

        ConfirmTransferCommand command =
                new ConfirmTransferCommand(
                        intentId,
                        "123456"
                );

        when(
                transferIntentCommandRepository
                        .findById(intentId)
        ).thenReturn(Optional.of(intent));

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
    void shouldThrowWhenIntentIsNotPending() {
        TransferIntent intent =
                createPendingIntent(
                        now.plusSeconds(300)
                );

        intent.setStatus(
                TransferIntentStatus.COMPLETED
        );

        ConfirmTransferCommand command =
                new ConfirmTransferCommand(
                        intentId,
                        "123456"
                );

        when(
                transferIntentCommandRepository
                        .findById(intentId)
        ).thenReturn(Optional.of(intent));

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
    void shouldThrowWhenIntentIsExpired() {
        TransferIntent intent =
                createPendingIntent(
                        now.minusSeconds(1)
                );

        ConfirmTransferCommand command =
                new ConfirmTransferCommand(
                        intentId,
                        "123456"
                );

        when(
                transferIntentCommandRepository
                        .findById(intentId)
        ).thenReturn(Optional.of(intent));

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
    void shouldNotCallExecutionWhenOtpVerificationFails() {
        TransferIntent intent =
                createPendingIntent(
                        now.plusSeconds(300)
                );

        ConfirmTransferCommand command =
                new ConfirmTransferCommand(
                        intentId,
                        "123456"
                );

        when(
                transferIntentCommandRepository
                        .findById(intentId)
        ).thenReturn(Optional.of(intent));

        doThrow(
                new BusinessException(
                        ErrorCode.INVALID_VERIFICATION_CODE
                )
        ).when(transferOtpPort)
                .verifyConfirmationOtp(
                        userId,
                        intentId,
                        "123456"
                );

        assertThrows(
                BusinessException.class,
                () -> handler.execute(
                        userId,
                        command
                )
        );

        verify(
                transferOtpPort
        ).verifyConfirmationOtp(
                userId,
                intentId,
                "123456"
        );

        verifyNoInteractions(
                confirmTransferExecutionService
        );
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
}