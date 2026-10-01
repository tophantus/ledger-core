
package com.example.ledgercore.ledger.command.handler;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.ledger.command.dto.RecordCreditInterestAccrualJournalCommand;
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
class RecordCreditInterestAccrualJournalHandlerTest {

    @Mock
    private JournalEntryCommandRepository journalEntryCommandRepository;

    @Mock
    private JournalEntryLineCommandRepository journalEntryLineCommandRepository;

    @Mock
    private SystemLedgerAccountService systemLedgerAccountService;

    @Mock
    private LedgerAccount interestReceivableAccount;

    @Mock
    private LedgerAccount interestIncomeAccount;

    @InjectMocks
    private RecordCreditInterestAccrualJournalHandler handler;

    private UUID accrualId;
    private UUID journalEntryId;
    private UUID interestReceivableAccountId;
    private UUID interestIncomeAccountId;

    private LocalDate businessDate;
    private Currency currency;
    private BigDecimal amount;

    private RecordCreditInterestAccrualJournalCommand command;

    @BeforeEach
    void setUp() {
        accrualId = UUID.randomUUID();
        journalEntryId = UUID.randomUUID();
        interestReceivableAccountId = UUID.randomUUID();
        interestIncomeAccountId = UUID.randomUUID();

        businessDate = LocalDate.of(2026, 9, 30);
        currency = Currency.VND;
        amount = new BigDecimal("150000.00");

        command = new RecordCreditInterestAccrualJournalCommand(
                accrualId,
                businessDate,
                currency,
                amount
        );
    }

    // -------------------------------------------------------------------------
    // Success cases
    // -------------------------------------------------------------------------

    @Test
    void execute_shouldRecordJournalWithDebitAndCreditLines_whenCommandIsValid() {
        givenInterestAccounts(currency);
        givenSavedJournalEntry(journalEntryId);

        UUID result = handler.execute(command);

        assertEquals(journalEntryId, result);

        ArgumentCaptor<JournalEntry> journalCaptor =
                ArgumentCaptor.forClass(JournalEntry.class);

        verify(journalEntryCommandRepository)
                .save(journalCaptor.capture());

        JournalEntry journalEntry = journalCaptor.getValue();

        assertEquals(
                JournalSourceType.CREDIT_INTEREST_ACCRUAL,
                journalEntry.getSourceType()
        );
        assertEquals(accrualId, journalEntry.getSourceId());
        assertEquals(businessDate, journalEntry.getBusinessDate());

        ArgumentCaptor<JournalEntryLine> lineCaptor =
                ArgumentCaptor.forClass(JournalEntryLine.class);

        verify(journalEntryLineCommandRepository, times(2))
                .save(lineCaptor.capture());

        List<JournalEntryLine> lines = lineCaptor.getAllValues();

        assertEquals(2, lines.size());

        JournalEntryLine debitLine = lines.get(0);
        JournalEntryLine creditLine = lines.get(1);

        assertEquals(journalEntryId, debitLine.getJournalEntryId());
        assertEquals(
                interestReceivableAccountId,
                debitLine.getLedgerAccountId()
        );
        assertEquals(EntryType.DEBIT, debitLine.getEntryType());
        assertBigDecimalEquals(amount, debitLine.getAmount());
        assertEquals(currency, debitLine.getCurrency());

        assertEquals(journalEntryId, creditLine.getJournalEntryId());
        assertEquals(
                interestIncomeAccountId,
                creditLine.getLedgerAccountId()
        );
        assertEquals(EntryType.CREDIT, creditLine.getEntryType());
        assertBigDecimalEquals(amount, creditLine.getAmount());
        assertEquals(currency, creditLine.getCurrency());

        assertBigDecimalEquals(
                debitLine.getAmount(),
                creditLine.getAmount()
        );

        verify(systemLedgerAccountService)
                .getInterestReceivableAccount(currency);
        verify(systemLedgerAccountService)
                .getInterestIncomeAccount(currency);
    }

    @Test
    void execute_shouldReturnSavedJournalEntryId_whenJournalIsCreated() {
        givenInterestAccounts(currency);
        givenSavedJournalEntry(journalEntryId);

        UUID result = handler.execute(command);

        assertNotNull(result);
        assertEquals(journalEntryId, result);
    }

    @Test
    void execute_shouldUseCommandCurrency_whenResolvingSystemAccounts() {
        Currency commandCurrency = Currency.USD;

        RecordCreditInterestAccrualJournalCommand usdCommand =
                new RecordCreditInterestAccrualJournalCommand(
                        accrualId,
                        businessDate,
                        commandCurrency,
                        amount
                );

        givenInterestAccounts(commandCurrency);
        givenSavedJournalEntry(journalEntryId);

        UUID result = handler.execute(usdCommand);

        assertEquals(journalEntryId, result);

        verify(systemLedgerAccountService)
                .getInterestReceivableAccount(commandCurrency);
        verify(systemLedgerAccountService)
                .getInterestIncomeAccount(commandCurrency);
    }

    // -------------------------------------------------------------------------
    // Command validation
    // -------------------------------------------------------------------------

    @Test
    void execute_shouldThrowInvalidRequest_whenCommandIsNull() {
        assertInvalidCommand(null);
    }

    @Test
    void execute_shouldThrowInvalidRequest_whenAccrualIdIsNull() {
        assertInvalidCommand(
                new RecordCreditInterestAccrualJournalCommand(
                        null,
                        businessDate,
                        currency,
                        amount
                )
        );
    }

