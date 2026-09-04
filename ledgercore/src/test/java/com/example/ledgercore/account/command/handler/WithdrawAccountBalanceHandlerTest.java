package com.example.ledgercore.account.command.handler;

import com.example.ledgercore.account.command.dto.WithdrawAccountCommand;
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
class WithdrawAccountBalanceHandlerTest {

    @Mock
    private AccountCommandRepository accountCommandRepository;

    @Mock
    private AccountDailyBalanceService accountDailyBalanceService;

    private WithdrawAccountBalanceHandler handler;

    @BeforeEach
    void setUp() {
        handler = new WithdrawAccountBalanceHandler(
                accountCommandRepository,
                accountDailyBalanceService
        );
    }

    @Test
    void shouldWithdrawBalanceSuccessfully() {
        // Given
        UUID accountId = UUID.randomUUID();
        LocalDate businessDate = LocalDate.of(2026, 9, 4);

        Account account = account(
                accountId,
                AccountStatus.ACTIVE,
                "1000"
        );

        WithdrawAccountCommand command =
                new WithdrawAccountCommand(
                        accountId,
                        new BigDecimal("400"),
                        businessDate
                );

        when(accountCommandRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        // When
        handler.execute(command);

        // Then
        assertThat(account.getBalance())
                .isEqualByComparingTo("600");

        verify(accountCommandRepository)
                .findById(accountId);

        verify(accountDailyBalanceService)
                .updateClosingBalance(
                        accountId,
                        businessDate,
                        new BigDecimal("600")
                );

        verify(accountCommandRepository)
                .save(account);

        verifyNoMoreInteractions(
                accountCommandRepository,
                accountDailyBalanceService
        );
    }

    @Test
    void shouldThrowWhenAmountIsNull() {
        // Given
        UUID accountId = UUID.randomUUID();

        WithdrawAccountCommand command =
                new WithdrawAccountCommand(
                        accountId,
                        null,
                        LocalDate.of(2026, 9, 4)
                );

        // When & Then
        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class);

        verifyNoInteractions(
                accountCommandRepository,
                accountDailyBalanceService
        );
    }

    @Test
    void shouldThrowWhenAmountIsZero() {
        // Given
        UUID accountId = UUID.randomUUID();

        WithdrawAccountCommand command =
                new WithdrawAccountCommand(
                        accountId,
                        BigDecimal.ZERO,
                        LocalDate.of(2026, 9, 4)
                );

        // When & Then
        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class);

        verifyNoInteractions(
                accountCommandRepository,
                accountDailyBalanceService
        );
    }

    @Test
    void shouldThrowWhenAmountIsNegative() {
        // Given
        UUID accountId = UUID.randomUUID();

        WithdrawAccountCommand command =
                new WithdrawAccountCommand(
                        accountId,
                        new BigDecimal("-100"),
                        LocalDate.of(2026, 9, 4)
                );

        // When & Then
        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class);

        verifyNoInteractions(
                accountCommandRepository,
                accountDailyBalanceService
        );
    }

    @Test
    void shouldThrowWhenAccountNotFound() {
        // Given
        UUID accountId = UUID.randomUUID();

        WithdrawAccountCommand command =
                new WithdrawAccountCommand(
                        accountId,
                        new BigDecimal("100"),
                        LocalDate.of(2026, 9, 4)
                );

        when(accountCommandRepository.findById(accountId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class);

        verify(accountCommandRepository)
                .findById(accountId);

        verify(accountCommandRepository, never())
                .save(any(Account.class));

        verifyNoInteractions(accountDailyBalanceService);

        verifyNoMoreInteractions(accountCommandRepository);
    }

    @Test
    void shouldThrowWhenAccountIsNotActive() {
        // Given
        UUID accountId = UUID.randomUUID();

        Account account = account(
                accountId,
                AccountStatus.BLOCKED,
                "1000"
        );

        WithdrawAccountCommand command =
                new WithdrawAccountCommand(
                        accountId,
                        new BigDecimal("100"),
                        LocalDate.of(2026, 9, 4)
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

        verify(accountCommandRepository, never())
                .save(any(Account.class));

        verifyNoInteractions(accountDailyBalanceService);

        verifyNoMoreInteractions(accountCommandRepository);
    }

    @Test
    void shouldThrowWhenBalanceIsInsufficient() {
        // Given
        UUID accountId = UUID.randomUUID();

        Account account = account(
                accountId,
                AccountStatus.ACTIVE,
                "100"
        );

        WithdrawAccountCommand command =
                new WithdrawAccountCommand(
                        accountId,
                        new BigDecimal("200"),
                        LocalDate.of(2026, 9, 4)
                );

        when(accountCommandRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        // When & Then
        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class);

        assertThat(account.getBalance())
                .isEqualByComparingTo("100");

        verify(accountCommandRepository)
                .findById(accountId);

        verify(accountCommandRepository, never())
                .save(any(Account.class));

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