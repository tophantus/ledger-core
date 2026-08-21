package com.example.ledgercore.ledger.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.ledger.command.dto.RecordWithdrawCommand;
import com.example.ledgercore.ledger.command.port.inbound.RecordWithdrawUseCase;
import com.example.ledgercore.ledger.command.port.outbound.AccountLedgerMappingPort;
import com.example.ledgercore.ledger.command.repository.LedgerEntryCommandRepository;
import com.example.ledgercore.ledger.entity.LedgerAccount;
import com.example.ledgercore.ledger.entity.LedgerEntry;
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

    private final LedgerEntryCommandRepository ledgerEntryCommandRepository;

    private final AccountLedgerMappingPort accountLedgerMappingPort;

    private final SystemLedgerAccountService systemLedgerAccountService;

    @Override
    @Transactional
    public void execute(
            RecordWithdrawCommand command
    ) {
        validateCommand(command);

        UUID customerLedgerAccountId =
                accountLedgerMappingPort.getLedgerAccountId(
                        command.sourceAccountId()
                );

        LedgerAccount systemCashAccount =
                systemLedgerAccountService.getCashAccount(
                        command.currency()
                );

        LedgerEntry debitEntry =
                LedgerEntry.builder()
                        .transactionId(command.transactionId())
                        .ledgerAccountId(
                                customerLedgerAccountId
                        )
                        .entryType(EntryType.DEBIT)
                        .amount(command.amount())
                        .currency(command.currency())
                        .build();

        LedgerEntry creditEntry =
                LedgerEntry.builder()
                        .transactionId(command.transactionId())
                        .ledgerAccountId(
                                systemCashAccount.getId()
                        )
                        .entryType(EntryType.CREDIT)
                        .amount(command.amount())
                        .currency(command.currency())
                        .build();

        ledgerEntryCommandRepository.save(debitEntry);
        ledgerEntryCommandRepository.save(creditEntry);
    }

    private void validateCommand(
            RecordWithdrawCommand command
    ) {
        if (command.transactionId() == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (command.sourceAccountId() == null) {
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

        if (command.currency() == null
                || command.currency().isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }
}