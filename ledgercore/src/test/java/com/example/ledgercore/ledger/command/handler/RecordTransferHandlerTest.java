package com.example.ledgercore.ledger.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.ledger.command.dto.RecordTransferCommand;
import com.example.ledgercore.ledger.command.port.outbound.AccountLedgerMappingPort;
import com.example.ledgercore.ledger.command.repository.LedgerEntryCommandRepository;
import com.example.ledgercore.ledger.entity.LedgerEntry;
import com.example.ledgercore.ledger.enums.EntryType;
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
class RecordTransferHandlerTest {

    @Mock
    private LedgerEntryCommandRepository ledgerEntryRepository;

    @Mock
    private AccountLedgerMappingPort accountLedgerMappingPort;

    private RecordTransferHandler handler;

    private UUID transactionId;
    private UUID sourceAccountId;
    private UUID destinationAccountId;

    private UUID sourceLedgerAccountId;
    private UUID destinationLedgerAccountId;

    private BigDecimal amount;
    private String currency;

    @BeforeEach
    void setUp() {
        handler = new RecordTransferHandler(
                ledgerEntryRepository,
                accountLedgerMappingPort
        );

        transactionId = UUID.randomUUID();
        sourceAccountId = UUID.randomUUID();
        destinationAccountId = UUID.randomUUID();

        sourceLedgerAccountId = UUID.randomUUID();
        destinationLedgerAccountId = UUID.randomUUID();

        amount = new BigDecimal("50000");
        currency = "VND";
    }

    @Test
    void shouldRecordTransfer() {
        when(accountLedgerMappingPort.getLedgerAccountId(
                sourceAccountId
        )).thenReturn(sourceLedgerAccountId);

        when(accountLedgerMappingPort.getLedgerAccountId(
                destinationAccountId
        )).thenReturn(destinationLedgerAccountId);

        handler.execute(
                new RecordTransferCommand(
                        transactionId,
                        sourceAccountId,
                        destinationAccountId,
                        amount,
                        currency
                )
        );

        verify(accountLedgerMappingPort)
                .getLedgerAccountId(sourceAccountId);

        verify(accountLedgerMappingPort)
                .getLedgerAccountId(destinationAccountId);

        verify(ledgerEntryRepository, times(2))
                .save(any(LedgerEntry.class));
    }

    @Test
    void shouldCreateDebitAndCreditEntries() {
        when(accountLedgerMappingPort.getLedgerAccountId(
                sourceAccountId
        )).thenReturn(sourceLedgerAccountId);

        when(accountLedgerMappingPort.getLedgerAccountId(
                destinationAccountId
        )).thenReturn(destinationLedgerAccountId);

        handler.execute(
                new RecordTransferCommand(
                        transactionId,
                        sourceAccountId,
                        destinationAccountId,
                        amount,
                        currency
                )
        );

        ArgumentCaptor<LedgerEntry> captor =
                ArgumentCaptor.forClass(LedgerEntry.class);

        verify(ledgerEntryRepository, times(2))
                .save(captor.capture());

        var entries = captor.getAllValues();

        LedgerEntry debit = entries.get(0);
        LedgerEntry credit = entries.get(1);

        assertEquals(transactionId, debit.getTransactionId());
        assertEquals(sourceLedgerAccountId, debit.getLedgerAccountId());
        assertEquals(EntryType.DEBIT, debit.getEntryType());
        assertEquals(amount, debit.getAmount());
        assertEquals(currency, debit.getCurrency());

        assertEquals(transactionId, credit.getTransactionId());
        assertEquals(
                destinationLedgerAccountId,
                credit.getLedgerAccountId()
        );
        assertEquals(EntryType.CREDIT, credit.getEntryType());
        assertEquals(amount, credit.getAmount());
        assertEquals(currency, credit.getCurrency());
    }

    @Test
    void shouldThrowWhenAmountIsNull() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                new RecordTransferCommand(
                                        transactionId,
                                        sourceAccountId,
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

        verifyNoInteractions(
                accountLedgerMappingPort,
                ledgerEntryRepository
        );
    }

    @Test
    void shouldThrowWhenAmountIsZero() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                new RecordTransferCommand(
                                        transactionId,
                                        sourceAccountId,
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
                                new RecordTransferCommand(
                                        transactionId,
                                        sourceAccountId,
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

    @Test
    void shouldThrowWhenSourceAndDestinationAreSame() {
        BusinessException exception =
                assertThrows(
                        BusinessException.class,
                        () -> handler.execute(
                                new RecordTransferCommand(
                                        transactionId,
                                        sourceAccountId,
                                        sourceAccountId,
                                        amount,
                                        currency
                                )
                        )
                );

        assertEquals(
                ErrorCode.SAME_ACCOUNT_TRANSFER,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                accountLedgerMappingPort,
                ledgerEntryRepository
        );
    }
}