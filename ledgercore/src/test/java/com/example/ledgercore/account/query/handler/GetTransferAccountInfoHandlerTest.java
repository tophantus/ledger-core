package com.example.ledgercore.account.query.handler;

import com.example.ledgercore.account.entity.Account;
import com.example.ledgercore.account.enums.AccountStatus;
import com.example.ledgercore.account.query.dto.AccountTransferInfo;
import com.example.ledgercore.account.query.repository.AccountQueryRepository;
import com.example.ledgercore.account.query.service.GetUserAccountService;
import com.example.ledgercore.account.query.service.dto.GetUserAccountCriteria;
import com.example.ledgercore.account.query.service.dto.GetUserAccountResult;
import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTransferAccountInfoHandlerTest {

    @Mock
    private GetUserAccountService getUserAccountService;

    @Mock
    private AccountQueryRepository accountQueryRepository;

    private GetTransferAccountInfoHandler handler;

    private UUID userId;
    private UUID sourceAccountId;
    private UUID destinationAccountId;
    private UUID sourceProductId;
    private UUID destinationProductId;
    private UUID sourceLedgerAccountId;
    private UUID destinationLedgerAccountId;

    @BeforeEach
    void setUp() {
        handler = new GetTransferAccountInfoHandler(
                getUserAccountService,
                accountQueryRepository
        );

        userId = UUID.randomUUID();
        sourceAccountId = UUID.randomUUID();
        destinationAccountId = UUID.randomUUID();
        sourceProductId = UUID.randomUUID();
        destinationProductId = UUID.randomUUID();
        sourceLedgerAccountId = UUID.randomUUID();
        destinationLedgerAccountId = UUID.randomUUID();
    }

    @Test
    void shouldReturnTransferInfoWhenBothAccountsAreActive() {
        GetUserAccountResult sourceAccount =
                sourceAccount(
                        userId,
                        Currency.VND,
                        new BigDecimal("1000000"),
                        new BigDecimal("200000"),
                        AccountStatus.ACTIVE
                );

        Account destinationAccount =
                destinationAccount(
                        destinationAccountId,
                        AccountStatus.ACTIVE
                );

        givenSourceAccount(sourceAccount);

        when(accountQueryRepository.findById(destinationAccountId))
                .thenReturn(
                        java.util.Optional.of(destinationAccount)
                );

        AccountTransferInfo response =
                handler.execute(
                        userId,
                        sourceAccountId,
                        destinationAccountId
                );

        assertAll(
                () -> assertEquals(
                        sourceAccountId,
                        response.sourceAccountId()
                ),
                () -> assertEquals(
                        destinationAccountId,
                        response.destinationAccountId()
                ),
                () -> assertEquals(
                        Currency.VND,
                        response.currency()
                ),
                () -> assertEquals(
                        new BigDecimal("800000"),
                        response.sourceAvailableBalance()
                )
        );

        verify(getUserAccountService)
                .execute(
                        new GetUserAccountCriteria(
                                sourceAccountId
                        )
                );

        verify(accountQueryRepository)
                .findById(destinationAccountId);

        verifyNoMoreInteractions(
                getUserAccountService,
                accountQueryRepository
        );
    }

    @Test
    void shouldThrowWhenSourceAccountDoesNotBelongToUser() {
        UUID ownerId = UUID.randomUUID();

        GetUserAccountResult sourceAccount =
                sourceAccount(
                        ownerId,
                        Currency.VND,
                        new BigDecimal("1000000"),
                        BigDecimal.ZERO,
                        AccountStatus.ACTIVE
                );

        givenSourceAccount(sourceAccount);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                userId,
                                sourceAccountId,
                                destinationAccountId
                        )
                );

        assertEquals(
                ErrorCode.ACCESS_DENIED,
                exception.getErrorCode()
        );

        verify(getUserAccountService)
                .execute(
                        new GetUserAccountCriteria(
                                sourceAccountId
                        )
                );

        verify(
                accountQueryRepository,
                never()
        ).findById(destinationAccountId);
    }

    @Test
    void shouldThrowWhenSourceAccountNotFound() {
        when(getUserAccountService.execute(
                new GetUserAccountCriteria(
                        sourceAccountId
                )
        )).thenThrow(
                new BusinessException(
                        ErrorCode.ACCOUNT_NOT_FOUND
                )
        );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                userId,
                                sourceAccountId,
                                destinationAccountId
                        )
                );

        assertEquals(
                ErrorCode.ACCOUNT_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(getUserAccountService)
                .execute(
                        new GetUserAccountCriteria(
                                sourceAccountId
                        )
                );

        verify(
                accountQueryRepository,
                never()
        ).findById(destinationAccountId);
    }

    @Test
    void shouldThrowWhenDestinationAccountNotFound() {
        GetUserAccountResult sourceAccount =
                sourceAccount(
                        userId,
                        Currency.VND,
                        new BigDecimal("1000000"),
                        BigDecimal.ZERO,
                        AccountStatus.ACTIVE
                );

        givenSourceAccount(sourceAccount);

        when(accountQueryRepository.findById(destinationAccountId))
                .thenReturn(java.util.Optional.empty());

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                userId,
                                sourceAccountId,
                                destinationAccountId
                        )
                );

        assertEquals(
                ErrorCode.ACCOUNT_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(getUserAccountService)
                .execute(
                        new GetUserAccountCriteria(
                                sourceAccountId
                        )
                );

        verify(accountQueryRepository)
                .findById(destinationAccountId);
    }

    @Test
    void shouldThrowWhenSourceAccountIsNotActive() {
        GetUserAccountResult sourceAccount =
                sourceAccount(
                        userId,
                        Currency.VND,
                        new BigDecimal("1000000"),
                        BigDecimal.ZERO,
                        AccountStatus.BLOCKED
                );

        Account destinationAccount =
                destinationAccount(
                        destinationAccountId,
                        AccountStatus.ACTIVE
                );

        givenSourceAccount(sourceAccount);

        when(accountQueryRepository.findById(destinationAccountId))
                .thenReturn(
                        java.util.Optional.of(destinationAccount)
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                userId,
                                sourceAccountId,
                                destinationAccountId
                        )
                );

        assertEquals(
                ErrorCode.ACCOUNT_NOT_ACTIVE,
                exception.getErrorCode()
        );

        verify(getUserAccountService)
                .execute(
                        new GetUserAccountCriteria(
                                sourceAccountId
                        )
                );

        verify(accountQueryRepository)
                .findById(destinationAccountId);
    }

    @Test
    void shouldThrowWhenDestinationAccountIsNotActive() {
        GetUserAccountResult sourceAccount =
                sourceAccount(
                        userId,
                        Currency.VND,
                        new BigDecimal("1000000"),
                        BigDecimal.ZERO,
                        AccountStatus.ACTIVE
                );

        Account destinationAccount =
                destinationAccount(
                        destinationAccountId,
                        AccountStatus.BLOCKED
                );

        givenSourceAccount(sourceAccount);

        when(accountQueryRepository.findById(destinationAccountId))
                .thenReturn(
                        java.util.Optional.of(destinationAccount)
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                userId,
                                sourceAccountId,
                                destinationAccountId
                        )
                );

        assertEquals(
                ErrorCode.ACCOUNT_NOT_ACTIVE,
                exception.getErrorCode()
        );

        verify(getUserAccountService)
                .execute(
                        new GetUserAccountCriteria(
                                sourceAccountId
                        )
                );

        verify(accountQueryRepository)
                .findById(destinationAccountId);
    }

    @Test
    void shouldReturnAvailableBalanceAfterSubtractingHoldAmount() {
        GetUserAccountResult sourceAccount =
                sourceAccount(
                        userId,
                        Currency.VND,
                        new BigDecimal("1000000"),
                        new BigDecimal("350000"),
                        AccountStatus.ACTIVE
                );

        Account destinationAccount =
                destinationAccount(
                        destinationAccountId,
                        AccountStatus.ACTIVE
                );

        givenSourceAccount(sourceAccount);

        when(accountQueryRepository.findById(destinationAccountId))
                .thenReturn(
                        java.util.Optional.of(destinationAccount)
                );

        AccountTransferInfo response =
                handler.execute(
                        userId,
                        sourceAccountId,
                        destinationAccountId
                );

        assertEquals(
                new BigDecimal("650000"),
                response.sourceAvailableBalance()
        );
    }

    private void givenSourceAccount(
            GetUserAccountResult sourceAccount
    ) {
        when(getUserAccountService.execute(
                new GetUserAccountCriteria(
                        sourceAccountId
                )
        )).thenReturn(sourceAccount);
    }

    private GetUserAccountResult sourceAccount(
            UUID ownerId,
            Currency currency,
            BigDecimal balance,
            BigDecimal holdAmount,
            AccountStatus status
    ) {
        Instant now = Instant.now();

        return new GetUserAccountResult(
                sourceAccountId,
                ownerId,
                sourceProductId,
                "1000000001",
                currency,
                balance,
                holdAmount,
                status,
                0L,
                sourceLedgerAccountId,
                now,
                now
        );
    }

    private Account destinationAccount(
            UUID accountId,
            AccountStatus status
    ) {
        return Account.builder()
                .id(accountId)
                .productId(destinationProductId)
                .accountNo("2000000001")
                .currency(Currency.VND)
                .balance(new BigDecimal("500000"))
                .holdAmount(BigDecimal.ZERO)
                .status(status)
                .version(0L)
                .ledgerAccountId(destinationLedgerAccountId)
                .build();
    }
}