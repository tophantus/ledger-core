
package com.example.ledgercore.ledger.command.handler;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.ledger.command.dto.RecordCreditInterestPostingCommand;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecordCreditInterestPostingHandlerTest {

    @Mock
    private JournalEntryCommandRepository journalEntryCommandRepository;

    @Mock
    private JournalEntryLineCommandRepository journalEntryLineCommandRepository;

    @Mock
    private CreditFacilityLedgerMappingPort creditFacilityLedgerMappingPort;

    @Mock
    private SystemLedgerAccountService systemLedgerAccountService;

    @InjectMocks
    private RecordCreditInterestPostingHandler handler;

    private UUID transactionId;
    private UUID creditFacilityId;
    private UUID creditLedgerAccountId;
    private UUID interestReceivableAccountId;
    private UUID journalEntryId;
    private LocalDate businessDate;
    private BigDecimal amount;
    private RecordCreditInterestPostingCommand command;

    @BeforeEach
    void setUp() {
        transactionId = UUID.randomUUID();
        creditFacilityId = UUID.randomUUID();
        creditLedgerAccountId = UUID.randomUUID();
        interestReceivableAccountId = UUID.randomUUID();
        journalEntryId = UUID.randomUUID();

        businessDate = LocalDate.of(2026, 9, 30);
        amount = new BigDecimal("15000");

        command = new RecordCreditInterestPostingCommand(
                transactionId,
                creditFacilityId,
                amount,
                Currency.VND,
                businessDate
        );
    }

    @Test
    void execute_shouldCreateJournalEntryAndTwoLines_whenCommandIsValid() {
        givenCreditLedgerAccount();
        givenInterestReceivableAccount(Currency.VND);
        givenSavedJournalEntry();

        handler.execute(command);

        ArgumentCaptor<JournalEntry> journalCaptor =
                ArgumentCaptor.forClass(JournalEntry.class);

        verify(journalEntryCommandRepository)
                .save(journalCaptor.capture());

        JournalEntry journalEntry = journalCaptor.getValue();

        assertEquals(
                JournalSourceType.TRANSACTION,
                journalEntry.getSourceType()
        );
        assertEquals(transactionId, journalEntry.getSourceId());
        assertEquals(businessDate, journalEntry.getBusinessDate());

        ArgumentCaptor<JournalEntryLine> lineCaptor =
                ArgumentCaptor.forClass(JournalEntryLine.class);

        verify(journalEntryLineCommandRepository, times(2))
                .save(lineCaptor.capture());

        assertEquals(2, lineCaptor.getAllValues().size());

        JournalEntryLine debitLine = lineCaptor.getAllValues().get(0);
        JournalEntryLine creditLine = lineCaptor.getAllValues().get(1);

        assertEquals(journalEntryId, debitLine.getJournalEntryId());
        assertEquals(
                creditLedgerAccountId,
                debitLine.getLedgerAccountId()
        );
        assertEquals(EntryType.DEBIT, debitLine.getEntryType());
        assertBigDecimalEquals(amount, debitLine.getAmount());
        assertEquals(Currency.VND, debitLine.getCurrency());

        assertEquals(journalEntryId, creditLine.getJournalEntryId());
        assertEquals(
                interestReceivableAccountId,
                creditLine.getLedgerAccountId()
        );
        assertEquals(EntryType.CREDIT, creditLine.getEntryType());
        assertBigDecimalEquals(amount, creditLine.getAmount());
        assertEquals(Currency.VND, creditLine.getCurrency());

        verify(creditFacilityLedgerMappingPort)
                .getLedgerAccountId(creditFacilityId);

        verify(systemLedgerAccountService)
                .getInterestReceivableAccount(Currency.VND);
    }

    @Test
    void execute_shouldUseCommandCurrency_whenResolvingInterestAccount() {
        RecordCreditInterestPostingCommand usdCommand =
                new RecordCreditInterestPostingCommand(
                        transactionId,
                        creditFacilityId,
                        amount,
                        Currency.USD,
                        businessDate
                );

        givenCreditLedgerAccount();
        givenInterestReceivableAccount(Currency.USD);
        givenSavedJournalEntry();

        handler.execute(usdCommand);

        verify(systemLedgerAccountService)
                .getInterestReceivableAccount(Currency.USD);

        ArgumentCaptor<JournalEntryLine> lineCaptor =
                ArgumentCaptor.forClass(JournalEntryLine.class);

        verify(journalEntryLineCommandRepository, times(2))
                .save(lineCaptor.capture());

        assertEquals(
                Currency.USD,
                lineCaptor.getAllValues().get(0).getCurrency()
        );
        assertEquals(
                Currency.USD,
                lineCaptor.getAllValues().get(1).getCurrency()
        );
    }

    @Test
    void execute_shouldThrowInvalidRequest_whenCommandIsNull() {
        assertInvalidRequest(null);
    }

    @Test
    void execute_shouldThrowInvalidRequest_whenTransactionIdIsNull() {
        assertInvalidRequest(
                new RecordCreditInterestPostingCommand(
                        null,
                        creditFacilityId,
                        amount,
                        Currency.VND,
                        businessDate
                )
        );
    }

    @Test
    void execute_shouldThrowInvalidRequest_whenCreditFacilityIdIsNull() {
        assertInvalidRequest(
                new RecordCreditInterestPostingCommand(
                        transactionId,
                        null,
                        amount,
                        Currency.VND,
                        businessDate
                )
        );
    }

    @Test
    void execute_shouldThrowInvalidRequest_whenCurrencyIsNull() {
        assertInvalidRequest(
                new RecordCreditInterestPostingCommand(
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
                new RecordCreditInterestPostingCommand(
                        transactionId,
                        creditFacilityId,
                        amount,
                        Currency.VND,
                        null
                )
        );
    }

    @Test
    void execute_shouldThrowInvalidTransferAmount_whenAmountIsNull() {
        assertInvalidTransferAmount(
                new RecordCreditInterestPostingCommand(
                        transactionId,
                        creditFacilityId,
                        null,
                        Currency.VND,
                        businessDate
                )
        );
    }

    @Test
    void execute_shouldThrowInvalidTransferAmount_whenAmountIsZero() {
        assertInvalidTransferAmount(
                new RecordCreditInterestPostingCommand(
                        transactionId,
                        creditFacilityId,
                        BigDecimal.ZERO,
                        Currency.VND,
                        businessDate
                )
        );
    }

    @Test
    void execute_shouldThrowInvalidTransferAmount_whenAmountIsNegative() {
        assertInvalidTransferAmount(
                new RecordCreditInterestPostingCommand(
                        transactionId,
                        creditFacilityId,
                        new BigDecimal("-100"),
                        Currency.VND,
                        businessDate
                )
        );
    }

    @Test
    void execute_shouldNotCallDependencies_whenCommandIsInvalid() {
        RecordCreditInterestPostingCommand invalidCommand =
                new RecordCreditInterestPostingCommand(
                        null,
                        creditFacilityId,
                        amount,
                        Currency.VND,
                        businessDate
                );

        assertThrows(
                BusinessException.class,
                () -> handler.execute(invalidCommand)
        );

        verifyNoInteractions(
                journalEntryCommandRepository,
                journalEntryLineCommandRepository,
                creditFacilityLedgerMappingPort,
                systemLedgerAccountService
        );
    }

    @Test
    void execute_shouldPropagateException_whenResolvingCreditLedgerAccountFails() {
        RuntimeException exception =
                new RuntimeException("Credit ledger mapping failed");

        when(creditFacilityLedgerMappingPort.getLedgerAccountId(
                creditFacilityId
        )).thenThrow(exception);

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> handler.execute(command)
        );

        assertSame(exception, thrown);

        verify(creditFacilityLedgerMappingPort)
                .getLedgerAccountId(creditFacilityId);

        verifyNoInteractions(
                journalEntryCommandRepository,
                journalEntryLineCommandRepository,
                systemLedgerAccountService
        );
    }

    @Test
    void execute_shouldPropagateException_whenResolvingInterestAccountFails() {
        RuntimeException exception =
                new RuntimeException("Interest account lookup failed");

        givenCreditLedgerAccount();

        when(systemLedgerAccountService.getInterestReceivableAccount(
                Currency.VND
        )).thenThrow(exception);

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> handler.execute(command)
        );

        assertSame(exception, thrown);

        verify(creditFacilityLedgerMappingPort)
                .getLedgerAccountId(creditFacilityId);

        verify(systemLedgerAccountService)
                .getInterestReceivableAccount(Currency.VND);

        verifyNoInteractions(
                journalEntryCommandRepository,
                journalEntryLineCommandRepository
        );
    }

    @Test
    void execute_shouldPropagateException_whenSavingJournalEntryFails() {
        RuntimeException exception =
                new RuntimeException("Journal entry save failed");

        givenCreditLedgerAccount();
        givenInterestReceivableAccount(Currency.VND);

        when(journalEntryCommandRepository.save(any(JournalEntry.class)))
                .thenThrow(exception);

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> handler.execute(command)
        );

        assertSame(exception, thrown);

        verify(journalEntryCommandRepository)
                .save(any(JournalEntry.class));

        verifyNoInteractions(journalEntryLineCommandRepository);
    }

    @Test
    void execute_shouldPropagateException_whenSavingDebitLineFails() {
        RuntimeException exception =
                new RuntimeException("Debit line save failed");

        givenCreditLedgerAccount();
        givenInterestReceivableAccount(Currency.VND);
        givenSavedJournalEntry();

        when(journalEntryLineCommandRepository.save(
                any(JournalEntryLine.class)
        )).thenThrow(exception);

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> handler.execute(command)
        );

        assertSame(exception, thrown);

        verify(journalEntryCommandRepository)
                .save(any(JournalEntry.class));

        verify(journalEntryLineCommandRepository, times(1))
                .save(any(JournalEntryLine.class));
    }

    @Test
    void execute_shouldPropagateException_whenSavingCreditLineFails() {
        RuntimeException exception =
                new RuntimeException("Credit line save failed");

        givenCreditLedgerAccount();
        givenInterestReceivableAccount(Currency.VND);
        givenSavedJournalEntry();

        when(journalEntryLineCommandRepository.save(
                any(JournalEntryLine.class)
        ))
                .thenAnswer(invocation -> invocation.getArgument(0))
                .thenThrow(exception);

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> handler.execute(command)
        );

        assertSame(exception, thrown);

        verify(journalEntryLineCommandRepository, times(2))
                .save(any(JournalEntryLine.class));
    }

    private void givenCreditLedgerAccount() {
        when(creditFacilityLedgerMappingPort.getLedgerAccountId(
                creditFacilityId
        )).thenReturn(creditLedgerAccountId);
    }

    private void givenInterestReceivableAccount(Currency currency) {
        LedgerAccount account = mock(LedgerAccount.class);

        when(systemLedgerAccountService.getInterestReceivableAccount(
                currency
        )).thenReturn(account);

        when(account.getId()).thenReturn(interestReceivableAccountId);
    }

    private void givenSavedJournalEntry() {
        JournalEntry savedJournalEntry = mock(JournalEntry.class);

        when(savedJournalEntry.getId()).thenReturn(journalEntryId);

        when(journalEntryCommandRepository.save(any(JournalEntry.class)))
                .thenReturn(savedJournalEntry);
    }

    private void assertInvalidRequest(
            RecordCreditInterestPostingCommand invalidCommand
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
                journalEntryCommandRepository,
                journalEntryLineCommandRepository,
                creditFacilityLedgerMappingPort,
                systemLedgerAccountService
        );
    }

    private void assertInvalidTransferAmount(
            RecordCreditInterestPostingCommand invalidCommand
    ) {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> handler.execute(invalidCommand)
        );

        assertEquals(
                ErrorCode.INVALID_TRANSFER_AMOUNT,
                exception.getErrorCode()
        );

        verifyNoInteractions(
                journalEntryCommandRepository,
                journalEntryLineCommandRepository,
                creditFacilityLedgerMappingPort,
                systemLedgerAccountService
        );
    }

    private void assertBigDecimalEquals(
            BigDecimal expected,
            BigDecimal actual
    ) {
        assertNotNull(actual);
        assertEquals(0, expected.compareTo(actual));
    }
}