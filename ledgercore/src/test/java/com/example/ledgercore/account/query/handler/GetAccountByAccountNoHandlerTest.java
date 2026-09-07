package com.example.ledgercore.account.query.handler;

import com.example.ledgercore.account.entity.Account;
import com.example.ledgercore.account.enums.AccountStatus;
import com.example.ledgercore.account.port.outbound.ProductAccountInfo;
import com.example.ledgercore.account.port.outbound.ProductAccountPort;
import com.example.ledgercore.account.query.dto.AccountResponse;
import com.example.ledgercore.account.query.dto.GetAccountByAccountNoQuery;
import com.example.ledgercore.account.query.repository.AccountQueryRepository;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAccountByAccountNoHandlerTest {

    @Mock
    private AccountQueryRepository accountQueryRepository;

    @Mock
    private ProductAccountPort productAccountPort;

    private GetAccountByAccountNoHandler handler;

    private UUID userId;
    private UUID accountId;
    private UUID productId;

    private String accountNo;
    private String productCode;

    @BeforeEach
    void setUp() {
        handler = new GetAccountByAccountNoHandler(
                accountQueryRepository,
                productAccountPort
        );

        userId = UUID.randomUUID();
        accountId = UUID.randomUUID();
        productId = UUID.randomUUID();

        accountNo = "1000000001";
        productCode = "CURRENT";
    }

    @Test
    void shouldReturnAccountWhenAccountExistsAndBelongsToUser() {
        Account account = account(
                accountId,
                userId,
                productId,
                accountNo,
                "VND",
                new BigDecimal("1000000"),
                AccountStatus.ACTIVE
        );

        ProductAccountInfo product =
                new ProductAccountInfo(
                        productId,
                        productCode
                );

        when(accountQueryRepository.findByAccountNo(accountNo))
                .thenReturn(Optional.of(account));

        when(productAccountPort.getActiveProduct(productId))
                .thenReturn(product);

        AccountResponse response =
                handler.execute(
                        new GetAccountByAccountNoQuery(
                                userId,
                                accountNo
                        )
                );

        assertAll(
                () -> assertEquals(
                        account.getId(),
                        response.id()
                ),
                () -> assertEquals(
                        account.getUserId(),
                        response.userId()
                ),
                () -> assertEquals(
                        account.getAccountNo(),
                        response.accountNo()
                ),
                () -> assertEquals(
                        productCode,
                        response.productCode()
                ),
                () -> assertEquals(
                        account.getCurrency(),
                        response.currency()
                ),
                () -> assertEquals(
                        account.getBalance().toPlainString(),
                        response.balance()
                ),
                () -> assertEquals(
                        account.getStatus(),
                        response.status()
                ),
                () -> assertEquals(
                        account.getCreatedAt(),
                        response.createdAt()
                ),
                () -> assertEquals(
                        account.getUpdatedAt(),
                        response.updatedAt()
                )
        );

        verify(accountQueryRepository)
                .findByAccountNo(accountNo);

        verify(productAccountPort)
                .getActiveProduct(productId);

        verifyNoMoreInteractions(
                accountQueryRepository,
                productAccountPort
        );
    }

    @Test
    void shouldThrowWhenAccountNotFound() {
        when(accountQueryRepository.findByAccountNo(accountNo))
                .thenReturn(Optional.empty());

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                new GetAccountByAccountNoQuery(
                                        userId,
                                        accountNo
                                )
                        )
                );

        assertEquals(
                ErrorCode.ACCOUNT_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(accountQueryRepository)
                .findByAccountNo(accountNo);

        verifyNoInteractions(productAccountPort);

        verifyNoMoreInteractions(accountQueryRepository);
    }

    @Test
    void shouldThrowWhenAccountDoesNotBelongToUser() {
        UUID ownerId = UUID.randomUUID();

        Account account = account(
                accountId,
                ownerId,
                productId,
                accountNo,
                "VND",
                new BigDecimal("1000000"),
                AccountStatus.ACTIVE
        );

        when(accountQueryRepository.findByAccountNo(accountNo))
                .thenReturn(Optional.of(account));

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                new GetAccountByAccountNoQuery(
                                        userId,
                                        accountNo
                                )
                        )
                );

        assertEquals(
                ErrorCode.ACCESS_DENIED,
                exception.getErrorCode()
        );

        verify(accountQueryRepository)
                .findByAccountNo(accountNo);

        verifyNoInteractions(productAccountPort);

        verifyNoMoreInteractions(accountQueryRepository);
    }

    @Test
    void shouldThrowWhenProductNotFound() {
        Account account = account(
                accountId,
                userId,
                productId,
                accountNo,
                "VND",
                new BigDecimal("1000000"),
                AccountStatus.ACTIVE
        );

        when(accountQueryRepository.findByAccountNo(accountNo))
                .thenReturn(Optional.of(account));

        when(productAccountPort.getActiveProduct(productId))
                .thenThrow(
                        new BusinessException(
                                ErrorCode.PRODUCT_NOT_FOUND
                        )
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                new GetAccountByAccountNoQuery(
                                        userId,
                                        accountNo
                                )
                        )
                );

        assertEquals(
                ErrorCode.PRODUCT_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(accountQueryRepository)
                .findByAccountNo(accountNo);

        verify(productAccountPort)
                .getActiveProduct(productId);

        verifyNoMoreInteractions(
                accountQueryRepository,
                productAccountPort
        );
    }

    @Test
    void shouldThrowWhenProductIsNotActive() {
        Account account = account(
                accountId,
                userId,
                productId,
                accountNo,
                "VND",
                new BigDecimal("1000000"),
                AccountStatus.ACTIVE
        );

        when(accountQueryRepository.findByAccountNo(accountNo))
                .thenReturn(Optional.of(account));

        when(productAccountPort.getActiveProduct(productId))
                .thenThrow(
                        new BusinessException(
                                ErrorCode.PRODUCT_NOT_ACTIVE
                        )
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                new GetAccountByAccountNoQuery(
                                        userId,
                                        accountNo
                                )
                        )
                );

        assertEquals(
                ErrorCode.PRODUCT_NOT_ACTIVE,
                exception.getErrorCode()
        );

        verify(accountQueryRepository)
                .findByAccountNo(accountNo);

        verify(productAccountPort)
                .getActiveProduct(productId);

        verifyNoMoreInteractions(
                accountQueryRepository,
                productAccountPort
        );
    }

    private Account account(
            UUID accountId,
            UUID userId,
            UUID productId,
            String accountNo,
            String currency,
            BigDecimal balance,
            AccountStatus status
    ) {
        Instant now = Instant.now();

        return Account.builder()
                .id(accountId)
                .userId(userId)
                .productId(productId)
                .accountNo(accountNo)
                .currency(currency)
                .balance(balance)
                .status(status)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}