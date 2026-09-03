package com.example.ledgercore.ledger.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.ledger.command.dto.RecordWithdrawCommand;
import com.example.ledgercore.ledger.command.port.inbound.RecordWithdrawUseCase;
import com.example.ledgercore.ledger.command.port.outbound.AccountLedgerMappingPort;
import com.example.ledgercore.ledger.command.repository.JournalEntryCommandRepository;
import com.example.ledgercore.ledger.command.repository.JournalEntryLineCommandRepository;
import com.example.ledgercore.ledger.entity.JournalEntry;
import com.example.ledgercore.ledger.entity.JournalEntryLine;
import com.example.ledgercore.ledger.entity.LedgerAccount;
import com.example.ledgercore.ledger.enums.EntryType;
import com.example.ledgercore.ledger.service.SystemLedgerAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecordWithdrawHandler
        implements RecordWithdrawUseCase {

    private final JournalEntryCommandRepository journalEntryCommandRepository;
    private final JournalEntryLineCommandRepository journalEntryLineCommandRepository;
    private final AccountLedgerMappingPort accountLedgerMappingPort;
    private final SystemLedgerAccountService systemLedgerAccountService;

    @Override
    @Transactional
    public void execute(RecordWithdrawCommand command) {
        validateCommand(command);

        UUID customerLedgerAccountId =
                accountLedgerMappingPort.getLedgerAccountId(
                        command.sourceAccountId()
                );

        LedgerAccount systemCashAccount =
                systemLedgerAccountService.getCashAccount(
                        command.currency()
                );

        JournalEntry journalEntry =
                JournalEntry.builder()
                        .transactionId(command.transactionId())
                        .businessDate(command.businessDate())
                        .build();

        JournalEntry savedJournalEntry =
                journalEntryCommandRepository.save(journalEntry);

        JournalEntryLine debitLine =
                JournalEntryLine.builder()
                        .journalEntryId(savedJournalEntry.getId())
                        .ledgerAccountId(customerLedgerAccountId)
                        .entryType(EntryType.DEBIT)
                        .amount(command.amount())
                        .currency(command.currency())
                        .build();

        JournalEntryLine creditLine =
                JournalEntryLine.builder()
                        .journalEntryId(savedJournalEntry.getId())
                        .ledgerAccountId(systemCashAccount.getId())
                        .entryType(EntryType.CREDIT)
                        .amount(command.amount())
                        .currency(command.currency())
                        .build();

        journalEntryLineCommandRepository.save(debitLine);
        journalEntryLineCommandRepository.save(creditLine);
    }

    private void validateCommand(
            RecordWithdrawCommand command
    ) {
        if (command == null
                || command.transactionId() == null
                || command.sourceAccountId() == null
                || command.currency() == null
                || command.currency().isBlank()
                || command.businessDate() == null) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (command.amount() == null
                || command.amount().signum() <= 0) {

            throw new BusinessException(
                    ErrorCode.INVALID_WITHDRAW_AMOUNT
            );
        }
    }
}