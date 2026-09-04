package com.example.ledgercore.account.command.handler;

import com.example.ledgercore.account.command.dto.TransferAccountCommand;
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
class TransferAccountHandlerTest {

    @Mock
    private AccountCommandRepository accountCommandRepository;

    @Mock
    private AccountDailyBalanceService accountDailyBalanceService;

    private TransferAccountHandler handler;

    @BeforeEach
    void setUp() {
        handler = new TransferAccountHandler(
                accountCommandRepository,
                accountDailyBalanceService
        );
    }

    @Test
    void shouldTransferSuccessfully() {
        // Given
        UUID sourceId = UUID.randomUUID();
        UUID destinationId = UUID.randomUUID();
        LocalDate businessDate = LocalDate.of(2026, 9, 4);

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
                        new BigDecimal("300"),
                        businessDate
                );

        mockAccounts(
                sourceId,
                source,
                destinationId,
                destination
        );

        // When
        handler.execute(command);

        // Then
        assertThat(source.getBalance())
                .isEqualByComparingTo("700");

        assertThat(destination.getBalance())
                .isEqualByComparingTo("800");

        verify(accountCommandRepository)
                .findById(sourceId);

        verify(accountCommandRepository)
                .findById(destinationId);

        verify(accountDailyBalanceService)
                .updateClosingBalance(
                        sourceId,
                        businessDate,
                        new BigDecimal("700")
                );

        verify(accountDailyBalanceService)
                .updateClosingBalance(
                        destinationId,
                        businessDate,
                        new BigDecimal("800")
                );

        verifyNoMoreInteractions(
                accountCommandRepository,
                accountDailyBalanceService
        );
    }

    @Test
    void shouldThrowWhenAmountIsNull() {
        // Given
        TransferAccountCommand command =
                new TransferAccountCommand(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
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
        TransferAccountCommand command =
                new TransferAccountCommand(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
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
        TransferAccountCommand command =
                new TransferAccountCommand(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
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
    void shouldThrowWhenSourceAccountNotFound() {
        // Given
        UUID sourceId = UUID.randomUUID();
        UUID destinationId = UUID.randomUUID();

        TransferAccountCommand command =
                new TransferAccountCommand(
                        sourceId,
                        destinationId,
                        new BigDecimal("100"),
                        LocalDate.of(2026, 9, 4)
                );

        when(accountCommandRepository.findById(sourceId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class);

        verify(accountCommandRepository)
                .findById(sourceId);

        verify(accountCommandRepository, never())
                .findById(destinationId);

        verifyNoInteractions(accountDailyBalanceService);

        verifyNoMoreInteractions(accountCommandRepository);
    }

    @Test
    void shouldThrowWhenDestinationAccountNotFound() {
        // Given
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
                        new BigDecimal("100"),
                        LocalDate.of(2026, 9, 4)
                );

        when(accountCommandRepository.findById(sourceId))
                .thenReturn(Optional.of(source));

        when(accountCommandRepository.findById(destinationId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class);

        verify(accountCommandRepository)
                .findById(sourceId);

        verify(accountCommandRepository)
                .findById(destinationId);

        verifyNoInteractions(accountDailyBalanceService);

        verifyNoMoreInteractions(accountCommandRepository);
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
                        new BigDecimal("100"),
                        LocalDate.of(2026, 9, 4)
                );

        when(accountCommandRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class);

        assertThat(account.getBalance())
                .isEqualByComparingTo("1000");

        verify(accountCommandRepository, times(2))
                .findById(accountId);

        verifyNoInteractions(accountDailyBalanceService);

        verifyNoMoreInteractions(accountCommandRepository);
    }

    @Test
    void shouldThrowWhenSourceAccountIsNotActive() {
        // Given
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

        mockAccounts(
                sourceId,
                source,
                destinationId,
                destination
        );

        TransferAccountCommand command =
                new TransferAccountCommand(
                        sourceId,
                        destinationId,
                        new BigDecimal("100"),
                        LocalDate.of(2026, 9, 4)
                );

        // When & Then
        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class);

        assertThat(source.getBalance())
                .isEqualByComparingTo("1000");

        assertThat(destination.getBalance())
                .isEqualByComparingTo("500");

        verifyNoInteractions(accountDailyBalanceService);

        verifyNoMoreInteractions(accountCommandRepository);
    }

    @Test
    void shouldThrowWhenDestinationAccountIsNotActive() {
        // Given
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

        mockAccounts(
                sourceId,
                source,
                destinationId,
                destination
        );

        TransferAccountCommand command =
                new TransferAccountCommand(
                        sourceId,
                        destinationId,
                        new BigDecimal("100"),
                        LocalDate.of(2026, 9, 4)
                );

        // When & Then
        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class);

        assertThat(source.getBalance())
                .isEqualByComparingTo("1000");

        assertThat(destination.getBalance())
                .isEqualByComparingTo("500");

        verifyNoInteractions(accountDailyBalanceService);

        verifyNoMoreInteractions(accountCommandRepository);
    }

    @Test
    void shouldThrowWhenCurrencyMismatch() {
        // Given
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

        mockAccounts(
                sourceId,
                source,
                destinationId,
                destination
        );

        TransferAccountCommand command =
                new TransferAccountCommand(
                        sourceId,
                        destinationId,
                        new BigDecimal("100"),
                        LocalDate.of(2026, 9, 4)
                );

        // When & Then
        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class);

        assertThat(source.getBalance())
                .isEqualByComparingTo("1000");

        assertThat(destination.getBalance())
                .isEqualByComparingTo("500");

        verifyNoInteractions(accountDailyBalanceService);

        verifyNoMoreInteractions(accountCommandRepository);
    }

    @Test
    void shouldThrowWhenBalanceIsInsufficient() {
        // Given
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

        mockAccounts(
                sourceId,
                source,
                destinationId,
                destination
        );

        TransferAccountCommand command =
                new TransferAccountCommand(
                        sourceId,
                        destinationId,
                        new BigDecimal("200"),
                        LocalDate.of(2026, 9, 4)
                );

        // When & Then
        assertThatThrownBy(() -> handler.execute(command))
                .isInstanceOf(BusinessException.class);

        assertThat(source.getBalance())
                .isEqualByComparingTo("100");

        assertThat(destination.getBalance())
                .isEqualByComparingTo("500");

        verifyNoInteractions(accountDailyBalanceService);

        verifyNoMoreInteractions(accountCommandRepository);
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