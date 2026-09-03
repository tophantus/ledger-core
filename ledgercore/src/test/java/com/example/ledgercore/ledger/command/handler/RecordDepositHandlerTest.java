package com.example.ledgercore.ledger.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
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
import java.time.LocalDate;
import java.util.List;
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

    private static final LocalDate BUSINESS_DATE =
            LocalDate.of(2026, 9, 4);

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
        // Given
        RecordDepositCommand command =
                new RecordDepositCommand(
                        transactionId,
                        destinationAccountId,
                        AMOUNT,
                        CURRENCY,
                        BUSINESS_DATE
                );

        LedgerAccount cashAccount =
                LedgerAccount.builder()
                        .id(cashLedgerAccountId)
                        .build();

        UUID journalEntryId = UUID.randomUUID();

        JournalEntry savedJournalEntry =
                JournalEntry.builder()
                        .id(journalEntryId)
                        .transactionId(transactionId)
                        .businessDate(BUSINESS_DATE)
                        .build();

        when(systemLedgerAccountService.getCashAccount(CURRENCY))
                .thenReturn(cashAccount);

        when(accountLedgerMappingPort.getLedgerAccountId(
                destinationAccountId
        )).thenReturn(destinationLedgerAccountId);

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

        // Debit: CASH
        JournalEntryLine debitLine = lines.getFirst();

        assertEquals(
                journalEntryId,
                debitLine.getJournalEntryId()
        );

        assertEquals(
                cashLedgerAccountId,
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

        // Credit: Customer
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

        verify(systemLedgerAccountService)
                .getCashAccount(CURRENCY);

        verify(accountLedgerMappingPort)
                .getLedgerAccountId(destinationAccountId);
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
        RecordDepositCommand command =
                new RecordDepositCommand(
                        null,
                        destinationAccountId,
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
    void shouldRejectNullDestinationAccountId() {
        // Given
        RecordDepositCommand command =
                new RecordDepositCommand(
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
        RecordDepositCommand command =
                new RecordDepositCommand(
                        transactionId,
                        destinationAccountId,
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
        RecordDepositCommand command =
                new RecordDepositCommand(
                        transactionId,
                        destinationAccountId,
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
        RecordDepositCommand command =
                new RecordDepositCommand(
                        transactionId,
                        destinationAccountId,
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
        RecordDepositCommand command =
                new RecordDepositCommand(
                        transactionId,
                        destinationAccountId,
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
                ErrorCode.INVALID_TRANSFER_AMOUNT,
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
        RecordDepositCommand command =
                new RecordDepositCommand(
                        transactionId,
                        destinationAccountId,
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
                ErrorCode.INVALID_TRANSFER_AMOUNT,
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
        RecordDepositCommand command =
                new RecordDepositCommand(
                        transactionId,
                        destinationAccountId,
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
                ErrorCode.INVALID_TRANSFER_AMOUNT,
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