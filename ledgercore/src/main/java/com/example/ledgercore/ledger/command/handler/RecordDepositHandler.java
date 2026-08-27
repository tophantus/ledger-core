package com.example.ledgercore.ledger.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.ledger.command.dto.RecordDepositCommand;
import com.example.ledgercore.ledger.command.port.inbound.RecordDepositUseCase;
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
public class RecordDepositHandler
        implements RecordDepositUseCase {

    private final JournalEntryCommandRepository journalEntryCommandRepository;
    private final JournalEntryLineCommandRepository journalEntryLineCommandRepository;
    private final AccountLedgerMappingPort accountLedgerMappingPort;
    private final SystemLedgerAccountService systemLedgerAccountService;

    @Override
    @Transactional
    public void execute(RecordDepositCommand command) {
        validateCommand(command);

        LedgerAccount sourceLedgerAccount =
                systemLedgerAccountService.getCashAccount(
                        command.currency()
                );

        UUID destinationLedgerAccountId =
                accountLedgerMappingPort.getLedgerAccountId(
                        command.destinationAccountId()
                );

        JournalEntry journalEntry =
                JournalEntry.builder()
                        .transactionId(command.transactionId())
                        .build();

        JournalEntry savedJournalEntry =
                journalEntryCommandRepository.save(journalEntry);

        JournalEntryLine debitLine =
                JournalEntryLine.builder()
                        .journalEntryId(savedJournalEntry.getId())
                        .ledgerAccountId(sourceLedgerAccount.getId())
                        .entryType(EntryType.DEBIT)
                        .amount(command.amount())
                        .currency(command.currency())
                        .build();

        JournalEntryLine creditLine =
                JournalEntryLine.builder()
                        .journalEntryId(savedJournalEntry.getId())
                        .ledgerAccountId(destinationLedgerAccountId)
                        .entryType(EntryType.CREDIT)
                        .amount(command.amount())
                        .currency(command.currency())
                        .build();

        journalEntryLineCommandRepository.save(debitLine);
        journalEntryLineCommandRepository.save(creditLine);
    }

    private void validateCommand(
            RecordDepositCommand command
    ) {
        if (command == null
                || command.transactionId() == null
                || command.destinationAccountId() == null
                || command.currency() == null
                || command.currency().isBlank()) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (command.amount() == null
                || command.amount().signum() <= 0) {

            throw new BusinessException(
                    ErrorCode.INVALID_TRANSFER_AMOUNT
            );
        }
    }
}