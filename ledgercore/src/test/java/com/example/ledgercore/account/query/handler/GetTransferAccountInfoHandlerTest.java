package com.example.ledgercore.account.query.handler;

import com.example.ledgercore.account.entity.Account;
import com.example.ledgercore.account.enums.AccountStatus;
import com.example.ledgercore.account.query.dto.AccountTransferInfo;
import com.example.ledgercore.account.query.repository.AccountQueryRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetTransferAccountInfoHandlerTest {

    @Mock
    private AccountQueryRepository accountQueryRepository;

    private GetTransferAccountInfoHandler handler;

    private UUID userId;
    private UUID sourceAccountId;
    private UUID destinationAccountId;

    @BeforeEach
    void setUp() {
        handler = new GetTransferAccountInfoHandler(
                accountQueryRepository
        );

        userId = UUID.randomUUID();
        sourceAccountId = UUID.randomUUID();
        destinationAccountId = UUID.randomUUID();
    }

    @Test
    void shouldReturnTransferInfoWhenBothAccountsAreActive() {
        Account sourceAccount = Account.builder()
                .id(sourceAccountId)
                .userId(userId)
                .currency("VND")
                .balance(new BigDecimal("1000000"))
                .status(AccountStatus.ACTIVE)
                .build();

        Account destinationAccount = Account.builder()
                .id(destinationAccountId)
                .userId(UUID.randomUUID())
                .currency("VND")
                .balance(new BigDecimal("500000"))
                .status(AccountStatus.ACTIVE)
                .build();

        when(accountQueryRepository
                .findByIdAndUserId(
                        sourceAccountId,
                        userId
                ))
                .thenReturn(Optional.of(sourceAccount));

        when(accountQueryRepository
                .findById(destinationAccountId))
                .thenReturn(Optional.of(destinationAccount));

        AccountTransferInfo response =
                handler.execute(
                        userId,
                        sourceAccountId,
                        destinationAccountId
                );

        assertEquals(
                sourceAccountId,
                response.sourceAccountId()
        );

        assertEquals(
                destinationAccountId,
                response.destinationAccountId()
        );

        assertEquals(
                "VND",
                response.currency()
        );

        assertEquals(
                new BigDecimal("1000000"),
                response.sourceBalance()
        );

        verify(accountQueryRepository)
                .findByIdAndUserId(
                        sourceAccountId,
                        userId
                );

        verify(accountQueryRepository)
                .findById(destinationAccountId);

        verifyNoMoreInteractions(accountQueryRepository);
    }

    @Test
    void shouldThrowWhenSourceAccountNotFound() {
        when(accountQueryRepository
                .findByIdAndUserId(
                        sourceAccountId,
                        userId
                ))
                .thenReturn(Optional.empty());

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

        verify(accountQueryRepository)
                .findByIdAndUserId(
                        sourceAccountId,
                        userId
                );

        verify(accountQueryRepository, never())
                .findById(destinationAccountId);
    }

    @Test
    void shouldThrowWhenDestinationAccountNotFound() {
        Account sourceAccount = Account.builder()
                .id(sourceAccountId)
                .userId(userId)
                .currency("VND")
                .balance(new BigDecimal("1000000"))
                .status(AccountStatus.ACTIVE)
                .build();

        when(accountQueryRepository
                .findByIdAndUserId(
                        sourceAccountId,
                        userId
                ))
                .thenReturn(Optional.of(sourceAccount));

        when(accountQueryRepository
                .findById(destinationAccountId))
                .thenReturn(Optional.empty());

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

        verify(accountQueryRepository)
                .findByIdAndUserId(
                        sourceAccountId,
                        userId
                );

        verify(accountQueryRepository)
                .findById(destinationAccountId);
    }

    @Test
    void shouldThrowWhenSourceAccountIsNotActive() {
        Account sourceAccount = Account.builder()
                .id(sourceAccountId)
                .userId(userId)
                .currency("VND")
                .balance(new BigDecimal("1000000"))
                .status(AccountStatus.BLOCKED)
                .build();

        Account destinationAccount = Account.builder()
                .id(destinationAccountId)
                .currency("VND")
                .status(AccountStatus.ACTIVE)
                .build();

        when(accountQueryRepository
                .findByIdAndUserId(
                        sourceAccountId,
                        userId
                ))
                .thenReturn(Optional.of(sourceAccount));

        when(accountQueryRepository
                .findById(destinationAccountId))
                .thenReturn(Optional.of(destinationAccount));

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
    }

    @Test
    void shouldThrowWhenDestinationAccountIsNotActive() {
        Account sourceAccount = Account.builder()
                .id(sourceAccountId)
                .userId(userId)
                .currency("VND")
                .balance(new BigDecimal("1000000"))
                .status(AccountStatus.ACTIVE)
                .build();

        Account destinationAccount = Account.builder()
                .id(destinationAccountId)
                .currency("VND")
                .status(AccountStatus.BLOCKED)
                .build();

        when(accountQueryRepository
                .findByIdAndUserId(
                        sourceAccountId,
                        userId
                ))
                .thenReturn(Optional.of(sourceAccount));

        when(accountQueryRepository
                .findById(destinationAccountId))
                .thenReturn(Optional.of(destinationAccount));

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
    }
}