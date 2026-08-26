package com.example.ledgercore.ledger.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.ledger.command.dto.RecordWithdrawCommand;
import com.example.ledgercore.ledger.command.port.outbound.AccountLedgerMappingPort;
import com.example.ledgercore.ledger.command.repository.LedgerEntryCommandRepository;
import com.example.ledgercore.ledger.entity.LedgerAccount;
import com.example.ledgercore.ledger.entity.LedgerEntry;
import com.example.ledgercore.ledger.enums.EntryType;
import com.example.ledgercore.ledger.service.SystemLedgerAccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecordWithdrawHandlerTest {

    @Mock
    private LedgerEntryCommandRepository ledgerEntryCommandRepository;

    @Mock
    private AccountLedgerMappingPort accountLedgerMappingPort;

    @Mock
    private SystemLedgerAccountService systemLedgerAccountService;

    private RecordWithdrawHandler handler;

    private UUID transactionId;
    private UUID sourceAccountId;

    private UUID customerLedgerAccountId;
    private UUID systemCashLedgerAccountId;

    private BigDecimal amount;
    private String currency;

    @BeforeEach
    void setUp() {
        handler = new RecordWithdrawHandler(
                ledgerEntryCommandRepository,
                accountLedgerMappingPort,
                systemLedgerAccountService
        );

        transactionId = UUID.randomUUID();
        sourceAccountId = UUID.randomUUID();

        customerLedgerAccountId = UUID.randomUUID();
        systemCashLedgerAccountId = UUID.randomUUID();

        amount = new BigDecimal("75000");
        currency = "VND";
    }

    @Test
    void shouldRecordWithdraw() {
        LedgerAccount cashAccount =
                mock(LedgerAccount.class);

        when(cashAccount.getId())
                .thenReturn(systemCashLedgerAccountId);

        when(accountLedgerMappingPort.getLedgerAccountId(
                sourceAccountId
        )).thenReturn(customerLedgerAccountId);

        when(systemLedgerAccountService.getCashAccount(currency))
                .thenReturn(cashAccount);

        handler.execute(
                new RecordWithdrawCommand(
                        transactionId,
                        sourceAccountId,
                        amount,
                        currency
                )
        );

        verify(accountLedgerMappingPort)
                .getLedgerAccountId(sourceAccountId);

        verify(systemLedgerAccountService)
                .getCashAccount(currency);

        verify(ledgerEntryCommandRepository, times(2))
                .save(any(LedgerEntry.class));
    }

    @Test
    void shouldCreateDebitAndCreditEntries() {
        LedgerAccount cashAccount =
                mock(LedgerAccount.class);

        when(cashAccount.getId())
                .thenReturn(systemCashLedgerAccountId);

        when(accountLedgerMappingPort.getLedgerAccountId(
                sourceAccountId
        )).thenReturn(customerLedgerAccountId);

        when(systemLedgerAccountService.getCashAccount(currency))
                .thenReturn(cashAccount);

        handler.execute(
                new RecordWithdrawCommand(
                        transactionId,
                        sourceAccountId,
                        amount,
                        currency
                )
        );

        ArgumentCaptor<LedgerEntry> captor =
                ArgumentCaptor.forClass(LedgerEntry.class);

        verify(ledgerEntryCommandRepository, times(2))
                .save(captor.capture());

        var entries = captor.getAllValues();

        LedgerEntry debit = entries.get(0);
        LedgerEntry credit = entries.get(1);

        assertEquals(transactionId, debit.getTransactionId());
        assertEquals(
                customerLedgerAccountId,
                debit.getLedgerAccountId()
        );
        assertEquals(EntryType.DEBIT, debit.getEntryType());
        assertEquals(amount, debit.getAmount());
        assertEquals(currency, debit.getCurrency());

        assertEquals(transactionId, credit.getTransactionId());
        assertEquals(
                systemCashLedgerAccountId,
                credit.getLedgerAccountId()
        );
        assertEquals(EntryType.CREDIT, credit.getEntryType());
        assertEquals(amount, credit.getAmount());
        assertEquals(currency, credit.getCurrency());
    }

    @Test
    void shouldThrowWhenTransactionIdIsNull() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                new RecordWithdrawCommand(
                                        null,
                                        sourceAccountId,
                                        amount,
                                        currency
                                )
                        )
                );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                accountLedgerMappingPort,
                systemLedgerAccountService,
                ledgerEntryCommandRepository
        );
    }

    @Test
    void shouldThrowWhenSourceAccountIdIsNull() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                new RecordWithdrawCommand(
                                        transactionId,
                                        null,
                                        amount,
                                        currency
                                )
                        )
                );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );
    }

    @Test
    void shouldThrowWhenAmountIsNull() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                new RecordWithdrawCommand(
                                        transactionId,
                                        sourceAccountId,
                                        null,
                                        currency
                                )
                        )
                );

        assertEquals(
                ErrorCode.INVALID_WITHDRAW_AMOUNT,
                exception.getErrorCode()
        );
    }

    @Test
    void shouldThrowWhenAmountIsZero() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                new RecordWithdrawCommand(
                                        transactionId,
                                        sourceAccountId,
                                        BigDecimal.ZERO,
                                        currency
                                )
                        )
                );

        assertEquals(
                ErrorCode.INVALID_WITHDRAW_AMOUNT,
                exception.getErrorCode()
        );
    }

    @Test
    void shouldThrowWhenAmountIsNegative() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                new RecordWithdrawCommand(
                                        transactionId,
                                        sourceAccountId,
                                        new BigDecimal("-1"),
                                        currency
                                )
                        )
                );

        assertEquals(
                ErrorCode.INVALID_WITHDRAW_AMOUNT,
                exception.getErrorCode()
        );
    }

    @Test
    void shouldThrowWhenCurrencyIsNull() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                new RecordWithdrawCommand(
                                        transactionId,
                                        sourceAccountId,
                                        amount,
                                        null
                                )
                        )
                );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );
    }

    @Test
    void shouldThrowWhenCurrencyIsBlank() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                new RecordWithdrawCommand(
                                        transactionId,
                                        sourceAccountId,
                                        amount,
                                        " "
                                )
                        )
                );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );
    }
}