    @Test
    void execute_shouldThrowInvalidRequest_whenBusinessDateIsNull() {
        assertInvalidCommand(
                new RecordCreditInterestAccrualJournalCommand(
                        accrualId,
                        null,
                        currency,
                        amount
                )
        );
    }

    @Test
    void execute_shouldThrowInvalidRequest_whenCurrencyIsNull() {
        assertInvalidCommand(
                new RecordCreditInterestAccrualJournalCommand(
                        accrualId,
                        businessDate,
                        null,
                        amount
                )
        );
    }

    @Test
    void execute_shouldThrowInvalidRequest_whenAmountIsNull() {
        assertInvalidCommand(
                new RecordCreditInterestAccrualJournalCommand(
                        accrualId,
                        businessDate,
                        currency,
                        null
                )
        );
    }

    @Test
    void execute_shouldThrowInvalidRequest_whenAmountIsZero() {
        assertInvalidCommand(
                new RecordCreditInterestAccrualJournalCommand(
                        accrualId,
                        businessDate,
                        currency,
                        BigDecimal.ZERO
                )
        );
    }

    @Test
    void execute_shouldThrowInvalidRequest_whenAmountIsNegative() {
        assertInvalidCommand(
                new RecordCreditInterestAccrualJournalCommand(
                        accrualId,
                        businessDate,
                        currency,
                        new BigDecimal("-0.01")
                )
        );
    }

    // -------------------------------------------------------------------------
    // Dependency exception cases
    // -------------------------------------------------------------------------

    @Test
    void execute_shouldPropagateException_whenInterestReceivableAccountLookupFails() {
        RuntimeException expected =
                new RuntimeException(
                        "Interest receivable account lookup failed"
                );

        when(systemLedgerAccountService
                .getInterestReceivableAccount(currency))
                .thenThrow(expected);

        RuntimeException actual = assertThrows(
                RuntimeException.class,
                () -> handler.execute(command)
        );

        assertSame(expected, actual);

        verify(systemLedgerAccountService)
                .getInterestReceivableAccount(currency);
        verify(systemLedgerAccountService, never())
                .getInterestIncomeAccount(any());

        verifyNoInteractions(
                journalEntryCommandRepository,
                journalEntryLineCommandRepository
        );
    }

    @Test
    void execute_shouldPropagateException_whenInterestIncomeAccountLookupFails() {
        when(systemLedgerAccountService
                .getInterestReceivableAccount(currency))
                .thenReturn(interestReceivableAccount);

        RuntimeException expected =
                new RuntimeException(
                        "Interest income account lookup failed"
                );

        when(systemLedgerAccountService
                .getInterestIncomeAccount(currency))
                .thenThrow(expected);

        RuntimeException actual = assertThrows(
                RuntimeException.class,
                () -> handler.execute(command)
        );

        assertSame(expected, actual);

        verify(systemLedgerAccountService)
                .getInterestReceivableAccount(currency);
        verify(systemLedgerAccountService)
                .getInterestIncomeAccount(currency);

        verifyNoInteractions(
                journalEntryCommandRepository,
                journalEntryLineCommandRepository
        );
    }

    @Test
    void execute_shouldPropagateException_whenSavingJournalEntryFails() {
        when(systemLedgerAccountService
                .getInterestReceivableAccount(currency))
                .thenReturn(interestReceivableAccount);
        when(systemLedgerAccountService
                .getInterestIncomeAccount(currency))
                .thenReturn(interestIncomeAccount);

        RuntimeException expected =
                new RuntimeException("Journal entry save failed");

        when(journalEntryCommandRepository.save(any(JournalEntry.class)))
                .thenThrow(expected);

        RuntimeException actual = assertThrows(
                RuntimeException.class,
                () -> handler.execute(command)
        );

        assertSame(expected, actual);

        verify(systemLedgerAccountService)
                .getInterestReceivableAccount(currency);
        verify(systemLedgerAccountService)
                .getInterestIncomeAccount(currency);
        verify(journalEntryCommandRepository)
                .save(any(JournalEntry.class));

        verifyNoInteractions(journalEntryLineCommandRepository);
    }

    @Test
    void execute_shouldPropagateException_whenSavingDebitLineFails() {
        givenInterestAccounts(currency);
        givenSavedJournalEntry(journalEntryId);

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

    private void givenInterestAccounts(Currency accountCurrency) {
        when(systemLedgerAccountService
                .getInterestReceivableAccount(accountCurrency))
                .thenReturn(interestReceivableAccount);
        when(systemLedgerAccountService
                .getInterestIncomeAccount(accountCurrency))
                .thenReturn(interestIncomeAccount);

        when(interestReceivableAccount.getId())
                .thenReturn(interestReceivableAccountId);
        when(interestIncomeAccount.getId())
                .thenReturn(interestIncomeAccountId);
    }

    private void givenSavedJournalEntry(UUID id) {
        JournalEntry savedJournalEntry = mock(JournalEntry.class);

        when(journalEntryCommandRepository.save(any(JournalEntry.class)))
                .thenReturn(savedJournalEntry);
        when(savedJournalEntry.getId())
                .thenReturn(id);
    }

    private void assertInvalidCommand(
            RecordCreditInterestAccrualJournalCommand invalidCommand
    ) {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(invalidCommand)
        );

        assertEquals(
                ErrorCode.INVALID_REQUEST,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                systemLedgerAccountService,
                journalEntryCommandRepository,
                journalEntryLineCommandRepository
        );
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