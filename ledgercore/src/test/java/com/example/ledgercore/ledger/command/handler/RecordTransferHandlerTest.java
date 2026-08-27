package com.example.ledgercore.ledger.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.ledger.command.dto.RecordTransferCommand;
import com.example.ledgercore.ledger.command.port.outbound.AccountLedgerMappingPort;
import com.example.ledgercore.ledger.command.repository.JournalEntryCommandRepository;
import com.example.ledgercore.ledger.command.repository.JournalEntryLineCommandRepository;
import com.example.ledgercore.ledger.entity.JournalEntry;
import com.example.ledgercore.ledger.entity.JournalEntryLine;
import com.example.ledgercore.ledger.enums.EntryType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecordTransferHandlerTest {

    @Mock
    private JournalEntryCommandRepository journalEntryCommandRepository;

    @Mock
    private JournalEntryLineCommandRepository journalEntryLineCommandRepository;

    @Mock
    private AccountLedgerMappingPort accountLedgerMappingPort;

    private RecordTransferHandler handler;

    private UUID transactionId;
    private UUID sourceAccountId;
    private UUID destinationAccountId;

    private UUID sourceLedgerAccountId;
    private UUID destinationLedgerAccountId;
    private UUID journalEntryId;

    private static final String CURRENCY = "VND";

    private static final BigDecimal AMOUNT =
            new BigDecimal("1000000.0000");

    @BeforeEach
    void setUp() {
        handler = new RecordTransferHandler(
                journalEntryCommandRepository,
                journalEntryLineCommandRepository,
                accountLedgerMappingPort
        );

        transactionId = UUID.randomUUID();
        sourceAccountId = UUID.randomUUID();
        destinationAccountId = UUID.randomUUID();

        sourceLedgerAccountId = UUID.randomUUID();
        destinationLedgerAccountId = UUID.randomUUID();
        journalEntryId = UUID.randomUUID();
    }

    @Test
    void shouldRecordTransferSuccessfully() {
        RecordTransferCommand command =
                new RecordTransferCommand(
                        transactionId,
                        sourceAccountId,
                        destinationAccountId,
                        AMOUNT,
                        CURRENCY
                );

        JournalEntry savedJournalEntry =
                JournalEntry.builder()
                        .id(journalEntryId)
                        .transactionId(transactionId)
                        .build();

        when(accountLedgerMappingPort.getLedgerAccountId(
                sourceAccountId
        )).thenReturn(sourceLedgerAccountId);

        when(accountLedgerMappingPort.getLedgerAccountId(
                destinationAccountId
        )).thenReturn(destinationLedgerAccountId);

        when(journalEntryCommandRepository.save(any(JournalEntry.class)))
                .thenReturn(savedJournalEntry);

        handler.execute(command);

        ArgumentCaptor<JournalEntry> journalCaptor =
                ArgumentCaptor.forClass(JournalEntry.class);

        verify(journalEntryCommandRepository)
                .save(journalCaptor.capture());

        JournalEntry journalEntry = journalCaptor.getValue();

        assertEquals(
                transactionId,
                journalEntry.getTransactionId()
        );

        ArgumentCaptor<JournalEntryLine> lineCaptor =
                ArgumentCaptor.forClass(JournalEntryLine.class);

        verify(journalEntryLineCommandRepository, times(2))
                .save(lineCaptor.capture());

        List<JournalEntryLine> lines =
                lineCaptor.getAllValues();

        assertEquals(2, lines.size());

        JournalEntryLine debitLine = lines.getFirst();

        assertEquals(
                journalEntryId,
                debitLine.getJournalEntryId()
        );
        assertEquals(
                sourceLedgerAccountId,
                debitLine.getLedgerAccountId()
        );
        assertEquals(
                EntryType.DEBIT,
                debitLine.getEntryType()
        );
        assertEquals(
                AMOUNT,
                debitLine.getAmount()
        );
        assertEquals(
                CURRENCY,
                debitLine.getCurrency()
        );

        JournalEntryLine creditLine = lines.get(1);

        assertEquals(
                journalEntryId,
                creditLine.getJournalEntryId()
        );
        assertEquals(
                destinationLedgerAccountId,
                creditLine.getLedgerAccountId()
        );
        assertEquals(
                EntryType.CREDIT,
                creditLine.getEntryType()
        );
        assertEquals(
                AMOUNT,
                creditLine.getAmount()
        );
        assertEquals(
                CURRENCY,
                creditLine.getCurrency()
        );

        verify(accountLedgerMappingPort)
                .getLedgerAccountId(sourceAccountId);

        verify(accountLedgerMappingPort)
                .getLedgerAccountId(destinationAccountId);
    }

    @Test
    void shouldRejectNullCommand() {
        assertThrows(
                BusinessException.class,
                () -> handler.execute(null)
        );

        verifyNoInteractions(
                journalEntryCommandRepository,
                journalEntryLineCommandRepository,
                accountLedgerMappingPort
        );
    }

    @Test
    void shouldRejectNullTransactionId() {
        RecordTransferCommand command =
                new RecordTransferCommand(
                        null,
                        sourceAccountId,
                        destinationAccountId,
                        AMOUNT,
                        CURRENCY
                );

        assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        verifyNoInteractions(
                journalEntryCommandRepository,
                journalEntryLineCommandRepository,
                accountLedgerMappingPort
        );
    }

    @Test
    void shouldRejectNullSourceAccountId() {
        RecordTransferCommand command =
                new RecordTransferCommand(
                        transactionId,
                        null,
                        destinationAccountId,
                        AMOUNT,
                        CURRENCY
                );

        assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        verifyNoInteractions(
                journalEntryCommandRepository,
                journalEntryLineCommandRepository,
                accountLedgerMappingPort
        );
    }

    @Test
    void shouldRejectNullDestinationAccountId() {
        RecordTransferCommand command =
                new RecordTransferCommand(
                        transactionId,
                        sourceAccountId,
                        null,
                        AMOUNT,
                        CURRENCY
                );

        assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        verifyNoInteractions(
                journalEntryCommandRepository,
                journalEntryLineCommandRepository,
                accountLedgerMappingPort
        );
    }

    @Test
    void shouldRejectNullCurrency() {
        RecordTransferCommand command =
                new RecordTransferCommand(
                        transactionId,
                        sourceAccountId,
                        destinationAccountId,
                        AMOUNT,
                        null
                );

        assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        verifyNoInteractions(
                journalEntryCommandRepository,
                journalEntryLineCommandRepository,
                accountLedgerMappingPort
        );
    }

    @Test
    void shouldRejectBlankCurrency() {
        RecordTransferCommand command =
                new RecordTransferCommand(
                        transactionId,
                        sourceAccountId,
                        destinationAccountId,
                        AMOUNT,
                        "   "
                );

        assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        verifyNoInteractions(
                journalEntryCommandRepository,
                journalEntryLineCommandRepository,
                accountLedgerMappingPort
        );
    }

    @Test
    void shouldRejectNullAmount() {
        RecordTransferCommand command =
                new RecordTransferCommand(
                        transactionId,
                        sourceAccountId,
                        destinationAccountId,
                        null,
                        CURRENCY
                );

        assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        verifyNoInteractions(
                journalEntryCommandRepository,
                journalEntryLineCommandRepository,
                accountLedgerMappingPort
        );
    }

    @Test
    void shouldRejectZeroAmount() {
        RecordTransferCommand command =
                new RecordTransferCommand(
                        transactionId,
                        sourceAccountId,
                        destinationAccountId,
                        BigDecimal.ZERO,
                        CURRENCY
                );

        assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        verifyNoInteractions(
                journalEntryCommandRepository,
                journalEntryLineCommandRepository,
                accountLedgerMappingPort
        );
    }

    @Test
    void shouldRejectNegativeAmount() {
        RecordTransferCommand command =
                new RecordTransferCommand(
                        transactionId,
                        sourceAccountId,
                        destinationAccountId,
                        new BigDecimal("-1"),
                        CURRENCY
                );

        assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        verifyNoInteractions(
                journalEntryCommandRepository,
                journalEntryLineCommandRepository,
                accountLedgerMappingPort
        );
    }

    @Test
    void shouldRejectSameAccountTransfer() {
        UUID accountId = UUID.randomUUID();

        RecordTransferCommand command =
                new RecordTransferCommand(
                        transactionId,
                        accountId,
                        accountId,
                        AMOUNT,
                        CURRENCY
                );

        assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        verifyNoInteractions(
                journalEntryCommandRepository,
                journalEntryLineCommandRepository,
                accountLedgerMappingPort
        );
    }
}