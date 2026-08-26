package com.example.ledgercore.account.command.handler;

import com.example.ledgercore.account.command.dto.BlockAccountCommand;
import com.example.ledgercore.account.command.repository.AccountCommandRepository;
import com.example.ledgercore.account.entity.Account;
import com.example.ledgercore.account.enums.AccountStatus;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlockAccountHandlerTest {

    @Mock
    private AccountCommandRepository accountCommandRepository;

    private BlockAccountHandler handler;

    private UUID userId;
    private UUID accountId;

    @BeforeEach
    void setUp() {
        handler = new BlockAccountHandler(
                accountCommandRepository
        );

        userId = UUID.randomUUID();
        accountId = UUID.randomUUID();
    }

    @Test
    void shouldBlockActiveAccount() {
        Account account = createAccount(
                accountId,
                userId,
                AccountStatus.ACTIVE
        );

        when(accountCommandRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        BlockAccountCommand command =
                new BlockAccountCommand(
                        userId,
                        accountId
                );

        handler.execute(command);

        assertEquals(
                AccountStatus.BLOCKED,
                account.getStatus()
        );

        verify(accountCommandRepository)
                .save(account);
    }

    @Test
    void shouldThrowWhenAccountNotFound() {
        when(accountCommandRepository.findById(accountId))
                .thenReturn(Optional.empty());

        BlockAccountCommand command =
                new BlockAccountCommand(
                        userId,
                        accountId
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.ACCOUNT_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(accountCommandRepository, never())
                .save(any(Account.class));
    }

    @Test
    void shouldThrowWhenUserDoesNotOwnAccount() {
        UUID ownerId = UUID.randomUUID();

        Account account = createAccount(
                accountId,
                ownerId,
                AccountStatus.ACTIVE
        );

        when(accountCommandRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        BlockAccountCommand command =
                new BlockAccountCommand(
                        userId,
                        accountId
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.ACCESS_DENIED,
                exception.getErrorCode()
        );

        verify(accountCommandRepository, never())
                .save(any(Account.class));
    }

    @Test
    void shouldThrowWhenAccountIsClosed() {
        Account account = createAccount(
                accountId,
                userId,
                AccountStatus.CLOSED
        );

        when(accountCommandRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        BlockAccountCommand command =
                new BlockAccountCommand(
                        userId,
                        accountId
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.ACCOUNT_ALREADY_CLOSED,
                exception.getErrorCode()
        );

        verify(accountCommandRepository, never())
                .save(any(Account.class));
    }

    @Test
    void shouldThrowWhenAccountIsAlreadyBlocked() {
        Account account = createAccount(
                accountId,
                userId,
                AccountStatus.BLOCKED
        );

        when(accountCommandRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        BlockAccountCommand command =
                new BlockAccountCommand(
                        userId,
                        accountId
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.ACCOUNT_ALREADY_BLOCKED,
                exception.getErrorCode()
        );

        verify(accountCommandRepository, never())
                .save(any(Account.class));
    }

    private Account createAccount(
            UUID accountId,
            UUID userId,
            AccountStatus status
    ) {
        return Account.builder()
                .id(accountId)
                .userId(userId)
                .status(status)
                .build();
    }
}