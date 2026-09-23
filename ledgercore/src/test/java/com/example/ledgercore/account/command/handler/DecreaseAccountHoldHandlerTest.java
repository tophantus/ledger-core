package com.example.ledgercore.account.command.handler;

import com.example.ledgercore.account.command.dto.DecreaseAccountHoldCommand;
import com.example.ledgercore.account.command.repository.AccountCommandRepository;
import com.example.ledgercore.account.entity.Account;
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
class DecreaseAccountHoldHandlerTest {

    @Mock
    private AccountCommandRepository accountCommandRepository;

    private DecreaseAccountHoldHandler handler;

    private UUID accountId;
    private UUID productId;
    private UUID ledgerAccountId;

    @BeforeEach
    void setUp() {
        handler = new DecreaseAccountHoldHandler(
                accountCommandRepository
        );

        accountId = UUID.randomUUID();
        productId = UUID.randomUUID();
        ledgerAccountId = UUID.randomUUID();
    }

    @Test
    void shouldDecreaseAccountHold() {
        Account account = account(
                Currency.VND,
                new BigDecimal("1000000"),
                new BigDecimal("300000")
        );

        DecreaseAccountHoldCommand command =
                command(
                        new BigDecimal("100000"),
                        Currency.VND
                );

        givenAccount(account);

        handler.execute(command);

        assertEquals(
                new BigDecimal("200000"),
                account.getHoldAmount()
        );
    }

    @Test
    void shouldDecreaseAccountHoldToZero() {
        Account account = account(
                Currency.VND,
                new BigDecimal("1000000"),
                new BigDecimal("300000")
        );

        DecreaseAccountHoldCommand command =
                command(
                        new BigDecimal("300000"),
                        Currency.VND
                );

        givenAccount(account);

        handler.execute(command);

        assertEquals(
                BigDecimal.ZERO,
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
        DecreaseAccountHoldCommand command =
                new DecreaseAccountHoldCommand(
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
        DecreaseAccountHoldCommand command =
                new DecreaseAccountHoldCommand(
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
        DecreaseAccountHoldCommand command =
                new DecreaseAccountHoldCommand(
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
        DecreaseAccountHoldCommand command =
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
        DecreaseAccountHoldCommand command =
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
        DecreaseAccountHoldCommand command =
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
    void shouldThrowWhenAccountCurrencyDoesNotMatch() {
        Account account = account(
                Currency.VND,
                new BigDecimal("1000000"),
                new BigDecimal("300000")
        );

        DecreaseAccountHoldCommand command =
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
                new BigDecimal("300000"),
                account.getHoldAmount()
        );
    }

    @Test
    void shouldThrowWhenHoldAmountIsInsufficient() {
        Account account = account(
                Currency.VND,
                new BigDecimal("1000000"),
                new BigDecimal("300000")
        );

        DecreaseAccountHoldCommand command =
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
                ErrorCode.ACCOUNT_HOLD_AMOUNT_INSUFFICIENT,
                exception.getErrorCode()
        );

        assertEquals(
                new BigDecimal("300000"),
                account.getHoldAmount()
        );
    }

    @Test
    void shouldThrowWhenAmountExceedsHoldAmount() {
        Account account = account(
                Currency.VND,
                new BigDecimal("1000000"),
                new BigDecimal("100000")
        );

        DecreaseAccountHoldCommand command =
                command(
                        new BigDecimal("100001"),
                        Currency.VND
                );

        givenAccount(account);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.ACCOUNT_HOLD_AMOUNT_INSUFFICIENT,
                exception.getErrorCode()
        );

        assertEquals(
                new BigDecimal("100000"),
                account.getHoldAmount()
        );
    }

    @Test
    void shouldAllowSmallestPositiveAmount() {
        Account account = account(
                Currency.VND,
                new BigDecimal("1000000"),
                new BigDecimal("100000")
        );

        DecreaseAccountHoldCommand command =
                command(
                        new BigDecimal("0.0001"),
                        Currency.VND
                );

        givenAccount(account);

        handler.execute(command);

        assertEquals(
                new BigDecimal("99999.9999"),
                account.getHoldAmount()
        );
    }

    private void givenAccount(Account account) {
        when(accountCommandRepository.findById(accountId))
                .thenReturn(Optional.of(account));
    }

    private DecreaseAccountHoldCommand command(
            BigDecimal amount,
            Currency currency
    ) {
        return new DecreaseAccountHoldCommand(
                accountId,
                amount,
                currency
        );
    }

    private Account account(
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
                .status(com.example.ledgercore.account.enums.AccountStatus.ACTIVE)
                .version(0L)
                .ledgerAccountId(ledgerAccountId)
                .build();
    }
}