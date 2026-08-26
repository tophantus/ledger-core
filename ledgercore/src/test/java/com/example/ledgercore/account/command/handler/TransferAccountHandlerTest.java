package com.example.ledgercore.account.command.handler;

import com.example.ledgercore.account.command.dto.TransferAccountCommand;
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
class TransferAccountHandlerTest {

    @Mock
    private AccountCommandRepository accountCommandRepository;

    private TransferAccountHandler handler;

    @BeforeEach
    void setUp() {
        handler = new TransferAccountHandler(
                accountCommandRepository
        );
    }

    @Test
    void shouldTransferSuccessfully() {
        UUID sourceId = UUID.randomUUID();
        UUID destinationId = UUID.randomUUID();

        Account source = account(
                sourceId,
                AccountStatus.ACTIVE,
                "1000",
                "VND"
        );

        Account destination = account(
                destinationId,
                AccountStatus.ACTIVE,
                "500",
                "VND"
        );

        TransferAccountCommand command =
                new TransferAccountCommand(
                        sourceId,
                        destinationId,
                        new BigDecimal("300")
                );

        when(accountCommandRepository.findById(sourceId))
                .thenReturn(Optional.of(source));

        when(accountCommandRepository.findById(destinationId))
                .thenReturn(Optional.of(destination));

        handler.execute(command);

        assertThat(source.getBalance())
                .isEqualByComparingTo("700");

        assertThat(destination.getBalance())
                .isEqualByComparingTo("800");

        verify(accountCommandRepository)
                .findById(sourceId);

        verify(accountCommandRepository)
                .findById(destinationId);

        verifyNoMoreInteractions(accountCommandRepository);
    }

    @Test
    void shouldThrowWhenAmountIsNull() {
        TransferAccountCommand command =
                new TransferAccountCommand(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        null
                );

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class);

