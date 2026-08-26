package com.example.ledgercore.account.command.handler;

import com.example.ledgercore.account.command.dto.WithdrawAccountCommand;
import com.example.ledgercore.account.command.repository.AccountCommandRepository;
import com.example.ledgercore.account.entity.Account;
import com.example.ledgercore.account.enums.AccountStatus;
import com.example.ledgercore.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WithdrawAccountBalanceHandlerTest {

    @Mock
    private AccountCommandRepository accountCommandRepository;

    private WithdrawAccountBalanceHandler handler;

    @BeforeEach
    void setUp() {
        handler = new WithdrawAccountBalanceHandler(
                accountCommandRepository
        );
    }

    @Test
    void shouldWithdrawBalanceSuccessfully() {
        UUID accountId = UUID.randomUUID();

        Account account = account(
                accountId,
                AccountStatus.ACTIVE,
                "1000"
        );

        WithdrawAccountCommand command =
                new WithdrawAccountCommand(
                        accountId,
                        new BigDecimal("400")
                );

        when(accountCommandRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        handler.execute(command);

        assertThat(account.getBalance())
                .isEqualByComparingTo("600");

        verify(accountCommandRepository)
                .findById(accountId);

        verify(accountCommandRepository)
                .save(account);
    }

    @Test
    void shouldThrowWhenAmountIsNull() {
        UUID accountId = UUID.randomUUID();

        WithdrawAccountCommand command =
                new WithdrawAccountCommand(
                        accountId,
                        null
                );

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class);

        verifyNoInteractions(accountCommandRepository);
    }

    @Test
    void shouldThrowWhenAmountIsZero() {
        UUID accountId = UUID.randomUUID();

        WithdrawAccountCommand command =
                new WithdrawAccountCommand(
                        accountId,
                        BigDecimal.ZERO
                );

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class);

        verifyNoInteractions(accountCommandRepository);
    }

    @Test
    void shouldThrowWhenAmountIsNegative() {
        UUID accountId = UUID.randomUUID();

        WithdrawAccountCommand command =
                new WithdrawAccountCommand(
                        accountId,
                        new BigDecimal("-100")
                );

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class);

        verifyNoInteractions(accountCommandRepository);
    }

    @Test
    void shouldThrowWhenAccountNotFound() {
        UUID accountId = UUID.randomUUID();

        WithdrawAccountCommand command =
                new WithdrawAccountCommand(
                        accountId,
                        new BigDecimal("100")
                );

        when(accountCommandRepository.findById(accountId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class);

        verify(accountCommandRepository)
                .findById(accountId);

        verify(accountCommandRepository, never())
                .save(any());
    }

    @Test
    void shouldThrowWhenAccountIsNotActive() {
        UUID accountId = UUID.randomUUID();

        Account account = account(
                accountId,
                AccountStatus.BLOCKED,
                "1000"
        );

        WithdrawAccountCommand command =
                new WithdrawAccountCommand(
                        accountId,
                        new BigDecimal("100")
                );

        when(accountCommandRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class);

        assertThat(account.getBalance())
                .isEqualByComparingTo("1000");

        verify(accountCommandRepository)
                .findById(accountId);

        verify(accountCommandRepository, never())
                .save(any());
    }

    @Test
    void shouldThrowWhenBalanceIsInsufficient() {
        UUID accountId = UUID.randomUUID();

        Account account = account(
                accountId,
                AccountStatus.ACTIVE,
                "100"
        );

        WithdrawAccountCommand command =
                new WithdrawAccountCommand(
                        accountId,
                        new BigDecimal("200")
                );

        when(accountCommandRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class);

        assertThat(account.getBalance())
                .isEqualByComparingTo("100");

        verify(accountCommandRepository)
                .findById(accountId);

        verify(accountCommandRepository, never())
                .save(any());
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