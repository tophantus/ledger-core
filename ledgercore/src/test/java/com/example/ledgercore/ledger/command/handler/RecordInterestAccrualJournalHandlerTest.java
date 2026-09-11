package com.example.ledgercore.ledger.command.handler;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.ledger.command.dto.RecordInterestAccrualJournalCommand;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecordInterestAccrualJournalHandlerTest {

    @Mock
    private JournalEntryCommandRepository journalEntryCommandRepository;

    @Mock
    private JournalEntryLineCommandRepository journalEntryLineCommandRepository;

    @Mock
    private SystemLedgerAccountService systemLedgerAccountService;

    private RecordInterestAccrualJournalHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RecordInterestAccrualJournalHandler(
                journalEntryCommandRepository,
                journalEntryLineCommandRepository,
                systemLedgerAccountService
        );
    }

    @Test
    void shouldRecordInterestAccrualJournal() {
        UUID accrualId = UUID.randomUUID();
        UUID journalEntryId = UUID.randomUUID();

        LocalDate businessDate = LocalDate.of(2026, 9, 7);

        BigDecimal amount =
                new BigDecimal("8219.1781");

        Currency currency = Currency.VND;

        RecordInterestAccrualJournalCommand command =
                new RecordInterestAccrualJournalCommand(
                        accrualId,
                        businessDate,
                        currency,
                        amount
                );

        LedgerAccount interestExpenseAccount =
                LedgerAccount.builder()
                        .id(UUID.randomUUID())
                        .code("INTEREST_EXPENSE_VND")
                        .currency(Currency.VND)
                        .build();

        LedgerAccount interestPayableAccount =
                LedgerAccount.builder()
                        .id(UUID.randomUUID())
                        .code("INTEREST_PAYABLE_VND")
                        .currency(Currency.VND)
                        .build();

        when(systemLedgerAccountService.getInterestExpenseAccount(currency))
                .thenReturn(interestExpenseAccount);

        when(systemLedgerAccountService.getInterestPayableAccount(currency))
                .thenReturn(interestPayableAccount);

        JournalEntry savedJournalEntry =
                JournalEntry.builder()
                        .id(journalEntryId)
                        .sourceType(JournalSourceType.INTEREST_ACCRUAL)
                        .sourceId(accrualId)
                        .businessDate(businessDate)
                        .build();

        when(journalEntryCommandRepository.save(any(JournalEntry.class)))
                .thenReturn(savedJournalEntry);

        UUID result = handler.execute(command);

        assertThat(result)
                .isEqualTo(journalEntryId);

        verify(systemLedgerAccountService)
                .getInterestExpenseAccount(currency);

        verify(systemLedgerAccountService)
                .getInterestPayableAccount(currency);

        ArgumentCaptor<JournalEntry> journalCaptor =
                ArgumentCaptor.forClass(JournalEntry.class);

        verify(journalEntryCommandRepository)
                .save(journalCaptor.capture());

        JournalEntry journalEntry =
                journalCaptor.getValue();

        assertThat(journalEntry.getSourceType())
                .isEqualTo(JournalSourceType.INTEREST_ACCRUAL);

        assertThat(journalEntry.getSourceId())
                .isEqualTo(accrualId);

        assertThat(journalEntry.getBusinessDate())
                .isEqualTo(businessDate);

        ArgumentCaptor<JournalEntryLine> lineCaptor =
                ArgumentCaptor.forClass(JournalEntryLine.class);

        verify(journalEntryLineCommandRepository, times(2))
                .save(lineCaptor.capture());

        assertThat(lineCaptor.getAllValues())
                .hasSize(2);

        JournalEntryLine debitLine =
                lineCaptor.getAllValues().getFirst();

        assertThat(debitLine.getJournalEntryId())
                .isEqualTo(journalEntryId);

        assertThat(debitLine.getLedgerAccountId())
                .isEqualTo(interestExpenseAccount.getId());

        assertThat(debitLine.getEntryType())
                .isEqualTo(EntryType.DEBIT);

        assertThat(debitLine.getAmount())
                .isEqualByComparingTo(amount);

        assertThat(debitLine.getCurrency())
                .isEqualTo(currency);

        JournalEntryLine creditLine =
                lineCaptor.getAllValues().get(1);

        assertThat(creditLine.getJournalEntryId())
                .isEqualTo(journalEntryId);

        assertThat(creditLine.getLedgerAccountId())
                .isEqualTo(interestPayableAccount.getId());

        assertThat(creditLine.getEntryType())
                .isEqualTo(EntryType.CREDIT);

        assertThat(creditLine.getAmount())
                .isEqualByComparingTo(amount);

        assertThat(creditLine.getCurrency())
                .isEqualTo(currency);
    }

    @Test
    void shouldRejectNullCommand() {
        assertThatThrownBy(() ->
                handler.execute(null)
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(
                        ErrorCode.INVALID_REQUEST.getMessage()
                );

        verifyNoInteractions(
                systemLedgerAccountService,
                journalEntryCommandRepository,
                journalEntryLineCommandRepository
        );
    }

    @Test
    void shouldRejectNullAccrualId() {
        RecordInterestAccrualJournalCommand command =
                new RecordInterestAccrualJournalCommand(
                        null,
                        LocalDate.of(2026, 9, 7),
                        Currency.VND,
                        new BigDecimal("1000")
                );

        assertThatThrownBy(() ->
                handler.execute(command)
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(
                        ErrorCode.INVALID_REQUEST.getMessage()
                );

        verifyNoInteractions(
                systemLedgerAccountService,
                journalEntryCommandRepository,
                journalEntryLineCommandRepository
        );
    }

    @Test
    void shouldRejectNullBusinessDate() {
        RecordInterestAccrualJournalCommand command =
                new RecordInterestAccrualJournalCommand(
                        UUID.randomUUID(),
                        null,
                        Currency.VND,
                        new BigDecimal("1000")
                );

        assertThatThrownBy(() ->
                handler.execute(command)
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(
                        ErrorCode.INVALID_REQUEST.getMessage()
                );

        verifyNoInteractions(
                systemLedgerAccountService,
                journalEntryCommandRepository,
                journalEntryLineCommandRepository
        );
    }

    @Test
    void shouldRejectNullAmount() {
        RecordInterestAccrualJournalCommand command =
                new RecordInterestAccrualJournalCommand(
                        UUID.randomUUID(),
                        LocalDate.of(2026, 9, 7),
                        Currency.VND,
                        null
                );

        assertThatThrownBy(() ->
                handler.execute(command)
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(
                        ErrorCode.INVALID_REQUEST.getMessage()
                );

        verifyNoInteractions(
                systemLedgerAccountService,
                journalEntryCommandRepository,
                journalEntryLineCommandRepository
        );
    }

    @Test
    void shouldRejectZeroAmount() {
        RecordInterestAccrualJournalCommand command =
                new RecordInterestAccrualJournalCommand(
                        UUID.randomUUID(),
                        LocalDate.of(2026, 9, 7),
                        Currency.VND,
                        BigDecimal.ZERO
                );

        assertThatThrownBy(() ->
                handler.execute(command)
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(
                        ErrorCode.INVALID_REQUEST.getMessage()
                );

        verifyNoInteractions(
                systemLedgerAccountService,
                journalEntryCommandRepository,
                journalEntryLineCommandRepository
        );
    }

    @Test
    void shouldRejectNegativeAmount() {
        RecordInterestAccrualJournalCommand command =
                new RecordInterestAccrualJournalCommand(
                        UUID.randomUUID(),
                        LocalDate.of(2026, 9, 7),
                        Currency.VND,
                        new BigDecimal("-1000")
                );

        assertThatThrownBy(() ->
                handler.execute(command)
        )
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(
                        ErrorCode.INVALID_REQUEST.getMessage()
                );

        verifyNoInteractions(
                systemLedgerAccountService,
                journalEntryCommandRepository,
                journalEntryLineCommandRepository
        );
    }
}