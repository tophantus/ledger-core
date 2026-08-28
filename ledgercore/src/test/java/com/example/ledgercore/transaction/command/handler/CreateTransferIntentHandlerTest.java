package com.example.ledgercore.transaction.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.otp.enums.OtpPurpose;
import com.example.ledgercore.transaction.command.dto.CreateTransferIntentCommand;
import com.example.ledgercore.transaction.command.dto.CreateTransferIntentResult;
import com.example.ledgercore.transaction.command.port.outbound.AccountTransferPort;
import com.example.ledgercore.transaction.command.port.outbound.TransferOtpPort;
import com.example.ledgercore.transaction.command.repository.TransferIntentCommandRepository;
import com.example.ledgercore.transaction.entity.TransferIntent;
import com.example.ledgercore.transaction.enums.TransferIntentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class CreateTransferIntentHandlerTest {

    @Mock
    private AccountTransferPort accountTransferPort;

    @Mock
    private TransferIntentCommandRepository
            transferIntentCommandRepository;

    @Mock
    private TransferOtpPort transferOtpPort;

    private Clock clock;

    private CreateTransferIntentHandler handler;

    private UUID userId;
    private UUID sourceAccountId;
    private UUID destinationAccountId;
    private UUID intentId;

    private static final Instant NOW =
            Instant.parse("2026-08-27T10:00:00Z");

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(
                NOW,
                ZoneOffset.UTC
        );

        handler = new CreateTransferIntentHandler(
                accountTransferPort,
                transferIntentCommandRepository,
                transferOtpPort,
                clock
        );

        userId = UUID.randomUUID();
        sourceAccountId = UUID.randomUUID();
        destinationAccountId = UUID.randomUUID();
        intentId = UUID.randomUUID();
    }

    @Test
    void shouldCreateTransferIntentSuccessfully() {
        CreateTransferIntentCommand command =
                createCommand();

        AccountTransferPort.TransferAccountInfo transferInfo =
                new AccountTransferPort.TransferAccountInfo(
                        sourceAccountId,
                        destinationAccountId,
                        "VND",
                        new BigDecimal("1000000")
                );

        TransferIntent savedIntent =
                TransferIntent.builder()
                        .id(intentId)
                        .userId(userId)
                        .sourceAccountId(sourceAccountId)
                        .destinationAccountId(destinationAccountId)
                        .amount(new BigDecimal("100000"))
                        .currency("VND")
                        .reference("REF-001")
                        .description("Test transfer")
                        .status(TransferIntentStatus.PENDING)
                        .expiresAt(
                                NOW.plus(
                                        OtpPurpose.CONFIRM_TRANSFER
                                                .getExpiration()
                                )
                        )
                        .createdAt(NOW)
                        .build();

        when(
                transferIntentCommandRepository
                        .findByReference("REF-001")
        ).thenReturn(Optional.empty());

        when(
                accountTransferPort.getAccountIdByAccountNo(
                        "0987654321"
                )
        ).thenReturn(destinationAccountId);

        when(
                accountTransferPort.getTransferInfo(
                        userId,
                        sourceAccountId,
                        destinationAccountId
                )
        ).thenReturn(transferInfo);

        when(
                transferIntentCommandRepository.save(
                        any(TransferIntent.class)
                )
        ).thenReturn(savedIntent);

        CreateTransferIntentResult result =
                handler.execute(
                        userId,
                        command
                );

        assertNotNull(result);

        assertEquals(
                intentId,
                result.intentId()
        );

        assertEquals(
                sourceAccountId,
                result.sourceAccountId()
        );

        assertEquals(
                destinationAccountId,
                result.destinationAccountId()
        );

        assertEquals(
                new BigDecimal("100000"),
                result.amount()
        );

        assertEquals(
                "VND",
                result.currency()
        );

        assertEquals(
                "REF-001",
                result.reference()
        );

        assertEquals(
                TransferIntentStatus.PENDING,
                result.status()
        );

        assertEquals(
                NOW.plus(
                        OtpPurpose.CONFIRM_TRANSFER
                                .getExpiration()
                ),
                result.expiresAt()
        );

        verify(
                transferOtpPort
        ).sendConfirmationOtp(
                userId,
                intentId
        );
    }

    @Test
    void shouldCreateIntentWithCorrectExpirationTime() {
        CreateTransferIntentCommand command =
                createCommand();

        AccountTransferPort.TransferAccountInfo transferInfo =
                new AccountTransferPort.TransferAccountInfo(
                        sourceAccountId,
                        destinationAccountId,
                        "VND",
                        new BigDecimal("1000000")
                );

        when(
                transferIntentCommandRepository
                        .findByReference("REF-001")
        ).thenReturn(Optional.empty());

        when(
                accountTransferPort.getAccountIdByAccountNo(
                        "0987654321"
                )
        ).thenReturn(destinationAccountId);

        when(
                accountTransferPort.getTransferInfo(
                        userId,
                        sourceAccountId,
                        destinationAccountId
                )
        ).thenReturn(transferInfo);

        when(
                transferIntentCommandRepository.save(
                        any(TransferIntent.class)
                )
        ).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        handler.execute(
                userId,
                command
        );

        ArgumentCaptor<TransferIntent> captor =
                ArgumentCaptor.forClass(
                        TransferIntent.class
                );

        verify(
                transferIntentCommandRepository
        ).save(captor.capture());

        TransferIntent intent =
                captor.getValue();

        assertEquals(
                NOW,
                intent.getCreatedAt()
        );

        assertEquals(
                NOW.plus(
                        OtpPurpose.CONFIRM_TRANSFER
                                .getExpiration()
                ),
                intent.getExpiresAt()
        );
    }

    @Test
    void shouldReturnExistingIntentWhenReferenceAlreadyExists() {
        CreateTransferIntentCommand command =
                createCommand();

        TransferIntent existingIntent =
                TransferIntent.builder()
                        .id(intentId)
                        .userId(userId)
                        .sourceAccountId(sourceAccountId)
                        .destinationAccountId(destinationAccountId)
                        .amount(new BigDecimal("100000"))
                        .currency("VND")
                        .reference("REF-001")
                        .description("Test transfer")
                        .status(TransferIntentStatus.PENDING)
                        .expiresAt(
                                NOW.plusSeconds(300)
                        )
                        .createdAt(NOW)
                        .build();

        when(
                transferIntentCommandRepository
                        .findByReference("REF-001")
        ).thenReturn(
                Optional.of(existingIntent)
        );

        CreateTransferIntentResult result =
                handler.execute(
                        userId,
                        command
                );

        assertEquals(
                intentId,
                result.intentId()
        );

        assertEquals(
                sourceAccountId,
                result.sourceAccountId()
        );

        assertEquals(
                destinationAccountId,
                result.destinationAccountId()
        );

        assertEquals(
                new BigDecimal("100000"),
                result.amount()
        );

        assertEquals(
                "VND",
                result.currency()
        );

        assertEquals(
                "REF-001",
                result.reference()
        );

        assertEquals(
                TransferIntentStatus.PENDING,
                result.status()
        );

        verify(
                transferIntentCommandRepository,
                never()
        ).save(any());

        verify(
                transferOtpPort,
                never()
        ).sendConfirmationOtp(
                any(),
                any()
        );

        verify(
                accountTransferPort,
                never()
        ).getAccountIdByAccountNo(any());

        verify(
                accountTransferPort,
                never()
        ).getTransferInfo(
                any(),
                any(),
                any()
        );
    }

    @Test
    void shouldThrowAccessDeniedWhenReferenceBelongsToAnotherUser() {
        CreateTransferIntentCommand command =
                createCommand();

        UUID anotherUserId =
                UUID.randomUUID();

        TransferIntent existingIntent =
                TransferIntent.builder()
                        .id(intentId)
                        .userId(anotherUserId)
                        .sourceAccountId(sourceAccountId)
                        .destinationAccountId(destinationAccountId)
                        .amount(new BigDecimal("100000"))
                        .currency("VND")
                        .reference("REF-001")
                        .status(TransferIntentStatus.PENDING)
                        .expiresAt(NOW.plusSeconds(300))
                        .createdAt(NOW)
                        .build();

        when(
                transferIntentCommandRepository
                        .findByReference("REF-001")
        ).thenReturn(
                Optional.of(existingIntent)
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
                ErrorCode.ACCESS_DENIED,
                exception.getErrorCode()
        );

        verify(
                transferIntentCommandRepository,
                never()
        ).save(any());

        verifyNoInteractions(
                accountTransferPort,
                transferOtpPort
        );
    }

    @Test
    void shouldThrowInvalidTransferAmountWhenAmountIsZero() {
        CreateTransferIntentCommand command =
                new CreateTransferIntentCommand(
                        sourceAccountId,
                        "0987654321",
                        BigDecimal.ZERO,
                        "VND",
                        "REF-001",
                        "Test transfer"
                );

        when(
                transferIntentCommandRepository
                        .findByReference("REF-001")
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
                ErrorCode.INVALID_TRANSFER_AMOUNT,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                accountTransferPort,
                transferOtpPort
        );

        verify(
                transferIntentCommandRepository,
                never()
        ).save(any());
    }

    @Test
    void shouldThrowInvalidTransferAmountWhenAmountIsNegative() {
        CreateTransferIntentCommand command =
                new CreateTransferIntentCommand(
                        sourceAccountId,
                        "0987654321",
                        new BigDecimal("-100"),
                        "VND",
                        "REF-001",
                        "Test transfer"
                );

        when(
                transferIntentCommandRepository
                        .findByReference("REF-001")
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
                ErrorCode.INVALID_TRANSFER_AMOUNT,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                accountTransferPort,
                transferOtpPort
        );
    }

    @Test
    void shouldThrowSameAccountTransferWhenSourceAndDestinationAreSame() {
        CreateTransferIntentCommand command =
                createCommand();

        when(
                transferIntentCommandRepository
                        .findByReference("REF-001")
        ).thenReturn(Optional.empty());

        when(
                accountTransferPort.getAccountIdByAccountNo(
                        "0987654321"
                )
        ).thenReturn(sourceAccountId);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                userId,
                                command
                        )
                );

        assertEquals(
                ErrorCode.SAME_ACCOUNT_TRANSFER,
                exception.getErrorCode()
        );

        verify(
                accountTransferPort
        ).getAccountIdByAccountNo(
                "0987654321"
        );

        verify(
                accountTransferPort,
                never()
        ).getTransferInfo(
                any(),
                any(),
                any()
        );

        verify(
                transferIntentCommandRepository,
                never()
        ).save(any());

        verifyNoInteractions(
                transferOtpPort
        );
    }

    @Test
    void shouldThrowCurrencyMismatchWhenCurrencyDoesNotMatch() {
        CreateTransferIntentCommand command =
                createCommand();

        AccountTransferPort.TransferAccountInfo transferInfo =
                new AccountTransferPort.TransferAccountInfo(
                        sourceAccountId,
                        destinationAccountId,
                        "USD",
                        new BigDecimal("1000000")
                );

        when(
                transferIntentCommandRepository
                        .findByReference("REF-001")
        ).thenReturn(Optional.empty());

        when(
                accountTransferPort.getAccountIdByAccountNo(
                        "0987654321"
                )
        ).thenReturn(destinationAccountId);

        when(
                accountTransferPort.getTransferInfo(
                        userId,
                        sourceAccountId,
                        destinationAccountId
                )
        ).thenReturn(transferInfo);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                userId,
                                command
                        )
                );

        assertEquals(
                ErrorCode.TRANSACTION_CURRENCY_MISMATCH,
                exception.getErrorCode()
        );

        verify(
                transferIntentCommandRepository,
                never()
        ).save(any());

        verifyNoInteractions(
                transferOtpPort
        );
    }

    @Test
    void shouldThrowInsufficientBalanceWhenBalanceIsNotEnough() {
        CreateTransferIntentCommand command =
                createCommand();

        AccountTransferPort.TransferAccountInfo transferInfo =
                new AccountTransferPort.TransferAccountInfo(
                        sourceAccountId,
                        destinationAccountId,
                        "VND",
                        new BigDecimal("50000")
                );

        when(
                transferIntentCommandRepository
                        .findByReference("REF-001")
        ).thenReturn(Optional.empty());

        when(
                accountTransferPort.getAccountIdByAccountNo(
                        "0987654321"
                )
        ).thenReturn(destinationAccountId);

        when(
                accountTransferPort.getTransferInfo(
                        userId,
                        sourceAccountId,
                        destinationAccountId
                )
        ).thenReturn(transferInfo);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                userId,
                                command
                        )
                );

        assertEquals(
                ErrorCode.ACCOUNT_INSUFFICIENT_BALANCE,
                exception.getErrorCode()
        );

        verify(
                transferIntentCommandRepository,
                never()
        ).save(any());

        verifyNoInteractions(
                transferOtpPort
        );
    }

    @Test
    void shouldNotSendOtpWhenSavingIntentFails() {
        CreateTransferIntentCommand command =
                createCommand();

        AccountTransferPort.TransferAccountInfo transferInfo =
                new AccountTransferPort.TransferAccountInfo(
                        sourceAccountId,
                        destinationAccountId,
                        "VND",
                        new BigDecimal("1000000")
                );

        when(
                transferIntentCommandRepository
                        .findByReference("REF-001")
        ).thenReturn(Optional.empty());

        when(
                accountTransferPort.getAccountIdByAccountNo(
                        "0987654321"
                )
        ).thenReturn(destinationAccountId);

        when(
                accountTransferPort.getTransferInfo(
                        userId,
                        sourceAccountId,
                        destinationAccountId
                )
        ).thenReturn(transferInfo);

        when(
                transferIntentCommandRepository.save(
                        any(TransferIntent.class)
                )
        ).thenThrow(
                new RuntimeException("Database error")
        );

        assertThrows(
                RuntimeException.class,
                () -> handler.execute(
                        userId,
                        command
                )
        );

        verify(
                transferOtpPort,
                never()
        ).sendConfirmationOtp(
                any(),
                any()
        );
    }

    private CreateTransferIntentCommand createCommand() {
        return new CreateTransferIntentCommand(
                sourceAccountId,
                "0987654321",
                new BigDecimal("100000"),
                "VND",
                "REF-001",
                "Test transfer"
        );
    }
}