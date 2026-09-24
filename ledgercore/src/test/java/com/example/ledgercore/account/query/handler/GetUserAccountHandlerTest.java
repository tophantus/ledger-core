package com.example.ledgercore.account.query.handler;

import com.example.ledgercore.account.query.dto.AccountResponse;
import com.example.ledgercore.account.query.dto.GetUserAccountQuery;
import com.example.ledgercore.account.query.service.GetUserAccountService;
import com.example.ledgercore.account.query.service.dto.GetUserAccountCriteria;
import com.example.ledgercore.account.query.service.dto.GetUserAccountResult;
import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.account.enums.AccountStatus;
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
class GetUserAccountHandlerTest {

    @Mock
    private GetUserAccountService getUserAccountService;

    private GetUserAccountHandler handler;

    private UUID userId;
    private UUID accountId;
    private UUID productId;
    private UUID ledgerAccountId;

    private String accountNo;

    @BeforeEach
    void setUp() {
        handler = new GetUserAccountHandler(
                getUserAccountService
        );

        userId = UUID.randomUUID();
        accountId = UUID.randomUUID();
        productId = UUID.randomUUID();
        ledgerAccountId = UUID.randomUUID();

        accountNo = "1000000001";
    }

    @Test
    void shouldReturnAccountWhenAccountExistsAndBelongsToUser() {
        GetUserAccountResult account =
                account(
                        accountId,
                        userId,
                        productId,
                        accountNo,
                        Currency.VND,
                        new BigDecimal("500000"),
                        new BigDecimal("0"),
                        AccountStatus.ACTIVE
                );

        when(getUserAccountService.execute(
                new GetUserAccountCriteria(
                        accountId
                )
        )).thenReturn(account);

        AccountResponse response =
                handler.execute(
                        new GetUserAccountQuery(
                                userId,
                                accountId
                        )
                );

        assertAll(
                () -> assertEquals(
                        account.accountId(),
                        response.id()
                ),
                () -> assertEquals(
                        account.productId(),
                        response.productId()
                ),
                () -> assertEquals(
                        account.accountNo(),
                        response.accountNo()
                ),
                () -> assertEquals(
                        account.currency(),
                        response.currency()
                ),
                () -> assertEquals(
                        account.balance().toPlainString(),
                        response.balance()
                ),
                () -> assertEquals(
                        account.status(),
                        response.status()
                ),
                () -> assertEquals(
                        account.createdAt(),
                        response.createdAt()
                ),
                () -> assertEquals(
                        account.updatedAt(),
                        response.updatedAt()
                )
        );

        verify(getUserAccountService)
                .execute(
                        new GetUserAccountCriteria(
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
                new GetUserAccountCriteria(
                        accountId
                )
        )).thenThrow(
                new BusinessException(
                        ErrorCode.ACCOUNT_NOT_FOUND
                )
        );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                new GetUserAccountQuery(
                                        userId,
                                        accountId
                                )
                        )
                );

        assertEquals(
                ErrorCode.ACCOUNT_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(getUserAccountService)
                .execute(
                        new GetUserAccountCriteria(
                                accountId
                        )
                );

        verifyNoMoreInteractions(
                getUserAccountService
        );
    }

    @Test
    void shouldThrowWhenAccountDoesNotBelongToUser() {
        UUID ownerId = UUID.randomUUID();

        GetUserAccountResult account =
                account(
                        accountId,
                        ownerId,
                        productId,
                        accountNo,
                        Currency.VND,
                        new BigDecimal("500000"),
                        new BigDecimal("0"),
                        AccountStatus.ACTIVE
                );

        when(getUserAccountService.execute(
                new GetUserAccountCriteria(
                        accountId
                )
        )).thenReturn(account);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                new GetUserAccountQuery(
                                        userId,
                                        accountId
                                )
                        )
                );

        assertEquals(
                ErrorCode.ACCESS_DENIED,
                exception.getErrorCode()
        );

        verify(getUserAccountService)
                .execute(
                        new GetUserAccountCriteria(
                                accountId
                        )
                );

        verifyNoMoreInteractions(
                getUserAccountService
        );
    }

    private GetUserAccountResult account(
            UUID accountId,
            UUID userId,
            UUID productId,
            String accountNo,
            Currency currency,
            BigDecimal balance,
            BigDecimal holdAmount,
            AccountStatus status
    ) {
        Instant now = Instant.now();

        return new GetUserAccountResult(
                accountId,
                userId,
                productId,
                accountNo,
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