        verifyNoInteractions(accountCommandRepository);
    }

    @Test
    void shouldThrowWhenAmountIsZero() {
        TransferAccountCommand command =
                new TransferAccountCommand(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        BigDecimal.ZERO
                );

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class);

        verifyNoInteractions(accountCommandRepository);
    }

    @Test
    void shouldThrowWhenAmountIsNegative() {
        TransferAccountCommand command =
                new TransferAccountCommand(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        new BigDecimal("-100")
                );

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class);

        verifyNoInteractions(accountCommandRepository);
    }

    @Test
    void shouldThrowWhenSourceAccountNotFound() {
        UUID sourceId = UUID.randomUUID();
        UUID destinationId = UUID.randomUUID();

        TransferAccountCommand command =
                new TransferAccountCommand(
                        sourceId,
                        destinationId,
                        new BigDecimal("100")
                );

        when(accountCommandRepository.findById(sourceId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class);

        verify(accountCommandRepository)
                .findById(sourceId);

        verify(accountCommandRepository, never())
                .findById(destinationId);
    }

    @Test
    void shouldThrowWhenDestinationAccountNotFound() {
        UUID sourceId = UUID.randomUUID();
        UUID destinationId = UUID.randomUUID();

        Account source = account(
                sourceId,
                AccountStatus.ACTIVE,
                "1000",
                "VND"
        );

        TransferAccountCommand command =
                new TransferAccountCommand(
                        sourceId,
                        destinationId,
                        new BigDecimal("100")
                );

        when(accountCommandRepository.findById(sourceId))
                .thenReturn(Optional.of(source));

        when(accountCommandRepository.findById(destinationId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class);

        verify(accountCommandRepository)
                .findById(sourceId);

        verify(accountCommandRepository)
                .findById(destinationId);
    }

    @Test
    void shouldThrowWhenTransferToSameAccount() {
        UUID accountId = UUID.randomUUID();

        Account account = account(
                accountId,
                AccountStatus.ACTIVE,
                "1000",
                "VND"
        );

        TransferAccountCommand command =
                new TransferAccountCommand(
                        accountId,
                        accountId,
                        new BigDecimal("100")
                );

        when(accountCommandRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class);

        assertThat(account.getBalance())
                .isEqualByComparingTo("1000");
    }

    @Test
    void shouldThrowWhenSourceAccountIsNotActive() {
        UUID sourceId = UUID.randomUUID();
        UUID destinationId = UUID.randomUUID();

        Account source = account(
                sourceId,
                AccountStatus.BLOCKED,
                "1000",
                "VND"
        );

        Account destination = account(
                destinationId,
                AccountStatus.ACTIVE,
                "500",
                "VND"
        );

        mockAccounts(sourceId, source, destinationId, destination);

        TransferAccountCommand command =
                new TransferAccountCommand(
                        sourceId,
                        destinationId,
                        new BigDecimal("100")
                );

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class);

        assertThat(source.getBalance())
                .isEqualByComparingTo("1000");

        assertThat(destination.getBalance())
                .isEqualByComparingTo("500");
    }

    @Test
    void shouldThrowWhenDestinationAccountIsNotActive() {
        UUID sourceId = UUID.randomUUID();
        UUID destinationId = UUID.randomUUID();

        Account source = account(
                sourceId,
                AccountStatus.ACTIVE,
                "1000",
                "VND"
        );

        Account destination = account(
                destinationId,
                AccountStatus.BLOCKED,
                "500",
                "VND"
        );

        mockAccounts(sourceId, source, destinationId, destination);

        TransferAccountCommand command =
                new TransferAccountCommand(
                        sourceId,
                        destinationId,
                        new BigDecimal("100")
                );

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class);

        assertThat(source.getBalance())
                .isEqualByComparingTo("1000");

        assertThat(destination.getBalance())
                .isEqualByComparingTo("500");
    }

    @Test
    void shouldThrowWhenCurrencyMismatch() {
        UUID sourceId = UUID.randomUUID();
        UUID destinationId = UUID.randomUUID();

        Account source = account(
                sourceId,
                AccountStatus.ACTIVE,
                "1000",
                "VND"
        );

        Account destination = account(
                destinationId,
                AccountStatus.ACTIVE,
                "500",
                "USD"
        );

        mockAccounts(sourceId, source, destinationId, destination);

        TransferAccountCommand command =
                new TransferAccountCommand(
                        sourceId,
                        destinationId,
                        new BigDecimal("100")
                );

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class);

        assertThat(source.getBalance())
                .isEqualByComparingTo("1000");

        assertThat(destination.getBalance())
                .isEqualByComparingTo("500");
    }

    @Test
    void shouldThrowWhenBalanceIsInsufficient() {
        UUID sourceId = UUID.randomUUID();
        UUID destinationId = UUID.randomUUID();

        Account source = account(
                sourceId,
                AccountStatus.ACTIVE,
                "100",
                "VND"
        );

        Account destination = account(
                destinationId,
                AccountStatus.ACTIVE,
                "500",
                "VND"
        );

        mockAccounts(sourceId, source, destinationId, destination);

        TransferAccountCommand command =
                new TransferAccountCommand(
                        sourceId,
                        destinationId,
                        new BigDecimal("200")
                );

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class);

        assertThat(source.getBalance())
                .isEqualByComparingTo("100");

        assertThat(destination.getBalance())
                .isEqualByComparingTo("500");
    }

    private void mockAccounts(
            UUID sourceId,
            Account source,
            UUID destinationId,
            Account destination
    ) {
        when(accountCommandRepository.findById(sourceId))
                .thenReturn(Optional.of(source));

        when(accountCommandRepository.findById(destinationId))
                .thenReturn(Optional.of(destination));
    }

    private Account account(
            UUID id,
            AccountStatus status,
            String balance,
            String currency
    ) {
        return Account.builder()
                .id(id)
                .userId(UUID.randomUUID())
                .accountNo("ACC-" + id)
                .currency(currency)
                .balance(new BigDecimal(balance))
                .status(status)
                .build();
    }
}