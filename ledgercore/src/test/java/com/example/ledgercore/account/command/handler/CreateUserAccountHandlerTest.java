package com.example.ledgercore.account.command.handler;

import com.example.ledgercore.account.command.dto.CreatUserAccountCommand;
import com.example.ledgercore.account.command.port.outbound.AccountNumberGeneratorPort;
import com.example.ledgercore.account.command.port.outbound.LedgerAccountPort;
import com.example.ledgercore.account.command.port.outbound.UserAccountPort;
import com.example.ledgercore.account.command.repository.AccountCommandRepository;
import com.example.ledgercore.account.command.repository.UserAccountCommandRepository;
import com.example.ledgercore.account.entity.Account;
import com.example.ledgercore.account.entity.UserAccount;
import com.example.ledgercore.account.enums.AccountStatus;
import com.example.ledgercore.account.port.outbound.ProductAccountPort;
import com.example.ledgercore.account.port.outbound.dto.ProductAccountInfo;
import com.example.ledgercore.account.query.dto.AccountResponse;
import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.product.enums.ProductType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateUserAccountHandlerTest {

    @Mock
    private AccountCommandRepository accountCommandRepository;

    @Mock
    private UserAccountCommandRepository userAccountCommandRepository;

    @Mock
    private AccountNumberGeneratorPort accountNumberGeneratorPort;

    @Mock
    private UserAccountPort userAccountPort;

    @Mock
    private LedgerAccountPort ledgerAccountPort;

    @Mock
    private ProductAccountPort productAccountPort;

    private CreateUserAccountHandler handler;

    private UUID userId;
    private UUID productId;
    private UUID ledgerAccountId;

    private String productCode;
    private String accountNo;
    private Currency currency;

    @BeforeEach
    void setUp() {
        handler = new CreateUserAccountHandler(
                accountCommandRepository,
                userAccountCommandRepository,
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
    void shouldCreateUserAccount() {
        CreatUserAccountCommand command = validCommand();

        UUID accountId = UUID.randomUUID();
        Instant createdAt = Instant.parse(
                "2026-01-01T00:00:00Z"
        );
        Instant updatedAt = Instant.parse(
                "2026-01-01T01:00:00Z"
        );

        ProductAccountInfo product =
                givenDepositProduct();

        Account savedAccount =
                Account.builder()
                        .id(accountId)
                        .productId(productId)
                        .accountNo(accountNo)
                        .currency(currency)
                        .balance(BigDecimal.ZERO)
                        .holdAmount(BigDecimal.ZERO)
                        .status(AccountStatus.ACTIVE)
                        .version(0L)
                        .createdAt(createdAt)
                        .updatedAt(updatedAt)
                        .ledgerAccountId(ledgerAccountId)
                        .build();

        givenUserExists();
        givenAccountCreation(product, savedAccount);

        AccountResponse response =
                handler.execute(command);

        assertAll(
                () -> assertEquals(
                        accountId,
                        response.id()
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

        verify(userAccountCommandRepository)
                .save(any(UserAccount.class));
    }

    @Test
    void shouldSaveAccountWithCorrectData() {
        CreatUserAccountCommand command =
                validCommand();

        ProductAccountInfo product =
                givenDepositProduct();

        Account savedAccount =
                createSavedAccount();

        givenUserExists();
        givenAccountCreation(
                product,
                savedAccount
        );

        handler.execute(command);

        ArgumentCaptor<Account> captor =
                ArgumentCaptor.forClass(Account.class);

        verify(accountCommandRepository)
                .save(captor.capture());

        Account account =
                captor.getValue();

        assertAll(
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
                ),
                () -> assertEquals(
                        BigDecimal.ZERO,
                        account.getBalance()
                ),
                () -> assertEquals(
                        BigDecimal.ZERO,
                        account.getHoldAmount()
                ),
                () -> assertEquals(
                        AccountStatus.ACTIVE,
                        account.getStatus()
                ),
                () -> assertEquals(
                        0L,
                        account.getVersion()
                )
        );
    }

    @Test
    void shouldSaveUserAccountWithCorrectData() {
        CreatUserAccountCommand command =
                validCommand();

        ProductAccountInfo product =
                givenDepositProduct();

        UUID accountId =
                UUID.randomUUID();

        Account savedAccount =
                createSavedAccount(accountId);

        givenUserExists();
        givenAccountCreation(
                product,
                savedAccount
        );

        handler.execute(command);

        ArgumentCaptor<UserAccount> captor =
                ArgumentCaptor.forClass(UserAccount.class);

        verify(userAccountCommandRepository)
                .save(captor.capture());

        UserAccount userAccount =
                captor.getValue();

        assertAll(
                () -> assertEquals(
                        userId,
                        userAccount.getUserId()
                ),
                () -> assertEquals(
                        accountId,
                        userAccount.getAccountId()
                )
        );
    }

    @Test
    void shouldCreateCustomerLedgerAccountWithGeneratedAccountNumber() {
        CreatUserAccountCommand command =
                validCommand();

        ProductAccountInfo product =
                givenDepositProduct();

        Account savedAccount =
                createSavedAccount();

        givenUserExists();
        givenAccountCreation(
                product,
                savedAccount
        );

        handler.execute(command);

        verify(accountNumberGeneratorPort)
                .generate();

        verify(ledgerAccountPort)
                .createCustomerAccount(
                        accountNo,
                        currency
                );
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        CreatUserAccountCommand command =
                validCommand();

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
                accountCommandRepository,
                userAccountCommandRepository
        );
    }

    @Test
    void shouldThrowWhenProductNotFound() {
        CreatUserAccountCommand command =
                validCommand();

        givenUserExists();

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
                accountCommandRepository,
                userAccountCommandRepository
        );
    }

    @Test
    void shouldThrowWhenProductNotActive() {
        CreatUserAccountCommand command =
                validCommand();

        givenUserExists();

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
                accountCommandRepository,
                userAccountCommandRepository
        );
    }

    @Test
    void shouldThrowWhenProductIsNotDeposit() {
        CreatUserAccountCommand command =
                validCommand();

        ProductAccountInfo product =
                new ProductAccountInfo(
                        productId,
                        productCode,
                        ProductType.CREDIT
                );

        givenUserExists();

        when(productAccountPort.getActiveProduct(productId))
                .thenReturn(product);

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.ACCOUNT_PRODUCT_TYPE_INVALID,
                exception.getErrorCode()
        );

        verify(userAccountPort)
                .existsById(userId);

        verify(productAccountPort)
                .getActiveProduct(productId);

        verifyNoInteractions(
                accountNumberGeneratorPort,
                ledgerAccountPort,
                accountCommandRepository,
                userAccountCommandRepository
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
                userAccountPort,
                productAccountPort,
                accountNumberGeneratorPort,
                ledgerAccountPort,
                accountCommandRepository,
                userAccountCommandRepository
        );
    }

    @Test
    void shouldThrowWhenUserIdIsNull() {
        CreatUserAccountCommand command =
                new CreatUserAccountCommand(
                        null,
                        productId,
                        currency
                );

        assertInvalidRequest(command);
    }

    @Test
    void shouldThrowWhenProductIdIsNull() {
        CreatUserAccountCommand command =
                new CreatUserAccountCommand(
                        userId,
                        null,
                        currency
                );

        assertInvalidRequest(command);
    }

    @Test
    void shouldThrowWhenCurrencyIsNull() {
        CreatUserAccountCommand command =
                new CreatUserAccountCommand(
                        userId,
                        productId,
                        null
                );

        assertInvalidRequest(command);
    }

    private CreatUserAccountCommand validCommand() {
        return new CreatUserAccountCommand(
                userId,
                productId,
                currency
        );
    }

    private ProductAccountInfo givenDepositProduct() {
        return new ProductAccountInfo(
                productId,
                productCode,
                ProductType.DEPOSIT
        );
    }

    private void givenUserExists() {
        when(userAccountPort.existsById(userId))
                .thenReturn(true);
    }

    private void givenAccountCreation(
            ProductAccountInfo product,
            Account savedAccount
    ) {
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
    }

    private Account createSavedAccount() {
        return createSavedAccount(
                UUID.randomUUID()
        );
    }

    private Account createSavedAccount(
            UUID accountId
    ) {
        return Account.builder()
                .id(accountId)
                .productId(productId)
                .accountNo(accountNo)
                .currency(currency)
                .balance(BigDecimal.ZERO)
                .holdAmount(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .version(0L)
                .createdAt(
                        Instant.parse(
                                "2026-01-01T00:00:00Z"
                        )
                )
                .updatedAt(
                        Instant.parse(
                                "2026-01-01T00:00:00Z"
                        )
                )
                .ledgerAccountId(ledgerAccountId)
                .build();
    }

    private void assertInvalidRequest(
            CreatUserAccountCommand command
    ) {
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
                userAccountPort,
                productAccountPort,
                accountNumberGeneratorPort,
                ledgerAccountPort,
                accountCommandRepository,
                userAccountCommandRepository
        );
    }
}