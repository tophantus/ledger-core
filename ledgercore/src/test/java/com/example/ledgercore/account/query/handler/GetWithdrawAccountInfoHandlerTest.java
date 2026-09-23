package com.example.ledgercore.account.query.handler;

import com.example.ledgercore.account.enums.AccountStatus;
import com.example.ledgercore.account.query.dto.AccountWithdrawInfo;
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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetWithdrawAccountInfoHandlerTest {

    @Mock
    private GetUserAccountService getUserAccountService;

    private GetWithdrawAccountInfoHandler handler;

    private UUID userId;
    private UUID accountId;
    private UUID productId;
    private UUID ledgerAccountId;

    @BeforeEach
    void setUp() {
        handler = new GetWithdrawAccountInfoHandler(
                getUserAccountService
        );

        userId = UUID.randomUUID();
        accountId = UUID.randomUUID();
        productId = UUID.randomUUID();
        ledgerAccountId = UUID.randomUUID();
    }

    @Test
    void shouldReturnWithdrawInfoWhenAccountIsActive() {
        BigDecimal balance =
                new BigDecimal("1000000");

        BigDecimal holdAmount =
                new BigDecimal("200000");

        BigDecimal expectedAvailableBalance =
                new BigDecimal("800000");

        GetUserAccountResult account =
                account(
                        AccountStatus.ACTIVE,
                        Currency.VND,
                        balance,
                        holdAmount
                );

        givenAccount(account);

        AccountWithdrawInfo response =
                handler.execute(accountId);

        assertAll(
                () -> assertEquals(
                        accountId,
                        response.accountId()
                ),
                () -> assertEquals(
                        userId,
                        response.userId()
                ),
                () -> assertEquals(
                        Currency.VND,
                        response.currency()
                ),
                () -> assertEquals(
                        expectedAvailableBalance,
                        response.availableBalance()
                )
        );

        verify(getUserAccountService)
                .execute(
                        new GetUserAccountQuery(
                                accountId
                        )
                );

        verifyNoMoreInteractions(
                getUserAccountService
        );
    }

    @Test
    void shouldThrowWhenAccountNotFound() {
        when(getUserAccountService.execute(
                new GetUserAccountQuery(accountId)
        )).thenThrow(
                new BusinessException(
                        ErrorCode.ACCOUNT_NOT_FOUND
                )
        );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(accountId)
                );

        assertEquals(
                ErrorCode.ACCOUNT_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(getUserAccountService)
                .execute(
                        new GetUserAccountQuery(
                                accountId
                        )
                );

        verifyNoMoreInteractions(
                getUserAccountService
        );
    }

    @Test
    void shouldThrowWhenAccountIsBlocked() {
        GetUserAccountResult account =
                account(
                        AccountStatus.BLOCKED,
                        Currency.VND,
                        new BigDecimal("1000000"),
                        new BigDecimal("200000")
                );

        givenAccount(account);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(accountId)
                );

        assertEquals(
                ErrorCode.ACCOUNT_NOT_ACTIVE,
                exception.getErrorCode()
        );

        verify(getUserAccountService)
                .execute(
                        new GetUserAccountQuery(
                                accountId
                        )
                );

        verifyNoMoreInteractions(
                getUserAccountService
        );
    }

    @Test
    void shouldThrowWhenAccountIsClosed() {
        GetUserAccountResult account =
                account(
                        AccountStatus.CLOSED,
                        Currency.VND,
                        new BigDecimal("1000000"),
                        BigDecimal.ZERO
                );

        givenAccount(account);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(accountId)
                );

        assertEquals(
                ErrorCode.ACCOUNT_NOT_ACTIVE,
                exception.getErrorCode()
        );

        verify(getUserAccountService)
                .execute(
                        new GetUserAccountQuery(
                                accountId
                        )
                );

        verifyNoMoreInteractions(
                getUserAccountService
        );
    }

    @Test
    void shouldReturnZeroAvailableBalanceWhenBalanceEqualsHoldAmount() {
        GetUserAccountResult account =
                account(
                        AccountStatus.ACTIVE,
                        Currency.VND,
                        new BigDecimal("500000"),
                        new BigDecimal("500000")
                );

        givenAccount(account);

        AccountWithdrawInfo response =
                handler.execute(accountId);

        assertEquals(
                BigDecimal.ZERO,
                response.availableBalance()
        );

        verify(getUserAccountService)
                .execute(
                        new GetUserAccountQuery(
                                accountId
                        )
                );
    }

    private void givenAccount(
            GetUserAccountResult account
    ) {
        when(getUserAccountService.execute(
                new GetUserAccountQuery(accountId)
        )).thenReturn(account);
    }

    private GetUserAccountResult account(
            AccountStatus status,
            Currency currency,
            BigDecimal balance,
            BigDecimal holdAmount
    ) {
        Instant now = Instant.now();

        return new GetUserAccountResult(
                accountId,
                userId,
                productId,
                "1000000001",
                currency,
                balance,
                holdAmount,
                status,
                0L,
                ledgerAccountId,
                now,
                now
        );
    }
}