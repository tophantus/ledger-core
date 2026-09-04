package com.example.ledgercore.account.command.handler;

import com.example.ledgercore.account.command.dto.DepositAccountCommand;
import com.example.ledgercore.account.command.repository.AccountCommandRepository;
import com.example.ledgercore.account.command.service.AccountDailyBalanceService;
import com.example.ledgercore.account.entity.Account;
import com.example.ledgercore.account.enums.AccountStatus;
import com.example.ledgercore.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepositAccountBalanceHandlerTest {

    @Mock
    private AccountCommandRepository accountCommandRepository;

    @Mock
    private AccountDailyBalanceService accountDailyBalanceService;

    private DepositAccountBalanceHandler handler;

    @BeforeEach
    void setUp() {
        handler = new DepositAccountBalanceHandler(
                accountCommandRepository,
                accountDailyBalanceService
        );
    }

    @Test
    void shouldDepositBalanceSuccessfully() {
        // Given
        UUID accountId = UUID.randomUUID();
        LocalDate businessDate = LocalDate.of(2026, 9, 4);

        Account account = account(
                accountId,
                AccountStatus.ACTIVE,
                "1000"
        );

        DepositAccountCommand command =
                new DepositAccountCommand(
                        accountId,
                        new BigDecimal("500"),
                        businessDate
                );

        when(accountCommandRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        // When
        handler.execute(command);

        // Then
        assertThat(account.getBalance())
                .isEqualByComparingTo("1500");

        verify(accountCommandRepository)
                .findById(accountId);

        verify(accountDailyBalanceService)
                .updateClosingBalance(
                        accountId,
                        businessDate,
                        new BigDecimal("1500")
                );

        verifyNoMoreInteractions(
                accountCommandRepository,
                accountDailyBalanceService
        );
    }

    @Test
    void shouldThrowWhenAccountNotFound() {
        // Given
        UUID accountId = UUID.randomUUID();
        LocalDate businessDate = LocalDate.of(2026, 9, 4);

        DepositAccountCommand command =
                new DepositAccountCommand(
                        accountId,
                        new BigDecimal("500"),
                        businessDate
                );

        when(accountCommandRepository.findById(accountId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class);

        verify(accountCommandRepository)
                .findById(accountId);

        verifyNoInteractions(accountDailyBalanceService);

        verifyNoMoreInteractions(accountCommandRepository);
    }

    @Test
    void shouldThrowWhenAccountIsNotActive() {
        // Given
        UUID accountId = UUID.randomUUID();
        LocalDate businessDate = LocalDate.of(2026, 9, 4);

        Account account = account(
                accountId,
                AccountStatus.BLOCKED,
                "1000"
        );

        DepositAccountCommand command =
                new DepositAccountCommand(
                        accountId,
                        new BigDecimal("500"),
                        businessDate
                );

        when(accountCommandRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        // When & Then
        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class);

        assertThat(account.getBalance())
                .isEqualByComparingTo("1000");

        verify(accountCommandRepository)
                .findById(accountId);

        verifyNoInteractions(accountDailyBalanceService);

        verifyNoMoreInteractions(accountCommandRepository);
    }

    private Account account(
            UUID id,
            AccountStatus status,
            String balance
    ) {
        return Account.builder()
                .id(id)
                .userId(UUID.randomUUID())
                .accountNo("ACC-001")
                .currency("VND")
                .balance(new BigDecimal(balance))
                .status(status)
                .build();
    }
}