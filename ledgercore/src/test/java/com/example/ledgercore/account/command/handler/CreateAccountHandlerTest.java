package com.example.ledgercore.account.command.handler;

import com.example.ledgercore.account.command.dto.CreateAccountCommand;
import com.example.ledgercore.account.command.port.outbound.AccountNumberGeneratorPort;
import com.example.ledgercore.account.command.port.outbound.LedgerAccountPort;
import com.example.ledgercore.account.command.port.outbound.UserAccountPort;
import com.example.ledgercore.account.command.repository.AccountCommandRepository;
import com.example.ledgercore.account.entity.Account;
import com.example.ledgercore.account.enums.AccountStatus;
import com.example.ledgercore.account.port.outbound.ProductAccountInfo;
import com.example.ledgercore.account.port.outbound.ProductAccountPort;
import com.example.ledgercore.account.query.dto.AccountResponse;
import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateAccountHandlerTest {

    @Mock
    private AccountCommandRepository accountCommandRepository;

    @Mock
    private AccountNumberGeneratorPort accountNumberGeneratorPort;

    @Mock
    private UserAccountPort userAccountPort;

    @Mock
    private LedgerAccountPort ledgerAccountPort;

    @Mock
    private ProductAccountPort productAccountPort;

    private CreateAccountHandler handler;

    private UUID userId;
    private UUID productId;
    private UUID ledgerAccountId;

    private String productCode;
    private String accountNo;
    private Currency currency;

    @BeforeEach
    void setUp() {
        handler = new CreateAccountHandler(
                accountCommandRepository,
                accountNumberGeneratorPort,
                userAccountPort,
                ledgerAccountPort,
                productAccountPort
        );

        userId = UUID.randomUUID();
        productId = UUID.randomUUID();
        ledgerAccountId = UUID.randomUUID();

        productCode = "CURRENT";
        accountNo = "1234567890";
        currency = Currency.VND;
    }

    @Test
    void shouldCreateAccount() {
        CreateAccountCommand command =
                new CreateAccountCommand(
                        userId,
                        productId,
                        currency
                );

        UUID accountId = UUID.randomUUID();
        Instant createdAt = Instant.now();
        Instant updatedAt = Instant.now();

        ProductAccountInfo product =
                new ProductAccountInfo(
                        productId,
                        productCode
                );

        Account savedAccount = Account.builder()
                .id(accountId)
                .userId(userId)
                .productId(productId)
                .accountNo(accountNo)
                .currency(currency)
                .ledgerAccountId(ledgerAccountId)
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        when(userAccountPort.existsById(userId))
                .thenReturn(true);

        when(productAccountPort.getActiveProduct(productId))
                .thenReturn(product);

        when(accountNumberGeneratorPort.generate())
                .thenReturn(accountNo);

        when(ledgerAccountPort.createCustomerAccount(
                accountNo,
                currency
        )).thenReturn(ledgerAccountId);

        when(accountCommandRepository.save(any(Account.class)))
                .thenReturn(savedAccount);

        AccountResponse response =
                handler.execute(command);

        assertAll(
                () -> assertEquals(
                        accountId,
                        response.id()
                ),
                () -> assertEquals(
                        userId,
                        response.userId()
                ),
                () -> assertEquals(
                        productId,
                        response.productId()
                ),
                () -> assertEquals(
                        accountNo,
                        response.accountNo()
                ),
                () -> assertEquals(
                        currency,
                        response.currency()
                ),
                () -> assertEquals(
                        BigDecimal.ZERO.toPlainString(),
                        response.balance()
                ),
                () -> assertEquals(
                        AccountStatus.ACTIVE,
                        response.status()
                ),
                () -> assertEquals(
                        createdAt,
                        response.createdAt()
                ),
                () -> assertEquals(
                        updatedAt,
                        response.updatedAt()
                )
        );

        verify(userAccountPort)
                .existsById(userId);

        verify(productAccountPort)
                .getActiveProduct(productId);

        verify(accountNumberGeneratorPort)
                .generate();

        verify(ledgerAccountPort)
                .createCustomerAccount(
                        accountNo,
                        currency
                );

        verify(accountCommandRepository)
                .save(any(Account.class));
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        CreateAccountCommand command =
                new CreateAccountCommand(
                        userId,
                        productId,
                        currency
                );

        when(userAccountPort.existsById(userId))
                .thenReturn(false);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.USER_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(userAccountPort)
                .existsById(userId);

        verifyNoInteractions(
                productAccountPort,
                accountNumberGeneratorPort,
                ledgerAccountPort,
                accountCommandRepository
        );
    }

    @Test
    void shouldThrowWhenProductNotActive() {
        CreateAccountCommand command =
                new CreateAccountCommand(
                        userId,
                        productId,
                        currency
                );

        when(userAccountPort.existsById(userId))
                .thenReturn(true);

        when(productAccountPort.getActiveProduct(productId))
                .thenThrow(
                        new BusinessException(
                                ErrorCode.PRODUCT_NOT_ACTIVE
                        )
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.PRODUCT_NOT_ACTIVE,
                exception.getErrorCode()
        );

        verify(userAccountPort)
                .existsById(userId);

        verify(productAccountPort)
                .getActiveProduct(productId);

        verifyNoInteractions(
                accountNumberGeneratorPort,
                ledgerAccountPort,
                accountCommandRepository
        );
    }

    @Test
    void shouldThrowWhenProductNotFound() {
        CreateAccountCommand command =
                new CreateAccountCommand(
                        userId,
                        productId,
                        currency
                );

        when(userAccountPort.existsById(userId))
                .thenReturn(true);

        when(productAccountPort.getActiveProduct(productId))
                .thenThrow(
                        new BusinessException(
                                ErrorCode.PRODUCT_NOT_FOUND
                        )
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.PRODUCT_NOT_FOUND,
                exception.getErrorCode()
        );

        verify(userAccountPort)
                .existsById(userId);

        verify(productAccountPort)
                .getActiveProduct(productId);

        verifyNoInteractions(
                accountNumberGeneratorPort,
                ledgerAccountPort,
                accountCommandRepository
        );
    }

    @Test
    void shouldCreateCustomerLedgerAccountWithGeneratedAccountNumber() {
        CreateAccountCommand command =
                new CreateAccountCommand(
                        userId,
                        productId,
                        currency
                );

        ProductAccountInfo product =
                new ProductAccountInfo(
                        productId,
                        productCode
                );

        when(userAccountPort.existsById(userId))
                .thenReturn(true);

        when(productAccountPort.getActiveProduct(productId))
                .thenReturn(product);

        when(accountNumberGeneratorPort.generate())
                .thenReturn(accountNo);

        when(ledgerAccountPort.createCustomerAccount(
                accountNo,
                currency
        )).thenReturn(ledgerAccountId);

        Account savedAccount = Account.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .productId(productId)
                .accountNo(accountNo)
                .currency(currency)
                .ledgerAccountId(ledgerAccountId)
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .build();

        when(accountCommandRepository.save(any(Account.class)))
                .thenReturn(savedAccount);

        handler.execute(command);

        verify(ledgerAccountPort)
                .createCustomerAccount(
                        accountNo,
                        currency
                );
    }

    @Test
    void shouldSaveAccountWithCorrectData() {
        CreateAccountCommand command =
                new CreateAccountCommand(
                        userId,
                        productId,
                        currency
                );

        ProductAccountInfo product =
                new ProductAccountInfo(
                        productId,
                        productCode
                );

        when(userAccountPort.existsById(userId))
                .thenReturn(true);

        when(productAccountPort.getActiveProduct(productId))
                .thenReturn(product);

        when(accountNumberGeneratorPort.generate())
                .thenReturn(accountNo);

        when(ledgerAccountPort.createCustomerAccount(
                accountNo,
                currency
        )).thenReturn(ledgerAccountId);

        Account savedAccount = Account.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .productId(productId)
                .accountNo(accountNo)
                .currency(currency)
                .ledgerAccountId(ledgerAccountId)
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .build();

        when(accountCommandRepository.save(any(Account.class)))
                .thenReturn(savedAccount);

        handler.execute(command);

        ArgumentCaptor<Account> captor =
                ArgumentCaptor.forClass(Account.class);

        verify(accountCommandRepository)
                .save(captor.capture());

        Account account =
                captor.getValue();

        assertAll(
                () -> assertEquals(
                        userId,
                        account.getUserId()
                ),
                () -> assertEquals(
                        productId,
                        account.getProductId()
                ),
                () -> assertEquals(
                        accountNo,
                        account.getAccountNo()
                ),
                () -> assertEquals(
                        currency,
                        account.getCurrency()
                ),
                () -> assertEquals(
                        ledgerAccountId,
                        account.getLedgerAccountId()
                )
        );
    }
}