package com.example.ledgercore.ledger.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.ledger.command.dto.RecordWithdrawCommand;
import com.example.ledgercore.ledger.command.port.outbound.AccountLedgerMappingPort;
import com.example.ledgercore.ledger.command.repository.JournalEntryCommandRepository;
import com.example.ledgercore.ledger.command.repository.JournalEntryLineCommandRepository;
import com.example.ledgercore.ledger.entity.JournalEntry;
import com.example.ledgercore.ledger.entity.JournalEntryLine;
import com.example.ledgercore.ledger.entity.LedgerAccount;
import com.example.ledgercore.ledger.enums.EntryType;
import com.example.ledgercore.ledger.service.SystemLedgerAccountService;
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
class RecordWithdrawHandlerTest {

    @Mock
    private JournalEntryCommandRepository journalEntryCommandRepository;

    @Mock
    private JournalEntryLineCommandRepository journalEntryLineCommandRepository;

    @Mock
    private AccountLedgerMappingPort accountLedgerMappingPort;

    @Mock
    private SystemLedgerAccountService systemLedgerAccountService;

    private RecordWithdrawHandler handler;

    private UUID transactionId;
    private UUID sourceAccountId;

    private UUID customerLedgerAccountId;
    private UUID cashLedgerAccountId;
    private UUID journalEntryId;

    private static final String CURRENCY = "VND";

    private static final BigDecimal AMOUNT =
            new BigDecimal("1000000.0000");

    @BeforeEach
    void setUp() {
        handler = new RecordWithdrawHandler(
                journalEntryCommandRepository,
                journalEntryLineCommandRepository,
                accountLedgerMappingPort,
                systemLedgerAccountService
        );

        transactionId = UUID.randomUUID();
        sourceAccountId = UUID.randomUUID();

        customerLedgerAccountId = UUID.randomUUID();
        cashLedgerAccountId = UUID.randomUUID();
        journalEntryId = UUID.randomUUID();
    }

    @Test
    void shouldRecordWithdrawSuccessfully() {
        RecordWithdrawCommand command =
                new RecordWithdrawCommand(
                        transactionId,
                        sourceAccountId,
                        AMOUNT,
                        CURRENCY
                );

        LedgerAccount cashAccount =
                LedgerAccount.builder()
                        .id(cashLedgerAccountId)
                        .build();

        JournalEntry savedJournalEntry =
                JournalEntry.builder()
                        .id(journalEntryId)
                        .transactionId(transactionId)
                        .build();

        when(accountLedgerMappingPort.getLedgerAccountId(
                sourceAccountId
        )).thenReturn(customerLedgerAccountId);

        when(systemLedgerAccountService.getCashAccount(
                CURRENCY
        )).thenReturn(cashAccount);

        when(journalEntryCommandRepository.save(any(JournalEntry.class)))
                .thenReturn(savedJournalEntry);

        handler.execute(command);

        ArgumentCaptor<JournalEntry> journalCaptor =
                ArgumentCaptor.forClass(JournalEntry.class);

        verify(journalEntryCommandRepository)
                .save(journalCaptor.capture());

        JournalEntry journalEntry =
                journalCaptor.getValue();

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
                customerLedgerAccountId,
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
                cashLedgerAccountId,
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

        verify(systemLedgerAccountService)
                .getCashAccount(CURRENCY);
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
                accountLedgerMappingPort,
                systemLedgerAccountService
        );
    }

    @Test
    void shouldRejectNullTransactionId() {
        RecordWithdrawCommand command =
                new RecordWithdrawCommand(
                        null,
                        sourceAccountId,
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
                accountLedgerMappingPort,
                systemLedgerAccountService
        );
    }

    @Test
    void shouldRejectNullSourceAccountId() {
        RecordWithdrawCommand command =
                new RecordWithdrawCommand(
                        transactionId,
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
                accountLedgerMappingPort,
                systemLedgerAccountService
        );
    }

    @Test
    void shouldRejectNullCurrency() {
        RecordWithdrawCommand command =
                new RecordWithdrawCommand(
                        transactionId,
                        sourceAccountId,
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
                accountLedgerMappingPort,
                systemLedgerAccountService
        );
    }

    @Test
    void shouldRejectBlankCurrency() {
        RecordWithdrawCommand command =
                new RecordWithdrawCommand(
                        transactionId,
                        sourceAccountId,
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
                accountLedgerMappingPort,
                systemLedgerAccountService
        );
    }

    @Test
    void shouldRejectNullAmount() {
        RecordWithdrawCommand command =
                new RecordWithdrawCommand(
                        transactionId,
                        sourceAccountId,
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
                accountLedgerMappingPort,
                systemLedgerAccountService
        );
    }

    @Test
    void shouldRejectZeroAmount() {
        RecordWithdrawCommand command =
                new RecordWithdrawCommand(
                        transactionId,
                        sourceAccountId,
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
                accountLedgerMappingPort,
                systemLedgerAccountService
        );
    }

    @Test
    void shouldRejectNegativeAmount() {
        RecordWithdrawCommand command =
                new RecordWithdrawCommand(
                        transactionId,
                        sourceAccountId,
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
                accountLedgerMappingPort,
                systemLedgerAccountService
        );
    }
}