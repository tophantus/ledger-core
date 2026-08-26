package com.example.ledgercore.account.command.handler;

import com.example.ledgercore.account.command.dto.DepositAccountCommand;
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
class DepositAccountBalanceHandlerTest {

    @Mock
    private AccountCommandRepository accountCommandRepository;

    private DepositAccountBalanceHandler handler;

    @BeforeEach
    void setUp() {
        handler = new DepositAccountBalanceHandler(
                accountCommandRepository
        );
    }

    @Test
    void shouldDepositBalanceSuccessfully() {
        UUID accountId = UUID.randomUUID();

        Account account = account(
                accountId,
                AccountStatus.ACTIVE,
                "1000"
        );

        DepositAccountCommand command =
                new DepositAccountCommand(
                        accountId,
                        new BigDecimal("500")
                );

        when(accountCommandRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        handler.execute(command);

        assertThat(account.getBalance())
                .isEqualByComparingTo("1500");

        verify(accountCommandRepository)
                .findById(accountId);

        verifyNoMoreInteractions(accountCommandRepository);
    }

    @Test
    void shouldThrowWhenAccountNotFound() {
        UUID accountId = UUID.randomUUID();

        DepositAccountCommand command =
                new DepositAccountCommand(
                        accountId,
                        new BigDecimal("500")
                );

        when(accountCommandRepository.findById(accountId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class);

        verify(accountCommandRepository)
                .findById(accountId);

        verifyNoMoreInteractions(accountCommandRepository);
    }

    @Test
    void shouldThrowWhenAccountIsNotActive() {
        UUID accountId = UUID.randomUUID();

        Account account = account(
                accountId,
                AccountStatus.BLOCKED,
                "1000"
        );

        DepositAccountCommand command =
                new DepositAccountCommand(
                        accountId,
                        new BigDecimal("500")
                );

        when(accountCommandRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class);

        assertThat(account.getBalance())
                .isEqualByComparingTo("1000");

        verify(accountCommandRepository)
                .findById(accountId);

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