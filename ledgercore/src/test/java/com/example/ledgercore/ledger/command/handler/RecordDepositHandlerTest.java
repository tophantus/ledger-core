package com.example.ledgercore.ledger.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.ledger.command.dto.RecordDepositCommand;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecordDepositHandlerTest {

    @Mock
    private JournalEntryCommandRepository journalEntryCommandRepository;

    @Mock
    private JournalEntryLineCommandRepository journalEntryLineCommandRepository;

    @Mock
    private AccountLedgerMappingPort accountLedgerMappingPort;

    @Mock
    private SystemLedgerAccountService systemLedgerAccountService;

    private RecordDepositHandler handler;

    private UUID transactionId;
    private UUID destinationAccountId;
    private UUID cashLedgerAccountId;
    private UUID destinationLedgerAccountId;

    private static final String CURRENCY = "VND";
    private static final BigDecimal AMOUNT =
            new BigDecimal("1000000.0000");

    @BeforeEach
    void setUp() {
        handler = new RecordDepositHandler(
                journalEntryCommandRepository,
                journalEntryLineCommandRepository,
                accountLedgerMappingPort,
                systemLedgerAccountService
        );

        transactionId = UUID.randomUUID();
        destinationAccountId = UUID.randomUUID();
        cashLedgerAccountId = UUID.randomUUID();
        destinationLedgerAccountId = UUID.randomUUID();
    }

    @Test
    void shouldRecordDepositSuccessfully() {
        RecordDepositCommand command =
                new RecordDepositCommand(
                        transactionId,
                        destinationAccountId,
                        AMOUNT,
                        CURRENCY
                );

        LedgerAccount cashAccount =
                LedgerAccount.builder()
                        .id(cashLedgerAccountId)
                        .build();

        JournalEntry savedJournalEntry =
                JournalEntry.builder()
                        .id(UUID.randomUUID())
                        .transactionId(transactionId)
                        .build();

        when(systemLedgerAccountService.getCashAccount(CURRENCY))
                .thenReturn(cashAccount);

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

        assertEquals(transactionId, journalEntry.getTransactionId());

        ArgumentCaptor<JournalEntryLine> lineCaptor =
                ArgumentCaptor.forClass(JournalEntryLine.class);

        verify(journalEntryLineCommandRepository, times(2))
                .save(lineCaptor.capture());

        var lines = lineCaptor.getAllValues();

        assertEquals(2, lines.size());

        JournalEntryLine debitLine = lines.getFirst();
        assertEquals(
                savedJournalEntry.getId(),
                debitLine.getJournalEntryId()
        );
        assertEquals(
                cashLedgerAccountId,
                debitLine.getLedgerAccountId()
        );
        assertEquals(EntryType.DEBIT, debitLine.getEntryType());
        assertEquals(AMOUNT, debitLine.getAmount());
        assertEquals(CURRENCY, debitLine.getCurrency());

        JournalEntryLine creditLine = lines.get(1);
        assertEquals(
                savedJournalEntry.getId(),
                creditLine.getJournalEntryId()
        );
        assertEquals(
                destinationLedgerAccountId,
                creditLine.getLedgerAccountId()
        );
        assertEquals(EntryType.CREDIT, creditLine.getEntryType());
        assertEquals(AMOUNT, creditLine.getAmount());
        assertEquals(CURRENCY, creditLine.getCurrency());

        verify(systemLedgerAccountService)
                .getCashAccount(CURRENCY);

        verify(accountLedgerMappingPort)
                .getLedgerAccountId(destinationAccountId);

        verifyNoMoreInteractions(
                systemLedgerAccountService,
                accountLedgerMappingPort
        );
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
        RecordDepositCommand command =
                new RecordDepositCommand(
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
                accountLedgerMappingPort,
                systemLedgerAccountService
        );
    }

    @Test
    void shouldRejectNullDestinationAccountId() {
        RecordDepositCommand command =
                new RecordDepositCommand(
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
        RecordDepositCommand command =
                new RecordDepositCommand(
                        transactionId,
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
                accountLedgerMappingPort,
                systemLedgerAccountService
        );
    }

    @Test
    void shouldRejectBlankCurrency() {
        RecordDepositCommand command =
                new RecordDepositCommand(
                        transactionId,
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
                accountLedgerMappingPort,
                systemLedgerAccountService
        );
    }

    @Test
    void shouldRejectNullAmount() {
        RecordDepositCommand command =
                new RecordDepositCommand(
                        transactionId,
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
                accountLedgerMappingPort,
                systemLedgerAccountService
        );
    }

    @Test
    void shouldRejectZeroAmount() {
        RecordDepositCommand command =
                new RecordDepositCommand(
                        transactionId,
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
                accountLedgerMappingPort,
                systemLedgerAccountService
        );
    }

    @Test
    void shouldRejectNegativeAmount() {
        RecordDepositCommand command =
                new RecordDepositCommand(
                        transactionId,
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
                accountLedgerMappingPort,
                systemLedgerAccountService
        );
    }
}