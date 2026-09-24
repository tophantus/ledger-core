package com.example.ledgercore.transfer.command.handler;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.otp.enums.OtpPurpose;
import com.example.ledgercore.transaction.command.port.outbound.TransferOtpPort;
import com.example.ledgercore.transaction.command.port.outbound.TransferUserAccountPort;
import com.example.ledgercore.transfer.command.dto.CreateTransferIntentCommand;
import com.example.ledgercore.transfer.command.dto.CreateTransferIntentResult;
import com.example.ledgercore.transfer.command.repository.TransferIntentCommandRepository;
import com.example.ledgercore.transfer.entity.TransferIntent;
import com.example.ledgercore.transfer.enums.TransferIntentStatus;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateTransferIntentHandlerTest {

    @Mock
    private TransferUserAccountPort transferUserAccountPort;

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

    private static final BigDecimal TRANSFER_AMOUNT =
            new BigDecimal("100000");

    private static final String DESTINATION_ACCOUNT_NO =
            "0987654321";

    private static final String REFERENCE =
            "REF-001";

    private static final String DESCRIPTION =
            "Test transfer";

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(
                NOW,
                ZoneOffset.UTC
        );

        handler = new CreateTransferIntentHandler(
                transferUserAccountPort,
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

        mockAvailableTransfer();

        when(
                transferIntentCommandRepository.save(
                        any(TransferIntent.class)
                )
        ).thenAnswer(invocation ->
        {
            TransferIntent intent =
                    invocation.getArgument(0);

            return TransferIntent.builder()
                    .id(intentId)
                    .userId(intent.getUserId())
                    .sourceAccountId(
                            intent.getSourceAccountId()
                    )
                    .destinationAccountId(
                            intent.getDestinationAccountId()
                    )
                    .amount(intent.getAmount())
                    .currency(intent.getCurrency())
                    .reference(intent.getReference())
                    .description(intent.getDescription())
                    .status(intent.getStatus())
                    .expiresAt(intent.getExpiresAt())
                    .createdAt(intent.getCreatedAt())
                    .build();
        });

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
                TRANSFER_AMOUNT,
                result.amount()
        );

        assertEquals(
                Currency.VND,
                result.currency()
        );

        assertEquals(
                REFERENCE,
                result.reference()
        );

        assertEquals(
                TransferIntentStatus.PENDING,
                result.status()
        );

        assertEquals(
                expectedExpiration(),
                result.expiresAt()
        );

        assertEquals(
                NOW,
                result.createdAt()
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

        mockAvailableTransfer();

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
                expectedExpiration(),
                intent.getExpiresAt()
        );
    }

    @Test
    void shouldReturnExistingIntentWhenReferenceAlreadyExists() {
        CreateTransferIntentCommand command =
                createCommand();

        TransferIntent existingIntent =
                createPendingIntent(
                        intentId,
                        userId
                );

        when(
                transferIntentCommandRepository
                        .findByReference(REFERENCE)
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
                TRANSFER_AMOUNT,
                result.amount()
        );

        assertEquals(
                Currency.VND,
                result.currency()
        );

        assertEquals(
                REFERENCE,
                result.reference()
        );

        assertEquals(
                TransferIntentStatus.PENDING,
                result.status()
        );

        assertEquals(
                existingIntent.getExpiresAt(),
                result.expiresAt()
        );

        assertEquals(
                existingIntent.getCreatedAt(),
                result.createdAt()
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
                transferUserAccountPort,
                never()
        ).getAccountIdByAccountNo(any());

        verify(
                transferUserAccountPort,
                never()
        ).getTransferInfo(
                any(),
                any(),
                any()
        );
    }

    @Test
    void shouldThrowInvalidCurrencyAmountWhenAmountScaleExceedsCurrencyScale() {
        CreateTransferIntentCommand command =
                new CreateTransferIntentCommand(
                        sourceAccountId,
                        DESTINATION_ACCOUNT_NO,
                        new BigDecimal("100.1"),
                        Currency.VND,
                        REFERENCE,
                        DESCRIPTION
                );

        mockReferenceNotFound();

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
                transferUserAccountPort,
                transferOtpPort
        );

        verify(
                transferIntentCommandRepository,
                never()
        ).save(any());
    }

    @Test
    void shouldThrowAccessDeniedWhenReferenceBelongsToAnotherUser() {
        CreateTransferIntentCommand command =
                createCommand();

        UUID anotherUserId =
                UUID.randomUUID();

        TransferIntent existingIntent =
                createPendingIntent(
                        intentId,
                        anotherUserId
                );

        when(
                transferIntentCommandRepository
                        .findByReference(REFERENCE)
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
                transferUserAccountPort,
                transferOtpPort
        );
    }

    @Test
    void shouldThrowInvalidTransferAmountWhenAmountIsZero() {
        CreateTransferIntentCommand command =
                new CreateTransferIntentCommand(
                        sourceAccountId,
                        DESTINATION_ACCOUNT_NO,
                        BigDecimal.ZERO,
                        Currency.VND,
                        REFERENCE,
                        DESCRIPTION
                );

        mockReferenceNotFound();

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
                transferUserAccountPort,
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
                        DESTINATION_ACCOUNT_NO,
                        new BigDecimal("-100"),
                        Currency.VND,
                        REFERENCE,
                        DESCRIPTION
                );

        mockReferenceNotFound();

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
                transferUserAccountPort,
                transferOtpPort
        );

        verify(
                transferIntentCommandRepository,
                never()
        ).save(any());
    }

    @Test
    void shouldThrowSameAccountTransferWhenSourceAndDestinationAreSame() {
        CreateTransferIntentCommand command =
                createCommand();

        mockReferenceNotFound();

        when(
                transferUserAccountPort.getAccountIdByAccountNo(
                        DESTINATION_ACCOUNT_NO
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
                transferUserAccountPort
        ).getAccountIdByAccountNo(
                DESTINATION_ACCOUNT_NO
        );

        verify(
                transferUserAccountPort,
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

        TransferUserAccountPort.TransferAccountInfo transferInfo =
                createTransferInfo(
                        Currency.USD,
                        new BigDecimal("1000000")
                );

        mockReferenceNotFound();

        when(
                transferUserAccountPort.getAccountIdByAccountNo(
                        DESTINATION_ACCOUNT_NO
                )
        ).thenReturn(destinationAccountId);

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

        TransferUserAccountPort.TransferAccountInfo transferInfo =
                createTransferInfo(
                        Currency.VND,
                        new BigDecimal("50000")
                );

        mockReferenceNotFound();

        when(
                transferUserAccountPort.getAccountIdByAccountNo(
                        DESTINATION_ACCOUNT_NO
                )
        ).thenReturn(destinationAccountId);

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

        mockAvailableTransfer();

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

    private void mockReferenceNotFound() {
        when(
                transferIntentCommandRepository
                        .findByReference(REFERENCE)
        ).thenReturn(Optional.empty());
    }

    private void mockAvailableTransfer() {
        mockReferenceNotFound();

        when(
                transferUserAccountPort.getAccountIdByAccountNo(
                        DESTINATION_ACCOUNT_NO
                )
        ).thenReturn(destinationAccountId);

        when(
                transferUserAccountPort.getTransferInfo(
                        userId,
                        sourceAccountId,
                        destinationAccountId
                )
        ).thenReturn(
                createTransferInfo(
                        Currency.VND,
                        new BigDecimal("1000000")
                )
        );
    }

    private TransferUserAccountPort.TransferAccountInfo
    createTransferInfo(
            Currency currency,
            BigDecimal balance
    ) {
        return new TransferUserAccountPort.TransferAccountInfo(
                sourceAccountId,
                destinationAccountId,
                currency,
                balance
        );
    }

    private TransferIntent createPendingIntent(
            UUID id,
            UUID ownerId
    ) {
        return TransferIntent.builder()
                .id(id)
                .userId(ownerId)
                .sourceAccountId(sourceAccountId)
                .destinationAccountId(destinationAccountId)
                .amount(TRANSFER_AMOUNT)
                .currency(Currency.VND)
                .reference(REFERENCE)
                .description(DESCRIPTION)
                .status(TransferIntentStatus.PENDING)
                .expiresAt(NOW.plusSeconds(300))
                .createdAt(NOW)
                .build();
    }

    private CreateTransferIntentCommand createCommand() {
        return new CreateTransferIntentCommand(
                sourceAccountId,
                DESTINATION_ACCOUNT_NO,
                TRANSFER_AMOUNT,
                Currency.VND,
                REFERENCE,
                DESCRIPTION
        );
    }

    private Instant expectedExpiration() {
        return NOW.plus(
                OtpPurpose.CONFIRM_TRANSFER
                        .getExpiration()
        );
    }
}