package com.example.ledgercore.ledger.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.ledger.command.dto.RecordCreditInterestAccrualJournalCommand;
import com.example.ledgercore.ledger.command.port.inbound.RecordCreditInterestAccrualJournalUseCase;
import com.example.ledgercore.ledger.command.repository.JournalEntryCommandRepository;
import com.example.ledgercore.ledger.command.repository.JournalEntryLineCommandRepository;
import com.example.ledgercore.ledger.entity.JournalEntry;
import com.example.ledgercore.ledger.entity.JournalEntryLine;
import com.example.ledgercore.ledger.entity.LedgerAccount;
import com.example.ledgercore.ledger.enums.EntryType;
import com.example.ledgercore.ledger.enums.JournalSourceType;
import com.example.ledgercore.ledger.service.SystemLedgerAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecordCreditInterestAccrualJournalHandler
        implements RecordCreditInterestAccrualJournalUseCase {

    private final JournalEntryCommandRepository
            journalEntryCommandRepository;

    private final JournalEntryLineCommandRepository
            journalEntryLineCommandRepository;

    private final SystemLedgerAccountService
            systemLedgerAccountService;

    @Override
    @Transactional
    public UUID execute(
            RecordCreditInterestAccrualJournalCommand command
    ) {
        validateCommand(command);

        LedgerAccount interestReceivableAccount =
                systemLedgerAccountService.getInterestReceivableAccount(
                        command.currency()
                );

        LedgerAccount interestIncomeAccount =
                systemLedgerAccountService.getInterestIncomeAccount(
                        command.currency()
                );

        JournalEntry journalEntry =
                JournalEntry.builder()
                        .sourceType(
                                JournalSourceType.CREDIT_INTEREST_ACCRUAL
                        )
                        .sourceId(command.accrualId())
                        .businessDate(command.businessDate())
                        .build();

        JournalEntry savedJournalEntry =
                journalEntryCommandRepository.save(journalEntry);

        JournalEntryLine debitLine =
                JournalEntryLine.builder()
                        .journalEntryId(savedJournalEntry.getId())
                        .ledgerAccountId(
                                interestReceivableAccount.getId()
                        )
                        .entryType(EntryType.DEBIT)
                        .amount(command.amount())
                        .currency(command.currency())
                        .build();

        JournalEntryLine creditLine =
                JournalEntryLine.builder()
                        .journalEntryId(savedJournalEntry.getId())
                        .ledgerAccountId(
                                interestIncomeAccount.getId()
                        )
                        .entryType(EntryType.CREDIT)
                        .amount(command.amount())
                        .currency(command.currency())
                        .build();

        journalEntryLineCommandRepository.save(debitLine);
        journalEntryLineCommandRepository.save(creditLine);

        return savedJournalEntry.getId();
    }

    private void validateCommand(
            RecordCreditInterestAccrualJournalCommand command
    ) {
        if (command == null
                || command.accrualId() == null
                || command.businessDate() == null
                || command.currency() == null) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (command.amount() == null
                || command.amount().signum() <= 0) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }
}