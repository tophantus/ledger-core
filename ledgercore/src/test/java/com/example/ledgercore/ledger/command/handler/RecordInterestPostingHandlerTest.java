package com.example.ledgercore.ledger.command.handler;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.ledger.command.dto.RecordInterestPostingCommand;
import com.example.ledgercore.ledger.command.port.outbound.AccountLedgerMappingPort;
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
class RecordInterestPostingHandlerTest {

    @Mock
    private JournalEntryCommandRepository journalEntryCommandRepository;

    @Mock
    private JournalEntryLineCommandRepository journalEntryLineCommandRepository;

    @Mock
    private AccountLedgerMappingPort accountLedgerMappingPort;

    @Mock
    private SystemLedgerAccountService systemLedgerAccountService;

    private RecordInterestPostingHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RecordInterestPostingHandler(
                journalEntryCommandRepository,
                journalEntryLineCommandRepository,
                accountLedgerMappingPort,
                systemLedgerAccountService
        );
    }

    @Test
    void shouldRecordInterestPosting() {
        UUID transactionId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID journalEntryId = UUID.randomUUID();
        UUID interestPayableLedgerAccountId = UUID.randomUUID();
        UUID customerLedgerAccountId = UUID.randomUUID();

        BigDecimal amount = new BigDecimal("10000.0000");
        Currency currency = Currency.VND;
        LocalDate businessDate = LocalDate.of(2026, 9, 7);

        RecordInterestPostingCommand command =
                new RecordInterestPostingCommand(
                        transactionId,
                        accountId,
                        amount,
                        currency,
                        businessDate
                );

        LedgerAccount interestPayableAccount =
                LedgerAccount.builder()
                        .id(interestPayableLedgerAccountId)
                        .build();

        when(systemLedgerAccountService
                .getInterestPayableAccount(currency))
                .thenReturn(interestPayableAccount);

        when(accountLedgerMappingPort
                .getLedgerAccountId(accountId))
                .thenReturn(customerLedgerAccountId);

        JournalEntry savedJournalEntry =
                JournalEntry.builder()
                        .id(journalEntryId)
                        .sourceType(JournalSourceType.TRANSACTION)
                        .sourceId(transactionId)
                        .businessDate(businessDate)
                        .build();

        when(journalEntryCommandRepository.save(
                any(JournalEntry.class)
        )).thenReturn(savedJournalEntry);

        handler.execute(command);

        ArgumentCaptor<JournalEntry> journalCaptor =
                ArgumentCaptor.forClass(JournalEntry.class);

        verify(journalEntryCommandRepository)
                .save(journalCaptor.capture());

        JournalEntry journalEntry =
                journalCaptor.getValue();

        assertThat(journalEntry.getSourceType())
                .isEqualTo(JournalSourceType.TRANSACTION);

        assertThat(journalEntry.getSourceId())
                .isEqualTo(transactionId);

        assertThat(journalEntry.getBusinessDate())
                .isEqualTo(businessDate);

        ArgumentCaptor<JournalEntryLine> lineCaptor =
                ArgumentCaptor.forClass(JournalEntryLine.class);

        verify(journalEntryLineCommandRepository, times(2))
                .save(lineCaptor.capture());

        var lines = lineCaptor.getAllValues();

        assertThat(lines)
                .hasSize(2);

        JournalEntryLine debitLine = lines.get(0);
        JournalEntryLine creditLine = lines.get(1);

        assertThat(debitLine.getJournalEntryId())
                .isEqualTo(journalEntryId);

        assertThat(debitLine.getLedgerAccountId())
                .isEqualTo(interestPayableLedgerAccountId);

        assertThat(debitLine.getEntryType())
                .isEqualTo(EntryType.DEBIT);

        assertThat(debitLine.getAmount())
                .isEqualByComparingTo(amount);

        assertThat(debitLine.getCurrency())
                .isEqualTo(currency);

        assertThat(creditLine.getJournalEntryId())
                .isEqualTo(journalEntryId);

        assertThat(creditLine.getLedgerAccountId())
                .isEqualTo(customerLedgerAccountId);

        assertThat(creditLine.getEntryType())
                .isEqualTo(EntryType.CREDIT);

        assertThat(creditLine.getAmount())
                .isEqualByComparingTo(amount);

        assertThat(creditLine.getCurrency())
                .isEqualTo(currency);
    }

    @Test
    void shouldRejectNullCommand() {
        assertThatThrownBy(
                () -> handler.execute(null)
        )
                .isInstanceOf(BusinessException.class);

        verifyNoInteractions(
                systemLedgerAccountService,
                accountLedgerMappingPort,
                journalEntryCommandRepository,
                journalEntryLineCommandRepository
        );
    }

    @Test
    void shouldRejectMissingTransactionId() {
        RecordInterestPostingCommand command =
                new RecordInterestPostingCommand(
                        null,
                        UUID.randomUUID(),
                        new BigDecimal("10000"),
                        Currency.VND,
                        LocalDate.of(2026, 9, 7)
                );

        assertThatThrownBy(
                () -> handler.execute(command)
        )
                .isInstanceOf(BusinessException.class);

        verifyNoInteractions(
                systemLedgerAccountService,
                accountLedgerMappingPort,
                journalEntryCommandRepository,
                journalEntryLineCommandRepository
        );
    }

    @Test
    void shouldRejectMissingAccountId() {
        RecordInterestPostingCommand command =
                new RecordInterestPostingCommand(
                        UUID.randomUUID(),
                        null,
                        new BigDecimal("10000"),
                        Currency.VND,
                        LocalDate.of(2026, 9, 7)
                );

        assertThatThrownBy(
                () -> handler.execute(command)
        )
                .isInstanceOf(BusinessException.class);

        verifyNoInteractions(
                systemLedgerAccountService,
                accountLedgerMappingPort,
                journalEntryCommandRepository,
                journalEntryLineCommandRepository
        );
    }

    @Test
    void shouldRejectMissingBusinessDate() {
        RecordInterestPostingCommand command =
                new RecordInterestPostingCommand(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        new BigDecimal("10000"),
                        Currency.VND,
                        null
                );

        assertThatThrownBy(
                () -> handler.execute(command)
        )
                .isInstanceOf(BusinessException.class);

        verifyNoInteractions(
                systemLedgerAccountService,
                accountLedgerMappingPort,
                journalEntryCommandRepository,
                journalEntryLineCommandRepository
        );
    }

    @Test
    void shouldRejectZeroAmount() {
        RecordInterestPostingCommand command =
                new RecordInterestPostingCommand(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        BigDecimal.ZERO,
                        Currency.VND,
                        LocalDate.of(2026, 9, 7)
                );

        assertThatThrownBy(
                () -> handler.execute(command)
        )
                .isInstanceOf(BusinessException.class);

        verifyNoInteractions(
                systemLedgerAccountService,
                accountLedgerMappingPort,
                journalEntryCommandRepository,
                journalEntryLineCommandRepository
        );
    }

    @Test
    void shouldRejectNegativeAmount() {
        RecordInterestPostingCommand command =
                new RecordInterestPostingCommand(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        new BigDecimal("-1"),
                        Currency.VND,
                        LocalDate.of(2026, 9, 7)
                );

        assertThatThrownBy(
                () -> handler.execute(command)
        )
                .isInstanceOf(BusinessException.class);

        verifyNoInteractions(
                systemLedgerAccountService,
                accountLedgerMappingPort,
                journalEntryCommandRepository,
                journalEntryLineCommandRepository
        );
    }
}