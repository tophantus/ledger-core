package com.example.ledgercore.account.query.handler;

import com.example.ledgercore.account.entity.Account;
import com.example.ledgercore.account.enums.AccountStatus;
import com.example.ledgercore.account.query.dto.AccountResponse;
import com.example.ledgercore.account.query.dto.GetAccountQuery;
import com.example.ledgercore.account.query.repository.AccountQueryRepository;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAccountHandlerTest {

    @Mock
    private AccountQueryRepository accountQueryRepository;

    private GetAccountHandler handler;

    private UUID userId;
    private UUID accountId;

    @BeforeEach
    void setUp() {
        handler = new GetAccountHandler(
                accountQueryRepository
        );

        userId = UUID.randomUUID();
        accountId = UUID.randomUUID();
    }

    @Test
    void shouldReturnAccountWhenAccountExistsAndBelongsToUser() {
        Account account = account(
                accountId,
                userId,
                "1000000001",
                "VND",
                new BigDecimal("500000"),
                AccountStatus.ACTIVE
        );

        when(accountQueryRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        AccountResponse response =
                handler.execute(
                        new GetAccountQuery(
                                userId,
                                accountId
                        )
                );

        assertEquals(account.getId(), response.id());
        assertEquals(account.getUserId(), response.userId());
        assertEquals(account.getAccountNo(), response.accountNo());
        assertEquals(account.getCurrency(), response.currency());
        assertEquals(account.getBalance(), response.balance());
        assertEquals(account.getStatus(), response.status());
        assertEquals(account.getCreatedAt(), response.createdAt());
        assertEquals(account.getUpdatedAt(), response.updatedAt());

        verify(accountQueryRepository)
                .findById(accountId);

        verifyNoMoreInteractions(accountQueryRepository);
    }

    @Test
    void shouldThrowWhenAccountNotFound() {
        when(accountQueryRepository.findById(accountId))
                .thenReturn(Optional.empty());

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                new GetAccountQuery(
                                        userId,
                                        accountId
                                )
                        )
                );

        assertEquals(
                ErrorCode.ACCOUNT_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(accountQueryRepository)
                .findById(accountId);

        verifyNoMoreInteractions(accountQueryRepository);
    }

    @Test
    void shouldThrowWhenAccountDoesNotBelongToUser() {
        UUID ownerId = UUID.randomUUID();

        Account account = account(
                accountId,
                ownerId,
                "1000000001",
                "VND",
                new BigDecimal("500000"),
                AccountStatus.ACTIVE
        );

        when(accountQueryRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                new GetAccountQuery(
                                        userId,
                                        accountId
                                )
                        )
                );

        assertEquals(
                ErrorCode.ACCESS_DENIED,
                exception.getErrorCode()
        );

        verify(accountQueryRepository)
                .findById(accountId);

        verifyNoMoreInteractions(accountQueryRepository);
    }

    private Account account(
            UUID accountId,
            UUID userId,
            String accountNo,
            String currency,
            BigDecimal balance,
            AccountStatus status
    ) {
        Instant now = Instant.now();

        return Account.builder()
                .id(accountId)
                .userId(userId)
                .accountNo(accountNo)
                .currency(currency)
                .balance(balance)
                .status(status)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}