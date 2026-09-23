package com.example.ledgercore.account.command.handler;

import com.example.ledgercore.account.command.dto.CloseUserAccountCommand;
import com.example.ledgercore.account.command.repository.AccountCommandRepository;
import com.example.ledgercore.account.enums.AccountStatus;
import com.example.ledgercore.account.query.service.GetUserAccountService;
import com.example.ledgercore.account.query.service.dto.GetUserAccountQuery;
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
class CloseUserAccountHandlerTest {

    @Mock
    private GetUserAccountService getUserAccountService;

    @Mock
    private AccountCommandRepository accountCommandRepository;

    private CloseUserAccountHandler handler;

    private UUID userId;
    private UUID accountId;
    private UUID productId;
    private UUID ledgerAccountId;

    @BeforeEach
    void setUp() {
        handler = new CloseUserAccountHandler(
                getUserAccountService,
                accountCommandRepository
        );

        userId = UUID.randomUUID();
        accountId = UUID.randomUUID();
        productId = UUID.randomUUID();
        ledgerAccountId = UUID.randomUUID();
    }

    @Test
    void shouldCloseAccountWhenBalanceIsZero() {
        GetUserAccountResult account = createAccount(
                accountId,
                userId,
                AccountStatus.ACTIVE,
                BigDecimal.ZERO
        );

        when(getUserAccountService.execute(
                new GetUserAccountQuery(accountId)
        )).thenReturn(account);

        CloseUserAccountCommand command =
                new CloseUserAccountCommand(
                        userId,
                        accountId
                );

        handler.execute(command);

        verify(accountCommandRepository)
                .updateStatus(
                        accountId,
                        AccountStatus.CLOSED
                );
    }

    @Test
    void shouldThrowWhenUserDoesNotOwnAccount() {
        UUID ownerId = UUID.randomUUID();

        GetUserAccountResult account = createAccount(
                accountId,
                ownerId,
                AccountStatus.ACTIVE,
                BigDecimal.ZERO
        );

        when(getUserAccountService.execute(
                new GetUserAccountQuery(accountId)
        )).thenReturn(account);

        CloseUserAccountCommand command =
                new CloseUserAccountCommand(
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
                        AccountStatus.CLOSED
                );
    }

    @Test
    void shouldThrowWhenAccountIsAlreadyClosed() {
        GetUserAccountResult account = createAccount(
                accountId,
                userId,
                AccountStatus.CLOSED,
                BigDecimal.ZERO
        );

        when(getUserAccountService.execute(
                new GetUserAccountQuery(accountId)
        )).thenReturn(account);

        CloseUserAccountCommand command =
                new CloseUserAccountCommand(
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
                        AccountStatus.CLOSED
                );
    }

    @Test
    void shouldThrowWhenAccountHasNonZeroBalance() {
        GetUserAccountResult account = createAccount(
                accountId,
                userId,
                AccountStatus.ACTIVE,
                new BigDecimal("100.00")
        );

        when(getUserAccountService.execute(
                new GetUserAccountQuery(accountId)
        )).thenReturn(account);

        CloseUserAccountCommand command =
                new CloseUserAccountCommand(
                        userId,
                        accountId
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.ACCOUNT_BALANCE_NOT_ZERO,
                exception.getErrorCode()
        );

        verify(accountCommandRepository, never())
                .updateStatus(
                        accountId,
                        AccountStatus.CLOSED
                );
    }

    @Test
    void shouldQueryAccountByAccountId() {
        GetUserAccountResult account = createAccount(
                accountId,
                userId,
                AccountStatus.ACTIVE,
                BigDecimal.ZERO
        );

        when(getUserAccountService.execute(
                new GetUserAccountQuery(accountId)
        )).thenReturn(account);

        CloseUserAccountCommand command =
                new CloseUserAccountCommand(
                        userId,
                        accountId
                );

        handler.execute(command);

        verify(getUserAccountService)
                .execute(
                        new GetUserAccountQuery(accountId)
                );
    }

    @Test
    void shouldNotUpdateStatusWhenClosingIsRejected() {
        GetUserAccountResult account = createAccount(
                accountId,
                userId,
                AccountStatus.ACTIVE,
                new BigDecimal("100.00")
        );

        when(getUserAccountService.execute(
                new GetUserAccountQuery(accountId)
        )).thenReturn(account);

        CloseUserAccountCommand command =
                new CloseUserAccountCommand(
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
                        AccountStatus.CLOSED
                );
    }

    private GetUserAccountResult createAccount(
            UUID accountId,
            UUID userId,
            AccountStatus status,
            BigDecimal balance
    ) {
        return new GetUserAccountResult(
                accountId,
                userId,
                productId,
                "0123456789",
                Currency.VND,
                balance,
                BigDecimal.ZERO,
                status,
                1L,
                ledgerAccountId,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z")
        );
    }
}