package com.example.ledgercore.ledger.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
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
import java.time.LocalDate;
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

    private static final LocalDate BUSINESS_DATE =
            LocalDate.of(2026, 9, 4);

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
        // Given
        RecordTransferCommand command =
                new RecordTransferCommand(
                        transactionId,
                        sourceAccountId,
                        destinationAccountId,
                        AMOUNT,
                        CURRENCY,
                        BUSINESS_DATE
                );

        JournalEntry savedJournalEntry =
                JournalEntry.builder()
                        .id(journalEntryId)
                        .transactionId(transactionId)
                        .businessDate(BUSINESS_DATE)
                        .build();

        when(accountLedgerMappingPort.getLedgerAccountId(
                sourceAccountId
        )).thenReturn(sourceLedgerAccountId);

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

        // Debit: Source account
        JournalEntryLine debitLine =
                lines.getFirst();

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

        // Credit: Destination account
        JournalEntryLine creditLine =
                lines.get(1);

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
                accountLedgerMappingPort
        );
    }

    @Test
    void shouldRejectNullTransactionId() {
        // Given
        RecordTransferCommand command =
                new RecordTransferCommand(
                        null,
                        sourceAccountId,
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
                accountLedgerMappingPort
        );
    }

    @Test
    void shouldRejectNullSourceAccountId() {
        // Given
        RecordTransferCommand command =
                new RecordTransferCommand(
                        transactionId,
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
                accountLedgerMappingPort
        );
    }

    @Test
    void shouldRejectNullDestinationAccountId() {
        // Given
        RecordTransferCommand command =
                new RecordTransferCommand(
                        transactionId,
                        sourceAccountId,
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
                accountLedgerMappingPort
        );
    }

    @Test
    void shouldRejectNullCurrency() {
        // Given
        RecordTransferCommand command =
                new RecordTransferCommand(
                        transactionId,
                        sourceAccountId,
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
                accountLedgerMappingPort
        );
    }

    @Test
    void shouldRejectBlankCurrency() {
        // Given
        RecordTransferCommand command =
                new RecordTransferCommand(
                        transactionId,
                        sourceAccountId,
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
                accountLedgerMappingPort
        );
    }

    @Test
    void shouldRejectNullBusinessDate() {
        // Given
        RecordTransferCommand command =
                new RecordTransferCommand(
                        transactionId,
                        sourceAccountId,
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
                accountLedgerMappingPort
        );
    }

    @Test
    void shouldRejectNullAmount() {
        // Given
        RecordTransferCommand command =
                new RecordTransferCommand(
                        transactionId,
                        sourceAccountId,
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
                accountLedgerMappingPort
        );
    }

    @Test
    void shouldRejectZeroAmount() {
        // Given
        RecordTransferCommand command =
                new RecordTransferCommand(
                        transactionId,
                        sourceAccountId,
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
                accountLedgerMappingPort
        );
    }

    @Test
    void shouldRejectNegativeAmount() {
        // Given
        RecordTransferCommand command =
                new RecordTransferCommand(
                        transactionId,
                        sourceAccountId,
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
                accountLedgerMappingPort
        );
    }

    @Test
    void shouldRejectSameAccountTransfer() {
        // Given
        UUID accountId = UUID.randomUUID();

        RecordTransferCommand command =
                new RecordTransferCommand(
                        transactionId,
                        accountId,
                        accountId,
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
                ErrorCode.SAME_ACCOUNT_TRANSFER,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                journalEntryCommandRepository,
                journalEntryLineCommandRepository,
                accountLedgerMappingPort
        );
    }
}