package com.example.ledgercore.account.command.handler;

import com.example.ledgercore.account.command.dto.BlockUserAccountCommand;
import com.example.ledgercore.account.command.repository.AccountCommandRepository;
import com.example.ledgercore.account.enums.AccountStatus;
import com.example.ledgercore.account.query.service.GetUserAccountService;
import com.example.ledgercore.account.query.service.dto.GetUserAccountCriteria;
import com.example.ledgercore.account.query.service.dto.GetUserAccountResult;
import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlockUserAccountHandlerTest {

    @Mock
    private GetUserAccountService getUserAccountService;

    @Mock
    private AccountCommandRepository accountCommandRepository;

    private BlockUserAccountHandler handler;

    private UUID userId;
    private UUID accountId;
    private UUID productId;
    private UUID ledgerAccountId;

    @BeforeEach
    void setUp() {
        handler = new BlockUserAccountHandler(
                getUserAccountService,
                accountCommandRepository
        );

        userId = UUID.randomUUID();
        accountId = UUID.randomUUID();
        productId = UUID.randomUUID();
        ledgerAccountId = UUID.randomUUID();
    }

    @Test
    void shouldBlockActiveAccount() {
        GetUserAccountResult account = createAccount(
                accountId,
                userId,
                AccountStatus.ACTIVE
        );

        when(getUserAccountService.execute(
                new GetUserAccountCriteria(accountId)
        )).thenReturn(account);

        BlockUserAccountCommand command =
                new BlockUserAccountCommand(
                        userId,
                        accountId
                );

        handler.execute(command);

        verify(accountCommandRepository)
                .updateStatus(
                        accountId,
                        AccountStatus.BLOCKED
                );
    }

    @Test
    void shouldThrowWhenUserDoesNotOwnAccount() {
        UUID ownerId = UUID.randomUUID();

        GetUserAccountResult account = createAccount(
                accountId,
                ownerId,
                AccountStatus.ACTIVE
        );

        when(getUserAccountService.execute(
                new GetUserAccountCriteria(accountId)
        )).thenReturn(account);

        BlockUserAccountCommand command =
                new BlockUserAccountCommand(
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
                .updateStatus(
                        accountId,
                        AccountStatus.BLOCKED
                );
    }

    @Test
    void shouldThrowWhenAccountIsClosed() {
        GetUserAccountResult account = createAccount(
                accountId,
                userId,
                AccountStatus.CLOSED
        );

        when(getUserAccountService.execute(
                new GetUserAccountCriteria(accountId)
        )).thenReturn(account);

        BlockUserAccountCommand command =
                new BlockUserAccountCommand(
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
                .updateStatus(
                        accountId,
                        AccountStatus.BLOCKED
                );
    }

    @Test
    void shouldThrowWhenAccountIsAlreadyBlocked() {
        GetUserAccountResult account = createAccount(
                accountId,
                userId,
                AccountStatus.BLOCKED
        );

        when(getUserAccountService.execute(
                new GetUserAccountCriteria(accountId)
        )).thenReturn(account);

        BlockUserAccountCommand command =
                new BlockUserAccountCommand(
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
                .updateStatus(
                        accountId,
                        AccountStatus.BLOCKED
                );
    }

    @Test
    void shouldQueryAccountByAccountId() {
        GetUserAccountResult account = createAccount(
                accountId,
                userId,
                AccountStatus.ACTIVE
        );

        when(getUserAccountService.execute(
                new GetUserAccountCriteria(accountId)
        )).thenReturn(account);

        BlockUserAccountCommand command =
                new BlockUserAccountCommand(
                        userId,
                        accountId
                );

        handler.execute(command);

        verify(getUserAccountService)
                .execute(
                        new GetUserAccountCriteria(accountId)
                );
    }

    @Test
    void shouldNotUpdateStatusWhenBlockingIsRejected() {
        GetUserAccountResult account = createAccount(
                accountId,
                userId,
                AccountStatus.CLOSED
        );

        when(getUserAccountService.execute(
                new GetUserAccountCriteria(accountId)
        )).thenReturn(account);

        BlockUserAccountCommand command =
                new BlockUserAccountCommand(
                        userId,
                        accountId
                );

        assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        verify(accountCommandRepository, never())
                .updateStatus(
                        accountId,
                        AccountStatus.BLOCKED
                );
    }

    private GetUserAccountResult createAccount(
            UUID accountId,
            UUID userId,
            AccountStatus status
    ) {
        return new GetUserAccountResult(
                accountId,
                userId,
                productId,
                "0123456789",
                Currency.VND,
                new BigDecimal("1000000.00"),
                new BigDecimal("100000.00"),
                status,
                1L,
                ledgerAccountId,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z")
        );
    }
}