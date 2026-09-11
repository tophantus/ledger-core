package com.example.ledgercore.account.query.handler;

import com.example.ledgercore.account.entity.Account;
import com.example.ledgercore.account.enums.AccountStatus;
import com.example.ledgercore.account.query.dto.AccountWithdrawInfo;
import com.example.ledgercore.account.query.repository.AccountQueryRepository;
import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetWithdrawAccountInfoHandlerTest {

    @Mock
    private AccountQueryRepository accountQueryRepository;

    private GetWithdrawAccountInfoHandler handler;

    private UUID userId;
    private UUID accountId;

    @BeforeEach
    void setUp() {
        handler = new GetWithdrawAccountInfoHandler(
                accountQueryRepository
        );

        userId = UUID.randomUUID();
        accountId = UUID.randomUUID();
    }

    @Test
    void shouldReturnWithdrawInfoWhenAccountIsActive() {
        BigDecimal balance =
                new BigDecimal("1000000");

        BigDecimal holdAmount =
                new BigDecimal("200000");

        BigDecimal expectedAvailableBalance =
                new BigDecimal("800000");

        Account account = Account.builder()
                .id(accountId)
                .userId(userId)
                .currency(Currency.VND)
                .balance(balance)
                .holdAmount(holdAmount)
                .status(AccountStatus.ACTIVE)
                .build();

        when(accountQueryRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        AccountWithdrawInfo response =
                handler.execute(accountId);

        assertEquals(
                accountId,
                response.accountId()
        );

        assertEquals(
                userId,
                response.userId()
        );

        assertEquals(
                Currency.VND,
                response.currency()
        );

        assertEquals(
                expectedAvailableBalance,
                response.availableBalance()
        );

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
                        () -> handler.execute(accountId)
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
    void shouldThrowWhenAccountIsNotActive() {
        Account account = Account.builder()
                .id(accountId)
                .userId(userId)
                .currency(Currency.VND)
                .balance(new BigDecimal("1000000"))
                .holdAmount(new BigDecimal("200000"))
                .status(AccountStatus.BLOCKED)
                .build();

        when(accountQueryRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(accountId)
                );

        assertEquals(
                ErrorCode.ACCOUNT_NOT_ACTIVE,
                exception.getErrorCode()
        );

        verify(accountQueryRepository)
                .findById(accountId);

        verifyNoMoreInteractions(accountQueryRepository);
    }
}