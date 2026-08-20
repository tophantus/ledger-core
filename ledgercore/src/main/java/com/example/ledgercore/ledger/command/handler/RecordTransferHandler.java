package com.example.ledgercore.ledger.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.ledger.command.dto.RecordTransferCommand;
import com.example.ledgercore.ledger.command.port.inbound.RecordTransferUseCase;
import com.example.ledgercore.ledger.command.port.outbound.AccountLedgerMappingPort;
import com.example.ledgercore.ledger.command.repository.LedgerEntryCommandRepository;
import com.example.ledgercore.ledger.entity.LedgerEntry;
import com.example.ledgercore.ledger.enums.EntryType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecordTransferHandler
        implements RecordTransferUseCase {

    private final LedgerEntryCommandRepository ledgerEntryRepository;
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

        LedgerEntry debitEntry = LedgerEntry.builder()
                .transactionId(command.transactionId())
                .ledgerAccountId(sourceLedgerAccountId)
                .entryType(EntryType.DEBIT)
                .amount(command.amount())
                .currency(command.currency())
                .build();

        LedgerEntry creditEntry = LedgerEntry.builder()
                .transactionId(command.transactionId())
                .ledgerAccountId(destinationLedgerAccountId)
                .entryType(EntryType.CREDIT)
                .amount(command.amount())
                .currency(command.currency())
                .build();

        ledgerEntryRepository.save(debitEntry);
        ledgerEntryRepository.save(creditEntry);
    }

    private void validateCommand(RecordTransferCommand command) {
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