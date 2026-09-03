package com.example.ledgercore.ledger.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
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
import java.time.LocalDate;
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

    private static final LocalDate BUSINESS_DATE =
            LocalDate.of(2026, 9, 4);

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
        // Given
        RecordWithdrawCommand command =
                new RecordWithdrawCommand(
                        transactionId,
                        sourceAccountId,
                        AMOUNT,
                        CURRENCY,
                        BUSINESS_DATE
                );

        LedgerAccount cashAccount =
                LedgerAccount.builder()
                        .id(cashLedgerAccountId)
                        .build();

        JournalEntry savedJournalEntry =
                JournalEntry.builder()
                        .id(journalEntryId)
                        .transactionId(transactionId)
                        .businessDate(BUSINESS_DATE)
                        .build();

        when(accountLedgerMappingPort.getLedgerAccountId(
                sourceAccountId
        )).thenReturn(customerLedgerAccountId);

        when(systemLedgerAccountService.getCashAccount(
                CURRENCY
        )).thenReturn(cashAccount);

        when(journalEntryCommandRepository.save(any(JournalEntry.class)))
                .thenReturn(savedJournalEntry);

        // When
        handler.execute(command);

        // Then
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

        assertEquals(
                BUSINESS_DATE,
                journalEntry.getBusinessDate()
        );

        ArgumentCaptor<JournalEntryLine> lineCaptor =
                ArgumentCaptor.forClass(JournalEntryLine.class);

        verify(journalEntryLineCommandRepository, times(2))
                .save(lineCaptor.capture());

        List<JournalEntryLine> lines =
                lineCaptor.getAllValues();

        assertEquals(2, lines.size());

        // Debit: Customer account
        JournalEntryLine debitLine =
                lines.getFirst();

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

        // Credit: System cash account
        JournalEntryLine creditLine =
                lines.get(1);

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
        // When
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(null)
        );

        // Then
        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
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
        // Given
        RecordWithdrawCommand command =
                new RecordWithdrawCommand(
                        null,
                        sourceAccountId,
                        AMOUNT,
                        CURRENCY,
                        BUSINESS_DATE
                );

        // When
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        // Then
        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
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
        // Given
        RecordWithdrawCommand command =
                new RecordWithdrawCommand(
                        transactionId,
                        null,
                        AMOUNT,
                        CURRENCY,
                        BUSINESS_DATE
                );

        // When
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        // Then
        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
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
        // Given
        RecordWithdrawCommand command =
                new RecordWithdrawCommand(
                        transactionId,
                        sourceAccountId,
                        AMOUNT,
                        null,
                        BUSINESS_DATE
                );

        // When
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        // Then
        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
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
        // Given
        RecordWithdrawCommand command =
                new RecordWithdrawCommand(
                        transactionId,
                        sourceAccountId,
                        AMOUNT,
                        "   ",
                        BUSINESS_DATE
                );

        // When
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        // Then
        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                journalEntryCommandRepository,
                journalEntryLineCommandRepository,
                accountLedgerMappingPort,
                systemLedgerAccountService
        );
    }

    @Test
    void shouldRejectNullBusinessDate() {
        // Given
        RecordWithdrawCommand command =
                new RecordWithdrawCommand(
                        transactionId,
                        sourceAccountId,
                        AMOUNT,
                        CURRENCY,
                        null
                );

        // When
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        // Then
        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
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
        // Given
        RecordWithdrawCommand command =
                new RecordWithdrawCommand(
                        transactionId,
                        sourceAccountId,
                        null,
                        CURRENCY,
                        BUSINESS_DATE
                );

        // When
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        // Then
        assertEquals(
                ErrorCode.INVALID_WITHDRAW_AMOUNT,
                exception.getErrorCode()
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
        // Given
        RecordWithdrawCommand command =
                new RecordWithdrawCommand(
                        transactionId,
                        sourceAccountId,
                        BigDecimal.ZERO,
                        CURRENCY,
                        BUSINESS_DATE
                );

        // When
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        // Then
        assertEquals(
                ErrorCode.INVALID_WITHDRAW_AMOUNT,
                exception.getErrorCode()
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
        // Given
        RecordWithdrawCommand command =
                new RecordWithdrawCommand(
                        transactionId,
                        sourceAccountId,
                        new BigDecimal("-1"),
                        CURRENCY,
                        BUSINESS_DATE
                );

        // When
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(command)
        );

        // Then
        assertEquals(
                ErrorCode.INVALID_WITHDRAW_AMOUNT,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                journalEntryCommandRepository,
                journalEntryLineCommandRepository,
                accountLedgerMappingPort,
                systemLedgerAccountService
        );
    }
}