package com.example.ledgercore.ledger.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.ledger.command.dto.RecordDepositCommand;
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
class RecordDepositHandlerTest {

    @Mock
    private LedgerEntryCommandRepository ledgerEntryCommandRepository;

    @Mock
    private AccountLedgerMappingPort accountLedgerMappingPort;

    @Mock
    private SystemLedgerAccountService systemLedgerAccountService;

    private RecordDepositHandler handler;

    private UUID transactionId;
    private UUID destinationAccountId;
    private UUID systemLedgerAccountId;

    private BigDecimal amount;
    private String currency;

    @BeforeEach
    void setUp() {
        handler = new RecordDepositHandler(
                ledgerEntryCommandRepository,
                accountLedgerMappingPort,
                systemLedgerAccountService
        );

        transactionId = UUID.randomUUID();
        destinationAccountId = UUID.randomUUID();
        systemLedgerAccountId = UUID.randomUUID();

        amount = new BigDecimal("100000");
        currency = "VND";
    }

    @Test
    void shouldRecordDeposit() {
        UUID customerLedgerAccountId = UUID.randomUUID();

        LedgerAccount cashAccount = LedgerAccount.builder()
                .build();

        // Entity ID thường được generate bởi Hibernate.
        // Nếu entity có setter ID thì set ID tương ứng.
        // Ở đây mock service không cần assert entity bên trong.

        when(systemLedgerAccountService.getCashAccount(currency))
                .thenReturn(cashAccount);

        when(accountLedgerMappingPort.getLedgerAccountId(
                destinationAccountId
        )).thenReturn(customerLedgerAccountId);

        // Nếu getId() của cashAccount chưa được set,
        // test này phụ thuộc cách entity LedgerAccount của project generate ID.
        // Tốt nhất entity nên có ID setter/package-private constructor.

        handler.execute(
                new RecordDepositCommand(
                        transactionId,
                        destinationAccountId,
                        amount,
                        currency
                )
        );

        verify(systemLedgerAccountService)
                .getCashAccount(currency);

        verify(accountLedgerMappingPort)
                .getLedgerAccountId(destinationAccountId);

        verify(ledgerEntryCommandRepository, times(2))
                .save(any(LedgerEntry.class));
    }

    @Test
    void shouldCreateDebitAndCreditEntries() {
        UUID customerLedgerAccountId = UUID.randomUUID();
        UUID cashLedgerAccountId = UUID.randomUUID();

        LedgerAccount cashAccount = mock(LedgerAccount.class);

        when(cashAccount.getId())
                .thenReturn(cashLedgerAccountId);

        when(systemLedgerAccountService.getCashAccount(currency))
                .thenReturn(cashAccount);

        when(accountLedgerMappingPort.getLedgerAccountId(
                destinationAccountId
        )).thenReturn(customerLedgerAccountId);

        handler.execute(
                new RecordDepositCommand(
                        transactionId,
                        destinationAccountId,
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
        assertEquals(cashLedgerAccountId, debit.getLedgerAccountId());
        assertEquals(EntryType.DEBIT, debit.getEntryType());
        assertEquals(amount, debit.getAmount());
        assertEquals(currency, debit.getCurrency());

        assertEquals(transactionId, credit.getTransactionId());
        assertEquals(customerLedgerAccountId, credit.getLedgerAccountId());
        assertEquals(EntryType.CREDIT, credit.getEntryType());
        assertEquals(amount, credit.getAmount());
        assertEquals(currency, credit.getCurrency());
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
                ledgerEntryCommandRepository,
                accountLedgerMappingPort,
                systemLedgerAccountService
        );
    }

    @Test
    void shouldThrowWhenTransactionIdIsNull() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                new RecordDepositCommand(
                                        null,
                                        destinationAccountId,
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
    void shouldThrowWhenDestinationAccountIdIsNull() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                new RecordDepositCommand(
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
    void shouldThrowWhenCurrencyIsNull() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                new RecordDepositCommand(
                                        transactionId,
                                        destinationAccountId,
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
                                new RecordDepositCommand(
                                        transactionId,
                                        destinationAccountId,
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

    @Test
    void shouldThrowWhenAmountIsNull() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                new RecordDepositCommand(
                                        transactionId,
                                        destinationAccountId,
                                        null,
                                        currency
                                )
                        )
                );

        assertEquals(
                ErrorCode.INVALID_TRANSFER_AMOUNT,
                exception.getErrorCode()
        );
    }

    @Test
    void shouldThrowWhenAmountIsZero() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                new RecordDepositCommand(
                                        transactionId,
                                        destinationAccountId,
                                        BigDecimal.ZERO,
                                        currency
                                )
                        )
                );

        assertEquals(
                ErrorCode.INVALID_TRANSFER_AMOUNT,
                exception.getErrorCode()
        );
    }

    @Test
    void shouldThrowWhenAmountIsNegative() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                new RecordDepositCommand(
                                        transactionId,
                                        destinationAccountId,
                                        new BigDecimal("-1"),
                                        currency
                                )
                        )
                );

        assertEquals(
                ErrorCode.INVALID_TRANSFER_AMOUNT,
                exception.getErrorCode()
        );
    }
}