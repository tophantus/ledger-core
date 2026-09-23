package com.example.ledgercore.account.command.handler;

import com.example.ledgercore.account.command.dto.DecreaseAccountBalanceCommand;
import com.example.ledgercore.account.command.repository.AccountCommandRepository;
import com.example.ledgercore.account.command.service.AccountDailyBalanceService;
import com.example.ledgercore.account.entity.Account;
import com.example.ledgercore.account.enums.AccountStatus;
import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DecreaseAccountBalanceHandlerTest {

    @Mock
    private AccountCommandRepository accountCommandRepository;

    @Mock
    private AccountDailyBalanceService accountDailyBalanceService;

    private DecreaseAccountBalanceHandler handler;

    private UUID accountId;
    private UUID productId;
    private UUID ledgerAccountId;
    private LocalDate businessDate;

    @BeforeEach
    void setUp() {
        handler = new DecreaseAccountBalanceHandler(
                accountCommandRepository,
                accountDailyBalanceService
        );

        accountId = UUID.randomUUID();
        productId = UUID.randomUUID();
        ledgerAccountId = UUID.randomUUID();
        businessDate = LocalDate.of(2026, 9, 23);
    }

    @Test
    void shouldDecreaseAccountBalance() {
        Account account = account(
                AccountStatus.ACTIVE,
                Currency.VND,
                new BigDecimal("1000000")
        );

        DecreaseAccountBalanceCommand command =
                command(
                        new BigDecimal("300000"),
                        Currency.VND
                );

        givenAccount(account);

        handler.execute(command);

        assertEquals(
                new BigDecimal("700000"),
                account.getBalance()
        );
    }

    @Test
    void shouldUpdateDailyClosingBalance() {
        Account account = account(
                AccountStatus.ACTIVE,
                Currency.VND,
                new BigDecimal("1000000")
        );

        DecreaseAccountBalanceCommand command =
                command(
                        new BigDecimal("300000"),
                        Currency.VND
                );

        givenAccount(account);

        handler.execute(command);

        verify(accountDailyBalanceService)
                .updateClosingBalance(
                        accountId,
                        businessDate,
                        new BigDecimal("700000")
                );
    }

    @Test
    void shouldDecreaseBalanceAndUpdateDailyClosingBalance() {
        Account account = account(
                AccountStatus.ACTIVE,
                Currency.VND,
                new BigDecimal("2500000")
        );

        DecreaseAccountBalanceCommand command =
                command(
                        new BigDecimal("750000"),
                        Currency.VND
                );

        givenAccount(account);

        handler.execute(command);

        assertEquals(
                new BigDecimal("1750000"),
                account.getBalance()
        );

        verify(accountDailyBalanceService)
                .updateClosingBalance(
                        accountId,
                        businessDate,
                        new BigDecimal("1750000")
                );
    }

    @Test
    void shouldAllowAmountEqualToBalance() {
        Account account = account(
                AccountStatus.ACTIVE,
                Currency.VND,
                new BigDecimal("1000000")
        );

        DecreaseAccountBalanceCommand command =
                command(
                        new BigDecimal("1000000"),
                        Currency.VND
                );

        givenAccount(account);

        handler.execute(command);

        assertEquals(
                BigDecimal.ZERO,
                account.getBalance()
        );

        verify(accountDailyBalanceService)
                .updateClosingBalance(
                        accountId,
                        businessDate,
                        BigDecimal.ZERO
                );
    }

    @Test
    void shouldThrowWhenCommandIsNull() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(null)
                );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                accountCommandRepository,
                accountDailyBalanceService
        );
    }

    @Test
    void shouldThrowWhenAccountIdIsNull() {
        DecreaseAccountBalanceCommand command =
                new DecreaseAccountBalanceCommand(
                        null,
                        new BigDecimal("100000"),
                        Currency.VND,
                        businessDate
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                accountCommandRepository,
                accountDailyBalanceService
        );
    }

    @Test
    void shouldThrowWhenAmountIsNull() {
        DecreaseAccountBalanceCommand command =
                new DecreaseAccountBalanceCommand(
                        accountId,
                        null,
                        Currency.VND,
                        businessDate
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                accountCommandRepository,
                accountDailyBalanceService
        );
    }

    @Test
    void shouldThrowWhenCurrencyIsNull() {
        DecreaseAccountBalanceCommand command =
                new DecreaseAccountBalanceCommand(
                        accountId,
                        new BigDecimal("100000"),
                        null,
                        businessDate
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                accountCommandRepository,
                accountDailyBalanceService
        );
    }

    @Test
    void shouldThrowWhenBusinessDateIsNull() {
        DecreaseAccountBalanceCommand command =
                new DecreaseAccountBalanceCommand(
                        accountId,
                        new BigDecimal("100000"),
                        Currency.VND,
                        null
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                accountCommandRepository,
                accountDailyBalanceService
        );
    }

    @Test
    void shouldThrowWhenAmountIsZero() {
        DecreaseAccountBalanceCommand command =
                command(
                        BigDecimal.ZERO,
                        Currency.VND
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.INVALID_AMOUNT,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                accountCommandRepository,
                accountDailyBalanceService
        );
    }

    @Test
    void shouldThrowWhenAmountIsNegative() {
        DecreaseAccountBalanceCommand command =
                command(
                        new BigDecimal("-100000"),
                        Currency.VND
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.INVALID_AMOUNT,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                accountCommandRepository,
                accountDailyBalanceService
        );
    }

    @Test
    void shouldThrowWhenAccountNotFound() {
        DecreaseAccountBalanceCommand command =
                command(
                        new BigDecimal("100000"),
                        Currency.VND
                );

        when(accountCommandRepository.findById(accountId))
                .thenReturn(Optional.empty());

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.ACCOUNT_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(accountCommandRepository)
                .findById(accountId);

        verifyNoInteractions(
                accountDailyBalanceService
        );
    }

    @Test
    void shouldThrowWhenAccountIsBlocked() {
        Account account = account(
                AccountStatus.BLOCKED,
                Currency.VND,
                new BigDecimal("1000000")
        );

        DecreaseAccountBalanceCommand command =
                command(
                        new BigDecimal("100000"),
                        Currency.VND
                );

        givenAccount(account);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.INVALID_TRANSACTION_STATUS,
                exception.getErrorCode()
        );

        assertEquals(
                new BigDecimal("1000000"),
                account.getBalance()
        );

        verifyNoInteractions(
                accountDailyBalanceService
        );
    }

    @Test
    void shouldThrowWhenAccountIsClosed() {
        Account account = account(
                AccountStatus.CLOSED,
                Currency.VND,
                new BigDecimal("1000000")
        );

        DecreaseAccountBalanceCommand command =
                command(
                        new BigDecimal("100000"),
                        Currency.VND
                );

        givenAccount(account);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.INVALID_TRANSACTION_STATUS,
                exception.getErrorCode()
        );

        assertEquals(
                new BigDecimal("1000000"),
                account.getBalance()
        );

        verifyNoInteractions(
                accountDailyBalanceService
        );
    }

    @Test
    void shouldThrowWhenAccountCurrencyDoesNotMatch() {
        Account account = account(
                AccountStatus.ACTIVE,
                Currency.VND,
                new BigDecimal("1000000")
        );

        DecreaseAccountBalanceCommand command =
                command(
                        new BigDecimal("100000"),
                        Currency.USD
                );

        givenAccount(account);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.ACCOUNT_CURRENCY_MISMATCH,
                exception.getErrorCode()
        );

        assertEquals(
                new BigDecimal("1000000"),
                account.getBalance()
        );

        verifyNoInteractions(
                accountDailyBalanceService
        );
    }

    @Test
    void shouldThrowWhenAccountBalanceIsInsufficient() {
        Account account = account(
                AccountStatus.ACTIVE,
                Currency.VND,
                new BigDecimal("1000000")
        );

        DecreaseAccountBalanceCommand command =
                command(
                        new BigDecimal("1000001"),
                        Currency.VND
                );

        givenAccount(account);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.ACCOUNT_INSUFFICIENT_BALANCE,
                exception.getErrorCode()
        );

        assertEquals(
                new BigDecimal("1000000"),
                account.getBalance()
        );

        verifyNoInteractions(
                accountDailyBalanceService
        );
    }

    @Test
    void shouldAllowSmallestPositiveAmount() {
        Account account = account(
                AccountStatus.ACTIVE,
                Currency.VND,
                new BigDecimal("1000000")
        );

        DecreaseAccountBalanceCommand command =
                command(
                        new BigDecimal("0.0001"),
                        Currency.VND
                );

        givenAccount(account);

        handler.execute(command);

        assertEquals(
                new BigDecimal("999999.9999"),
                account.getBalance()
        );

        verify(accountDailyBalanceService)
                .updateClosingBalance(
                        accountId,
                        businessDate,
                        new BigDecimal("999999.9999")
                );
    }

    private void givenAccount(Account account) {
        when(accountCommandRepository.findById(accountId))
                .thenReturn(Optional.of(account));
    }

    private DecreaseAccountBalanceCommand command(
            BigDecimal amount,
            Currency currency
    ) {
        return new DecreaseAccountBalanceCommand(
                accountId,
                amount,
                currency,
                businessDate
        );
    }

    private Account account(
            AccountStatus status,
            Currency currency,
            BigDecimal balance
    ) {
        return Account.builder()
                .id(accountId)
                .productId(productId)
                .accountNo("1000000001")
                .currency(currency)
                .balance(balance)
                .holdAmount(BigDecimal.ZERO)
                .status(status)
                .version(0L)
                .ledgerAccountId(ledgerAccountId)
                .build();
    }
}