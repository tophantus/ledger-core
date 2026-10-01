
package com.example.ledgercore.ledger.command.handler;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.ledger.command.dto.RecordCreditFeeCommand;
import com.example.ledgercore.ledger.command.port.outbound.CreditFacilityLedgerMappingPort;
import com.example.ledgercore.ledger.command.repository.JournalEntryCommandRepository;
import com.example.ledgercore.ledger.command.repository.JournalEntryLineCommandRepository;
import com.example.ledgercore.ledger.entity.JournalEntry;
import com.example.ledgercore.ledger.entity.JournalEntryLine;
import com.example.ledgercore.ledger.entity.LedgerAccount;
import com.example.ledgercore.ledger.enums.EntryType;
import com.example.ledgercore.ledger.enums.JournalSourceType;
import com.example.ledgercore.ledger.service.SystemLedgerAccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecordCreditFeeHandlerTest {

    @Mock
    private JournalEntryCommandRepository journalEntryCommandRepository;

    @Mock
    private JournalEntryLineCommandRepository journalEntryLineCommandRepository;

    @Mock
    private CreditFacilityLedgerMappingPort creditFacilityLedgerMappingPort;

    @Mock
    private SystemLedgerAccountService systemLedgerAccountService;

    @Mock
    private LedgerAccount feeIncomeAccount;

    @InjectMocks
    private RecordCreditFeeHandler handler;

    private UUID transactionId;
    private UUID creditFacilityId;
    private UUID creditLedgerAccountId;
    private UUID feeIncomeLedgerAccountId;
    private UUID journalEntryId;

    private BigDecimal amount;
    private Currency currency;
    private LocalDate businessDate;

    private RecordCreditFeeCommand command;

    @BeforeEach
    void setUp() {
        transactionId = UUID.randomUUID();
        creditFacilityId = UUID.randomUUID();
        creditLedgerAccountId = UUID.randomUUID();
        feeIncomeLedgerAccountId = UUID.randomUUID();
        journalEntryId = UUID.randomUUID();

        amount = new BigDecimal("15000.00");
        currency = Currency.VND;
        businessDate = LocalDate.of(2026, 9, 30);

        command = new RecordCreditFeeCommand(
                transactionId,
                creditFacilityId,
                amount,
                currency,
                businessDate
        );
    }

    // -------------------------------------------------------------------------
    // Success cases
    // -------------------------------------------------------------------------

    @Test
    void execute_shouldRecordFeeJournalWithDebitAndCreditLines_whenCommandIsValid() {
        givenCreditLedgerAccount();
        givenFeeIncomeAccount();
        givenSavedJournalEntry();

        assertDoesNotThrow(() -> handler.execute(command));

        ArgumentCaptor<JournalEntry> journalCaptor =
                ArgumentCaptor.forClass(JournalEntry.class);

        verify(journalEntryCommandRepository)
                .save(journalCaptor.capture());

        JournalEntry journalEntry = journalCaptor.getValue();

        assertEquals(
                JournalSourceType.FEE,
                journalEntry.getSourceType()
        );
        assertEquals(
                transactionId,
                journalEntry.getSourceId()
        );
        assertEquals(
                businessDate,
                journalEntry.getBusinessDate()
        );

        ArgumentCaptor<JournalEntryLine> lineCaptor =
                ArgumentCaptor.forClass(JournalEntryLine.class);

        verify(journalEntryLineCommandRepository, times(2))
                .save(lineCaptor.capture());

        List<JournalEntryLine> lines = lineCaptor.getAllValues();

        assertEquals(2, lines.size());

        JournalEntryLine debitLine = lines.get(0);
        JournalEntryLine creditLine = lines.get(1);

        assertEquals(
                journalEntryId,
                debitLine.getJournalEntryId()
        );
        assertEquals(
                creditLedgerAccountId,
                debitLine.getLedgerAccountId()
        );
        assertEquals(
                EntryType.DEBIT,
                debitLine.getEntryType()
        );
        assertBigDecimalEquals(
                amount,
                debitLine.getAmount()
        );
        assertEquals(
                currency,
                debitLine.getCurrency()
        );

        assertEquals(
                journalEntryId,
                creditLine.getJournalEntryId()
        );
        assertEquals(
                feeIncomeLedgerAccountId,
                creditLine.getLedgerAccountId()
        );
        assertEquals(
                EntryType.CREDIT,
                creditLine.getEntryType()
        );
        assertBigDecimalEquals(
                amount,
                creditLine.getAmount()
        );
        assertEquals(
                currency,
                creditLine.getCurrency()
        );

        assertBigDecimalEquals(
                debitLine.getAmount(),
                creditLine.getAmount()
        );

        verify(creditFacilityLedgerMappingPort)
                .getLedgerAccountId(creditFacilityId);
        verify(systemLedgerAccountService)
                .getFeeIncomeAccount(currency);
    }

    @Test
    void execute_shouldUseCommandCurrency_whenResolvingFeeIncomeAccount() {
        Currency commandCurrency = Currency.USD;

        RecordCreditFeeCommand usdCommand =
                new RecordCreditFeeCommand(
                        transactionId,
                        creditFacilityId,
                        amount,
                        commandCurrency,
                        businessDate
                );

        when(creditFacilityLedgerMappingPort
                .getLedgerAccountId(creditFacilityId))
                .thenReturn(creditLedgerAccountId);
        when(systemLedgerAccountService
                .getFeeIncomeAccount(commandCurrency))
                .thenReturn(feeIncomeAccount);
        when(feeIncomeAccount.getId())
                .thenReturn(feeIncomeLedgerAccountId);
        givenSavedJournalEntry();

        assertDoesNotThrow(() -> handler.execute(usdCommand));

        verify(systemLedgerAccountService)
                .getFeeIncomeAccount(commandCurrency);

        ArgumentCaptor<JournalEntryLine> lineCaptor =
                ArgumentCaptor.forClass(JournalEntryLine.class);

        verify(journalEntryLineCommandRepository, times(2))
                .save(lineCaptor.capture());

        assertEquals(
                commandCurrency,
                lineCaptor.getAllValues().get(0).getCurrency()
        );
        assertEquals(
                commandCurrency,
                lineCaptor.getAllValues().get(1).getCurrency()
        );
    }

    // -------------------------------------------------------------------------
    // Command validation
    // -------------------------------------------------------------------------

    @Test
    void execute_shouldThrowInvalidRequest_whenCommandIsNull() {
        assertInvalidRequest(null);
    }

    @Test
    void execute_shouldThrowInvalidRequest_whenTransactionIdIsNull() {
        assertInvalidRequest(
                new RecordCreditFeeCommand(
                        null,
                        creditFacilityId,
                        amount,
                        currency,
                        businessDate
                )
        );
    }

    @Test
    void execute_shouldThrowInvalidRequest_whenCreditFacilityIdIsNull() {
        assertInvalidRequest(
                new RecordCreditFeeCommand(
                        transactionId,
                        null,
                        amount,
                        currency,
                        businessDate
                )
        );
    }

    @Test
    void execute_shouldThrowInvalidRequest_whenCurrencyIsNull() {
        assertInvalidRequest(
                new RecordCreditFeeCommand(
                        transactionId,
                        creditFacilityId,
                        amount,
                        null,
                        businessDate
                )
        );
    }

    @Test
    void execute_shouldThrowInvalidRequest_whenBusinessDateIsNull() {
        assertInvalidRequest(
                new RecordCreditFeeCommand(
                        transactionId,
                        creditFacilityId,
                        amount,
                        currency,
                        null
                )
        );
    }

    private void assertInvalidRequest(RecordCreditFeeCommand invalidCommand) {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(invalidCommand)
        );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                creditFacilityLedgerMappingPort,
                systemLedgerAccountService,
                journalEntryCommandRepository,
                journalEntryLineCommandRepository
        );
    }

    // -------------------------------------------------------------------------
    // Amount validation
    // -------------------------------------------------------------------------

    @Test
    void execute_shouldThrowInvalidTransferAmount_whenAmountIsNull() {
        assertInvalidAmount(
                new RecordCreditFeeCommand(
                        transactionId,
                        creditFacilityId,
                        null,
                        currency,
                        businessDate
                )
        );
    }

    @Test
    void execute_shouldThrowInvalidTransferAmount_whenAmountIsZero() {
        assertInvalidAmount(
                new RecordCreditFeeCommand(
                        transactionId,
                        creditFacilityId,
                        BigDecimal.ZERO,
                        currency,
                        businessDate
                )
        );
    }

    @Test
    void execute_shouldThrowInvalidTransferAmount_whenAmountIsNegative() {
        assertInvalidAmount(
                new RecordCreditFeeCommand(
                        transactionId,
                        creditFacilityId,
                        new BigDecimal("-1.00"),
                        currency,
                        businessDate
                )
        );
    }

    private void assertInvalidAmount(RecordCreditFeeCommand invalidCommand) {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(invalidCommand)
        );

        assertEquals(
                ErrorCode.INVALID_TRANSFER_AMOUNT,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                creditFacilityLedgerMappingPort,
                systemLedgerAccountService,
                journalEntryCommandRepository,
                journalEntryLineCommandRepository
        );
    }

    // -------------------------------------------------------------------------
    // Dependency exception cases
    // -------------------------------------------------------------------------

    @Test
    void execute_shouldPropagateException_whenCreditFacilityMappingFails() {
        RuntimeException expected =
                new RuntimeException("Credit facility mapping failed");

        when(creditFacilityLedgerMappingPort
                .getLedgerAccountId(creditFacilityId))
                .thenThrow(expected);

        RuntimeException actual = assertThrows(
                RuntimeException.class,
                () -> handler.execute(command)
        );

        assertSame(expected, actual);

        verify(creditFacilityLedgerMappingPort)
                .getLedgerAccountId(creditFacilityId);

        verifyNoInteractions(
                systemLedgerAccountService,
                journalEntryCommandRepository,
                journalEntryLineCommandRepository
        );
    }

    @Test
    void execute_shouldPropagateException_whenFeeIncomeAccountLookupFails() {
        when(creditFacilityLedgerMappingPort
                .getLedgerAccountId(creditFacilityId))
                .thenReturn(creditLedgerAccountId);

        RuntimeException expected =
                new RuntimeException("Fee income account lookup failed");

        when(systemLedgerAccountService.getFeeIncomeAccount(currency))
                .thenThrow(expected);

        RuntimeException actual = assertThrows(
                RuntimeException.class,
                () -> handler.execute(command)
        );

        assertSame(expected, actual);

        verify(creditFacilityLedgerMappingPort)
                .getLedgerAccountId(creditFacilityId);
        verify(systemLedgerAccountService)
                .getFeeIncomeAccount(currency);

        verifyNoInteractions(
                journalEntryCommandRepository,
                journalEntryLineCommandRepository
        );
    }

    @Test
    void execute_shouldPropagateException_whenSavingJournalEntryFails() {
        when(creditFacilityLedgerMappingPort
                .getLedgerAccountId(creditFacilityId))
                .thenReturn(creditLedgerAccountId);
        when(systemLedgerAccountService.getFeeIncomeAccount(currency))
                .thenReturn(feeIncomeAccount);
        when(feeIncomeAccount.getId())
                .thenReturn(feeIncomeLedgerAccountId);

        RuntimeException expected =
                new RuntimeException("Journal entry save failed");

        when(journalEntryCommandRepository.save(any(JournalEntry.class)))
                .thenThrow(expected);

        RuntimeException actual = assertThrows(
                RuntimeException.class,
                () -> handler.execute(command)
        );

        assertSame(expected, actual);

        verify(journalEntryCommandRepository)
                .save(any(JournalEntry.class));
        verifyNoInteractions(journalEntryLineCommandRepository);
    }

    @Test
    void execute_shouldPropagateException_whenSavingDebitLineFails() {
        givenCreditLedgerAccount();
        givenFeeIncomeAccount();
        givenSavedJournalEntry();

        RuntimeException expected =
                new RuntimeException("Debit line save failed");

        when(journalEntryLineCommandRepository.save(
                any(JournalEntryLine.class)
        )).thenThrow(expected);

        RuntimeException actual = assertThrows(
                RuntimeException.class,
                () -> handler.execute(command)
        );

        assertSame(expected, actual);

        verify(journalEntryCommandRepository)
                .save(any(JournalEntry.class));
        verify(journalEntryLineCommandRepository)
                .save(any(JournalEntryLine.class));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void givenCreditLedgerAccount() {
        when(creditFacilityLedgerMappingPort
                .getLedgerAccountId(creditFacilityId))
                .thenReturn(creditLedgerAccountId);
    }

    private void givenFeeIncomeAccount() {
        when(systemLedgerAccountService.getFeeIncomeAccount(currency))
                .thenReturn(feeIncomeAccount);
        when(feeIncomeAccount.getId())
                .thenReturn(feeIncomeLedgerAccountId);
    }

    private void givenSavedJournalEntry() {
        JournalEntry savedJournalEntry = mock(JournalEntry.class);

        when(journalEntryCommandRepository.save(any(JournalEntry.class)))
                .thenReturn(savedJournalEntry);
        when(savedJournalEntry.getId())
                .thenReturn(journalEntryId);
    }

    private void assertBigDecimalEquals(
            BigDecimal expected,
            BigDecimal actual
    ) {
        assertNotNull(actual);
        assertEquals(
                0,
                expected.compareTo(actual),
                () -> "Expected " + expected + " but was " + actual
        );
    }
}