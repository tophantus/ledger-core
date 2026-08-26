package com.example.ledgercore.account.query.handler;

import com.example.ledgercore.account.entity.Account;
import com.example.ledgercore.account.enums.AccountStatus;
import com.example.ledgercore.account.query.dto.AccountDepositInfo;
import com.example.ledgercore.account.query.repository.AccountQueryRepository;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetDepositAccountInfoHandlerTest {

    @Mock
    private AccountQueryRepository accountQueryRepository;

    private GetDepositAccountInfoHandler handler;

    private UUID accountId;

    @BeforeEach
    void setUp() {
        handler = new GetDepositAccountInfoHandler(
                accountQueryRepository
        );

        accountId = UUID.randomUUID();
    }

    @Test
    void shouldReturnDepositInfoWhenAccountIsActive() {
        Account account = Account.builder()
                .id(accountId)
                .currency("VND")
                .status(AccountStatus.ACTIVE)
                .build();

        when(accountQueryRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        AccountDepositInfo response =
                handler.execute(accountId);

        assertEquals(
                accountId,
                response.accountId()
        );

        assertEquals(
                "VND",
                response.currency()
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
                .currency("VND")
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
                ErrorCode.INVALID_TRANSACTION_STATUS,
                exception.getErrorCode()
        );

        verify(accountQueryRepository)
                .findById(accountId);

        verifyNoMoreInteractions(accountQueryRepository);
    }
}