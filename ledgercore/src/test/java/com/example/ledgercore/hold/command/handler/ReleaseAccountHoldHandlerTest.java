package com.example.ledgercore.hold.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.hold.command.dto.ReleaseAccountHoldCommand;
import com.example.ledgercore.hold.command.port.outbound.AccountHoldPort;
import com.example.ledgercore.hold.command.repository.AccountHoldCommandRepository;
import com.example.ledgercore.hold.entity.AccountHold;
import com.example.ledgercore.hold.enums.AccountHoldReferenceType;
import com.example.ledgercore.hold.enums.AccountHoldStatus;
import com.example.ledgercore.hold.enums.AccountHoldType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReleaseAccountHoldHandlerTest {

    @Mock
    private AccountHoldCommandRepository
            accountHoldCommandRepository;

    @Mock
    private AccountHoldPort accountHoldPort;

    private ReleaseAccountHoldHandler handler;

    private UUID holdId;
    private UUID accountId;
    private UUID referenceId;

    @BeforeEach
    void setUp() {
        handler = new ReleaseAccountHoldHandler(
                accountHoldCommandRepository,
                accountHoldPort
        );

        holdId = UUID.randomUUID();
        accountId = UUID.randomUUID();
        referenceId = UUID.randomUUID();
    }

    @Test
    void shouldReleaseAccountHoldSuccessfully() {

        AccountHold hold = activeHold();

        when(accountHoldCommandRepository.findById(holdId))
                .thenReturn(Optional.of(hold));

        handler.execute(
                new ReleaseAccountHoldCommand(holdId)
        );

        assertEquals(
                AccountHoldStatus.RELEASED,
                hold.getStatus()
        );

        assertNotNull(
                hold.getReleasedAt()
        );

        verify(accountHoldCommandRepository)
                .findById(holdId);

        verify(accountHoldPort)
                .decreaseHold(
                        accountId,
                        new BigDecimal("100000"),
                        "VND"
                );
    }

    @Test
    void shouldDecreaseAccountHoldBeforeMarkingAsReleased() {

        AccountHold hold = activeHold();

        when(accountHoldCommandRepository.findById(holdId))
                .thenReturn(Optional.of(hold));

        doAnswer(invocation -> {

            assertEquals(
                    AccountHoldStatus.ACTIVE,
                    hold.getStatus()
            );

            assertNull(
                    hold.getReleasedAt()
            );

            return null;

        }).when(accountHoldPort)
                .decreaseHold(
                        accountId,
                        new BigDecimal("100000"),
                        "VND"
                );

        handler.execute(
                new ReleaseAccountHoldCommand(holdId)
        );

        assertEquals(
                AccountHoldStatus.RELEASED,
                hold.getStatus()
        );

        assertNotNull(
                hold.getReleasedAt()
        );

        verify(accountHoldPort)
                .decreaseHold(
                        accountId,
                        new BigDecimal("100000"),
                        "VND"
                );
    }

    @Test
    void shouldUseHoldAccountAndAmountWhenDecreasing() {

        AccountHold hold = activeHold();

        when(accountHoldCommandRepository.findById(holdId))
                .thenReturn(Optional.of(hold));

        handler.execute(
                new ReleaseAccountHoldCommand(holdId)
        );

        ArgumentCaptor<UUID> accountIdCaptor =
                ArgumentCaptor.forClass(UUID.class);

        ArgumentCaptor<BigDecimal> amountCaptor =
                ArgumentCaptor.forClass(BigDecimal.class);

        ArgumentCaptor<String> currencyCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(accountHoldPort)
                .decreaseHold(
                        accountIdCaptor.capture(),
                        amountCaptor.capture(),
                        currencyCaptor.capture()
                );

        assertEquals(
                accountId,
                accountIdCaptor.getValue()
        );

        assertEquals(
                new BigDecimal("100000"),
                amountCaptor.getValue()
        );

        assertEquals(
                "VND",
                currencyCaptor.getValue()
        );
    }

    @Test
    void shouldSetReleasedAtWhenReleased() {

        AccountHold hold = activeHold();

        when(accountHoldCommandRepository.findById(holdId))
                .thenReturn(Optional.of(hold));

        Instant before = Instant.now();

        handler.execute(
                new ReleaseAccountHoldCommand(holdId)
        );

        Instant releasedAt = hold.getReleasedAt();

        Instant after = Instant.now();

        assertNotNull(releasedAt);

        assertFalse(
                releasedAt.isBefore(before)
        );

        assertFalse(
                releasedAt.isAfter(after)
        );
    }

    @Test
    void shouldThrowWhenCommandIsNull() {

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(null)
                );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                accountHoldCommandRepository,
                accountHoldPort
        );
    }

    @Test
    void shouldThrowWhenHoldIdIsNull() {

        ReleaseAccountHoldCommand command =
                new ReleaseAccountHoldCommand(null);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                accountHoldCommandRepository,
                accountHoldPort
        );
    }

    @Test
    void shouldThrowWhenHoldDoesNotExist() {

        when(accountHoldCommandRepository.findById(holdId))
                .thenReturn(Optional.empty());

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                new ReleaseAccountHoldCommand(holdId)
                        )
                );

        assertEquals(
                ErrorCode.ACCOUNT_HOLD_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(accountHoldCommandRepository)
                .findById(holdId);

        verifyNoInteractions(accountHoldPort);
    }

    @Test
    void shouldThrowWhenHoldIsReleased() {

        AccountHold hold = activeHold();
        hold.setStatus(AccountHoldStatus.RELEASED);
        hold.setReleasedAt(Instant.now());

        when(accountHoldCommandRepository.findById(holdId))
                .thenReturn(Optional.of(hold));

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                new ReleaseAccountHoldCommand(holdId)
                        )
                );

        assertEquals(
                ErrorCode.ACCOUNT_HOLD_NOT_ACTIVE,
                exception.getErrorCode()
        );

        verify(accountHoldCommandRepository)
                .findById(holdId);

        verifyNoInteractions(accountHoldPort);
    }

    @Test
    void shouldThrowWhenHoldIsExpired() {

        AccountHold hold = activeHold();
        hold.setStatus(AccountHoldStatus.EXPIRED);

        when(accountHoldCommandRepository.findById(holdId))
                .thenReturn(Optional.of(hold));

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                new ReleaseAccountHoldCommand(holdId)
                        )
                );

        assertEquals(
                ErrorCode.ACCOUNT_HOLD_NOT_ACTIVE,
                exception.getErrorCode()
        );

        verifyNoInteractions(accountHoldPort);
    }

    @Test
    void shouldThrowWhenHoldIsCancelled() {

        AccountHold hold = activeHold();
        hold.setStatus(AccountHoldStatus.CANCELLED);

        when(accountHoldCommandRepository.findById(holdId))
                .thenReturn(Optional.of(hold));

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                new ReleaseAccountHoldCommand(holdId)
                        )
                );

        assertEquals(
                ErrorCode.ACCOUNT_HOLD_NOT_ACTIVE,
                exception.getErrorCode()
        );

        verifyNoInteractions(accountHoldPort);
    }

    @Test
    void shouldNotReleaseInactiveHold() {

        AccountHold hold = activeHold();
        hold.setStatus(AccountHoldStatus.RELEASED);

        when(accountHoldCommandRepository.findById(holdId))
                .thenReturn(Optional.of(hold));

        assertThrows(
                BusinessException.class,
                () -> handler.execute(
                        new ReleaseAccountHoldCommand(holdId)
                )
        );

        verify(accountHoldPort, never())
                .decreaseHold(
                        any(),
                        any(),
                        any()
                );

        assertEquals(
                AccountHoldStatus.RELEASED,
                hold.getStatus()
        );
    }

    @Test
    void shouldNotChangeHoldWhenDecreaseFails() {

        AccountHold hold = activeHold();

        when(accountHoldCommandRepository.findById(holdId))
                .thenReturn(Optional.of(hold));

        doThrow(new RuntimeException("Decrease failed"))
                .when(accountHoldPort)
                .decreaseHold(
                        accountId,
                        new BigDecimal("100000"),
                        "VND"
                );

        assertThrows(
                RuntimeException.class,
                () -> handler.execute(
                        new ReleaseAccountHoldCommand(holdId)
                )
        );

        assertEquals(
                AccountHoldStatus.ACTIVE,
                hold.getStatus()
        );

        assertNull(
                hold.getReleasedAt()
        );
    }

    private AccountHold activeHold() {

        return AccountHold.builder()
                .id(holdId)
                .accountId(accountId)
                .amount(new BigDecimal("100000"))
                .currency("VND")
                .holdType(
                        AccountHoldType
                                .AVAILABLE_BALANCE_RESERVATION
                )
                .referenceType(
                        AccountHoldReferenceType
                                .WITHDRAWAL_INTENT
                )
                .referenceId(referenceId)
                .status(AccountHoldStatus.ACTIVE)
                .build();
    }
}