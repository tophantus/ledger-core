package com.example.ledgercore.ledger.command.handler;

import com.example.ledgercore.common.currency.CurrencyAmountPolicy;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.ledger.command.dto.RecordCreditPaymentCommand;
import com.example.ledgercore.ledger.command.port.inbound.RecordCreditPaymentUseCase;
import com.example.ledgercore.ledger.command.port.outbound.AccountLedgerMappingPort;
import com.example.ledgercore.ledger.command.port.outbound.CreditFacilityLedgerMappingPort;
import com.example.ledgercore.ledger.command.repository.JournalEntryCommandRepository;
import com.example.ledgercore.ledger.command.repository.JournalEntryLineCommandRepository;
import com.example.ledgercore.ledger.entity.JournalEntry;
import com.example.ledgercore.ledger.entity.JournalEntryLine;
import com.example.ledgercore.ledger.enums.EntryType;
import com.example.ledgercore.ledger.enums.JournalSourceType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecordCreditPaymentHandler
        implements RecordCreditPaymentUseCase {

    private final JournalEntryCommandRepository
            journalEntryCommandRepository;

    private final JournalEntryLineCommandRepository
            journalEntryLineCommandRepository;

    private final CreditFacilityLedgerMappingPort
            creditFacilityLedgerMappingPort;

    private final AccountLedgerMappingPort
            accountLedgerMappingPort;

    @Override
    @Transactional
    public void execute(
            RecordCreditPaymentCommand command
    ) {
        validateCommand(command);

        UUID creditLedgerAccountId =
                creditFacilityLedgerMappingPort.getLedgerAccountId(
                        command.creditFacilityId()
                );

        UUID providerLedgerAccountId =
                accountLedgerMappingPort.getLedgerAccountId(
                        command.providerAccountId()
                );

        JournalEntry journalEntry =
                JournalEntry.builder()
                        .sourceType(JournalSourceType.TRANSACTION)
                        .sourceId(command.transactionId())
                        .businessDate(command.businessDate())
                        .build();

        JournalEntry savedJournalEntry =
                journalEntryCommandRepository.save(journalEntry);

        JournalEntryLine debitLine =
                JournalEntryLine.builder()
                        .journalEntryId(savedJournalEntry.getId())
                        .ledgerAccountId(providerLedgerAccountId)
                        .entryType(EntryType.DEBIT)
                        .amount(command.amount())
                        .currency(command.currency())
                        .build();

        JournalEntryLine creditLine =
                JournalEntryLine.builder()
                        .journalEntryId(savedJournalEntry.getId())
                        .ledgerAccountId(creditLedgerAccountId)
                        .entryType(EntryType.CREDIT)
                        .amount(command.amount())
                        .currency(command.currency())
                        .build();

        journalEntryLineCommandRepository.save(debitLine);
        journalEntryLineCommandRepository.save(creditLine);
    }

    private void validateCommand(
            RecordCreditPaymentCommand command
    ) {
        if (command == null
                || command.transactionId() == null
                || command.creditFacilityId() == null
                || command.providerAccountId() == null
                || command.currency() == null
                || command.businessDate() == null) {

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

        CurrencyAmountPolicy.validate(
                command.amount(),
                command.currency()
        );
    }
}