package com.example.ledgercore.hold.command.handler;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.hold.command.dto.CreateAccountHoldCommand;
import com.example.ledgercore.hold.command.dto.CreateAccountHoldResponse;
import com.example.ledgercore.hold.command.port.outbound.AccountHoldPort;
import com.example.ledgercore.hold.command.repository.AccountHoldCommandRepository;
import com.example.ledgercore.hold.entity.AccountHold;
import com.example.ledgercore.hold.enums.AccountHoldReferenceType;
import com.example.ledgercore.hold.enums.AccountHoldStatus;
import com.example.ledgercore.hold.enums.AccountHoldType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateAccountHoldHandlerTest {

    @Mock
    private AccountHoldCommandRepository
            accountHoldCommandRepository;

    @Mock
    private AccountHoldPort accountHoldPort;

    private CreateAccountHoldHandler handler;

    private UUID accountId;
    private UUID referenceId;
    private UUID holdId;

    @BeforeEach
    void setUp() {
        handler = new CreateAccountHoldHandler(
                accountHoldCommandRepository,
                accountHoldPort
        );

        accountId = UUID.randomUUID();
        referenceId = UUID.randomUUID();
        holdId = UUID.randomUUID();
    }

    @Test
    void shouldCreateAccountHoldSuccessfully() {

        CreateAccountHoldCommand command =
                command(
                        "100000",
                        Currency.VND
                );

        mockSaveHold();

        CreateAccountHoldResponse response =
                handler.execute(command);

        assertNotNull(response);

        assertEquals(
                holdId,
                response.holdId()
        );

        assertEquals(
                accountId,
                response.accountId()
        );

        assertEquals(
                new BigDecimal("100000"),
                response.amount()
        );

        assertEquals(
                Currency.VND,
                response.currency()
        );

        assertEquals(
                AccountHoldStatus.ACTIVE,
                response.status()
        );

        assertNull(
                response.expiresAt()
        );

        ArgumentCaptor<AccountHold> captor =
                ArgumentCaptor.forClass(AccountHold.class);

        verify(accountHoldCommandRepository)
                .save(captor.capture());

        AccountHold hold = captor.getValue();

        assertEquals(
                accountId,
                hold.getAccountId()
        );

        assertEquals(
                new BigDecimal("100000"),
                hold.getAmount()
        );

        assertEquals(
                Currency.VND,
                hold.getCurrency()
        );

        assertEquals(
                AccountHoldType.AVAILABLE_BALANCE_RESERVATION,
                hold.getHoldType()
        );

        assertEquals(
                AccountHoldReferenceType.WITHDRAWAL_INTENT,
                hold.getReferenceType()
        );

        assertEquals(
                referenceId,
                hold.getReferenceId()
        );

        assertEquals(
                AccountHoldStatus.ACTIVE,
                hold.getStatus()
        );

        verify(accountHoldPort)
                .increaseHold(
                        accountId,
                        new BigDecimal("100000"),
                        Currency.VND
                );
    }

    @Test
    void shouldCreateHoldAsActive() {

        CreateAccountHoldCommand command =
                command(
                        "100000",
                        Currency.VND
                );

        mockSaveHold();

        handler.execute(command);

        ArgumentCaptor<AccountHold> captor =
                ArgumentCaptor.forClass(AccountHold.class);

        verify(accountHoldCommandRepository)
                .save(captor.capture());

        assertEquals(
                AccountHoldStatus.ACTIVE,
                captor.getValue().getStatus()
        );
    }

    @Test
    void shouldPreserveHoldTypeAndReference() {

        CreateAccountHoldCommand command =
                new CreateAccountHoldCommand(
                        accountId,
                        new BigDecimal("100000"),
                        Currency.VND,
                        AccountHoldType.COLLATERAL,
                        AccountHoldReferenceType.CARD_AUTHORIZATION,
                        referenceId
                );

        mockSaveHold();

        handler.execute(command);

        ArgumentCaptor<AccountHold> captor =
                ArgumentCaptor.forClass(AccountHold.class);

        verify(accountHoldCommandRepository)
                .save(captor.capture());

        AccountHold hold = captor.getValue();

        assertEquals(
                AccountHoldType.COLLATERAL,
                hold.getHoldType()
        );

        assertEquals(
                AccountHoldReferenceType.CARD_AUTHORIZATION,
                hold.getReferenceType()
        );

        assertEquals(
                referenceId,
                hold.getReferenceId()
        );
    }

    @Test
    void shouldIncreaseAccountHoldAfterSavingHold() {

        CreateAccountHoldCommand command =
                command(
                        "100000",
                        Currency.VND
                );

        mockSaveHold();

        var inOrder = inOrder(
                accountHoldCommandRepository,
                accountHoldPort
        );

        handler.execute(command);

        inOrder.verify(accountHoldCommandRepository)
                .save(any(AccountHold.class));

        inOrder.verify(accountHoldPort)
                .increaseHold(
                        accountId,
                        new BigDecimal("100000"),
                        Currency.VND
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
                accountHoldCommandRepository,
                accountHoldPort
        );
    }

    @Test
    void shouldThrowWhenAccountIdIsNull() {

        CreateAccountHoldCommand command =
                new CreateAccountHoldCommand(
                        null,
                        new BigDecimal("100000"),
                        Currency.VND,
                        AccountHoldType.AVAILABLE_BALANCE_RESERVATION,
                        AccountHoldReferenceType.WITHDRAWAL_INTENT,
                        referenceId
                );

        assertInvalidRequest(command);
    }

    @Test
    void shouldThrowWhenAmountIsNull() {

        CreateAccountHoldCommand command =
                new CreateAccountHoldCommand(
                        accountId,
                        null,
                        Currency.VND,
                        AccountHoldType.AVAILABLE_BALANCE_RESERVATION,
                        AccountHoldReferenceType.WITHDRAWAL_INTENT,
                        referenceId
                );

        assertInvalidRequest(command);
    }

    @Test
    void shouldThrowWhenAmountScaleExceedsCurrencyScale() {

        CreateAccountHoldCommand command =
                command(
                        "100.1",
                        Currency.VND
                );

        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(command)
                );

        assertEquals(
                ErrorCode.INVALID_CURRENCY_AMOUNT,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                accountHoldCommandRepository,
                accountHoldPort
        );
    }

    @Test
    void shouldThrowWhenCurrencyIsNull() {

        CreateAccountHoldCommand command =
                new CreateAccountHoldCommand(
                        accountId,
                        new BigDecimal("100000"),
                        null,
                        AccountHoldType.AVAILABLE_BALANCE_RESERVATION,
                        AccountHoldReferenceType.WITHDRAWAL_INTENT,
                        referenceId
                );

        assertInvalidRequest(command);
    }

    @Test
    void shouldThrowWhenHoldTypeIsNull() {

        CreateAccountHoldCommand command =
                new CreateAccountHoldCommand(
                        accountId,
                        new BigDecimal("100000"),
                        Currency.VND,
                        null,
                        AccountHoldReferenceType.WITHDRAWAL_INTENT,
                        referenceId
                );

        assertInvalidRequest(command);
    }

    @Test
    void shouldThrowWhenReferenceTypeIsNull() {

        CreateAccountHoldCommand command =
                new CreateAccountHoldCommand(
                        accountId,
                        new BigDecimal("100000"),
                        Currency.VND,
                        AccountHoldType.AVAILABLE_BALANCE_RESERVATION,
                        null,
                        referenceId
                );

        assertInvalidRequest(command);
    }

    @Test
    void shouldThrowWhenReferenceIdIsNull() {

        CreateAccountHoldCommand command =
                new CreateAccountHoldCommand(
                        accountId,
                        new BigDecimal("100000"),
                        Currency.VND,
                        AccountHoldType.AVAILABLE_BALANCE_RESERVATION,
                        AccountHoldReferenceType.WITHDRAWAL_INTENT,
                        null
                );

        assertInvalidRequest(command);
    }

    @Test
    void shouldThrowWhenAmountIsZero() {

        CreateAccountHoldCommand command =
                command(
                        "0",
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
                accountHoldCommandRepository,
                accountHoldPort
        );
    }

    @Test
    void shouldThrowWhenAmountIsNegative() {

        CreateAccountHoldCommand command =
                command(
                        "-100",
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
                accountHoldCommandRepository,
                accountHoldPort
        );
    }

    private void mockSaveHold() {

        doAnswer(invocation -> {

            AccountHold hold =
                    invocation.getArgument(0);

            hold.setId(holdId);

            return hold;

        }).when(accountHoldCommandRepository)
                .save(any(AccountHold.class));
    }

    private CreateAccountHoldCommand command(
            String amount,
            Currency currency
    ) {
        return new CreateAccountHoldCommand(
                accountId,
                new BigDecimal(amount),
                currency,
                AccountHoldType.AVAILABLE_BALANCE_RESERVATION,
                AccountHoldReferenceType.WITHDRAWAL_INTENT,
                referenceId
        );
    }

    private void assertInvalidRequest(
            CreateAccountHoldCommand command
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
                accountHoldCommandRepository,
                accountHoldPort
        );
    }
}