package com.example.ledgercore.ledger.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.ledger.command.dto.RecordTransferCommand;
import com.example.ledgercore.ledger.command.port.inbound.RecordTransferUseCase;
import com.example.ledgercore.ledger.command.port.outbound.AccountLedgerMappingPort;
import com.example.ledgercore.ledger.command.repository.JournalEntryCommandRepository;
import com.example.ledgercore.ledger.command.repository.JournalEntryLineCommandRepository;
import com.example.ledgercore.ledger.entity.JournalEntry;
import com.example.ledgercore.ledger.entity.JournalEntryLine;
import com.example.ledgercore.ledger.enums.EntryType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecordTransferHandler
        implements RecordTransferUseCase {

    private final JournalEntryCommandRepository journalEntryCommandRepository;
    private final JournalEntryLineCommandRepository journalEntryLineCommandRepository;
    private final AccountLedgerMappingPort accountLedgerMappingPort;

    @Override
    @Transactional
    public void execute(RecordTransferCommand command) {
        validateCommand(command);

        UUID sourceLedgerAccountId =
                accountLedgerMappingPort.getLedgerAccountId(
                        command.sourceAccountId()
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
                        .ledgerAccountId(sourceLedgerAccountId)
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
            RecordTransferCommand command
    ) {
        if (command == null
                || command.transactionId() == null
                || command.sourceAccountId() == null
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

        if (command.sourceAccountId()
                .equals(command.destinationAccountId())) {

            throw new BusinessException(
                    ErrorCode.SAME_ACCOUNT_TRANSFER
            );
        }
    }
}