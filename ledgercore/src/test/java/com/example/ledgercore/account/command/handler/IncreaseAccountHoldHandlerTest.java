package com.example.ledgercore.account.command.handler;

import com.example.ledgercore.account.command.dto.IncreaseAccountHoldCommand;
import com.example.ledgercore.account.command.repository.AccountCommandRepository;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IncreaseAccountHoldHandlerTest {

    @Mock
    private AccountCommandRepository accountCommandRepository;

    private IncreaseAccountHoldHandler handler;

    private UUID accountId;
    private UUID productId;
    private UUID ledgerAccountId;

    @BeforeEach
    void setUp() {
        handler = new IncreaseAccountHoldHandler(
                accountCommandRepository
        );

        accountId = UUID.randomUUID();
        productId = UUID.randomUUID();
        ledgerAccountId = UUID.randomUUID();
    }

    @Test
    void shouldIncreaseAccountHold() {
        Account account = account(
                AccountStatus.ACTIVE,
                Currency.VND,
                new BigDecimal("1000000"),
                new BigDecimal("200000")
        );

        IncreaseAccountHoldCommand command =
                command(
                        new BigDecimal("300000"),
                        Currency.VND
                );

        givenAccount(account);

        handler.execute(command);

        assertEquals(
                new BigDecimal("500000"),
                account.getHoldAmount()
        );
    }

    @Test
    void shouldIncreaseAccountHoldWhenAvailableBalanceEqualsAmount() {
        Account account = account(
                AccountStatus.ACTIVE,
                Currency.VND,
                new BigDecimal("1000000"),
                new BigDecimal("700000")
        );

        IncreaseAccountHoldCommand command =
                command(
                        new BigDecimal("300000"),
                        Currency.VND
                );

        givenAccount(account);

        handler.execute(command);

        assertEquals(
                new BigDecimal("1000000"),
                account.getHoldAmount()
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
                accountCommandRepository
        );
    }

    @Test
    void shouldThrowWhenAccountIdIsNull() {
        IncreaseAccountHoldCommand command =
                new IncreaseAccountHoldCommand(
                        null,
                        new BigDecimal("100000"),
                        Currency.VND
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
                accountCommandRepository
        );
    }

    @Test
    void shouldThrowWhenAmountIsNull() {
        IncreaseAccountHoldCommand command =
                new IncreaseAccountHoldCommand(
                        accountId,
                        null,
                        Currency.VND
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
                accountCommandRepository
        );
    }

    @Test
    void shouldThrowWhenCurrencyIsNull() {
        IncreaseAccountHoldCommand command =
                new IncreaseAccountHoldCommand(
                        accountId,
                        new BigDecimal("100000"),
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
                accountCommandRepository
        );
    }

    @Test
    void shouldThrowWhenAmountIsZero() {
        IncreaseAccountHoldCommand command =
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
                ErrorCode.INVALID_HOLD_AMOUNT,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                accountCommandRepository
        );
    }

    @Test
    void shouldThrowWhenAmountIsNegative() {
        IncreaseAccountHoldCommand command =
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
                ErrorCode.INVALID_HOLD_AMOUNT,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                accountCommandRepository
        );
    }

    @Test
    void shouldThrowWhenAccountNotFound() {
        IncreaseAccountHoldCommand command =
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
    }

    @Test
    void shouldThrowWhenAccountIsBlocked() {
        Account account = account(
                AccountStatus.BLOCKED,
                Currency.VND,
                new BigDecimal("1000000"),
                BigDecimal.ZERO
        );

        IncreaseAccountHoldCommand command =
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
                ErrorCode.ACCOUNT_NOT_ACTIVE,
                exception.getErrorCode()
        );

        assertEquals(
                BigDecimal.ZERO,
                account.getHoldAmount()
        );
    }

    @Test
    void shouldThrowWhenAccountIsClosed() {
        Account account = account(
                AccountStatus.CLOSED,
                Currency.VND,
                new BigDecimal("1000000"),
                BigDecimal.ZERO
        );

        IncreaseAccountHoldCommand command =
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
                ErrorCode.ACCOUNT_NOT_ACTIVE,
                exception.getErrorCode()
        );

        assertEquals(
                BigDecimal.ZERO,
                account.getHoldAmount()
        );
    }

    @Test
    void shouldThrowWhenAccountCurrencyDoesNotMatch() {
        Account account = account(
                AccountStatus.ACTIVE,
                Currency.VND,
                new BigDecimal("1000000"),
                BigDecimal.ZERO
        );

        IncreaseAccountHoldCommand command =
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
                BigDecimal.ZERO,
                account.getHoldAmount()
        );
    }

    @Test
    void shouldThrowWhenAvailableBalanceIsInsufficient() {
        Account account = account(
                AccountStatus.ACTIVE,
                Currency.VND,
                new BigDecimal("1000000"),
                new BigDecimal("700000")
        );

        IncreaseAccountHoldCommand command =
                command(
                        new BigDecimal("300001"),
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
                new BigDecimal("700000"),
                account.getHoldAmount()
        );
    }

    @Test
    void shouldThrowWhenAmountExceedsBalance() {
        Account account = account(
                AccountStatus.ACTIVE,
                Currency.VND,
                new BigDecimal("1000000"),
                BigDecimal.ZERO
        );

        IncreaseAccountHoldCommand command =
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
                BigDecimal.ZERO,
                account.getHoldAmount()
        );
    }

    @Test
    void shouldIncreaseHoldByAmountWhenExistingHoldIsPresent() {
        Account account = account(
                AccountStatus.ACTIVE,
                Currency.VND,
                new BigDecimal("2000000"),
                new BigDecimal("500000")
        );

        IncreaseAccountHoldCommand command =
                command(
                        new BigDecimal("750000"),
                        Currency.VND
                );

        givenAccount(account);

        handler.execute(command);

        assertEquals(
                new BigDecimal("1250000"),
                account.getHoldAmount()
        );
    }

    @Test
    void shouldAllowSmallestPositiveHoldAmount() {
        Account account = account(
                AccountStatus.ACTIVE,
                Currency.VND,
                new BigDecimal("1000000"),
                BigDecimal.ZERO
        );

        IncreaseAccountHoldCommand command =
                command(
                        new BigDecimal("0.0001"),
                        Currency.VND
                );

        givenAccount(account);

        handler.execute(command);

        assertEquals(
                new BigDecimal("0.0001"),
                account.getHoldAmount()
        );
    }

    private void givenAccount(Account account) {
        when(accountCommandRepository.findById(accountId))
                .thenReturn(Optional.of(account));
    }

    private IncreaseAccountHoldCommand command(
            BigDecimal amount,
            Currency currency
    ) {
        return new IncreaseAccountHoldCommand(
                accountId,
                amount,
                currency
        );
    }

    private Account account(
            AccountStatus status,
            Currency currency,
            BigDecimal balance,
            BigDecimal holdAmount
    ) {
        return Account.builder()
                .id(accountId)
                .productId(productId)
                .accountNo("1000000001")
                .currency(currency)
                .balance(balance)
                .holdAmount(holdAmount)
                .status(status)
                .version(0L)
                .ledgerAccountId(ledgerAccountId)
                .build();
    }
}