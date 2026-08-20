package com.example.ledgercore.transaction.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.transaction.command.dto.TransferMoneyCommand;
import com.example.ledgercore.transaction.command.port.inbound.TransferMoneyUseCase;
import com.example.ledgercore.transaction.command.port.outbound.AccountTransferPort;
import com.example.ledgercore.transaction.command.port.outbound.LedgerTransferPort;
import com.example.ledgercore.transaction.command.repository.TransactionCommandRepository;
import com.example.ledgercore.transaction.entity.MoneyTransaction;
import com.example.ledgercore.transaction.enums.TransactionStatus;
import com.example.ledgercore.transaction.enums.TransactionType;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransferMoneyHandler implements TransferMoneyUseCase {

    private final TransactionCommandRepository transactionCommandRepository;
    private final AccountTransferPort accountTransferPort;
    private final LedgerTransferPort ledgerTransferPort;

    @Override
    @Transactional
    public TransactionResponse execute(
            UUID userId,
            TransferMoneyCommand command
    ) {
        validateAccounts(command);

        MoneyTransaction existingTransaction =
                transactionCommandRepository
                        .findByReference(command.reference())
                        .orElse(null);

        if (existingTransaction != null) {
            return handleExistingTransaction(
                    userId,
                    existingTransaction
            );
        }

        AccountTransferPort.TransferAccountInfo transferInfo =
                accountTransferPort.getTransferInfo(
                        userId,
                        command.sourceAccountId(),
                        command.destinationAccountId()
                );

        validateTransfer(command, transferInfo);

        MoneyTransaction transaction =
                createTransaction(command);

        transactionCommandRepository.save(transaction);

        accountTransferPort.transfer(
                command.sourceAccountId(),
                command.destinationAccountId(),
                command.amount()
        );

        ledgerTransferPort.recordTransfer(
                transaction.getId(),
                command.sourceAccountId(),
                command.destinationAccountId(),
                command.amount(),
                command.currency()
        );

        completeTransaction(transaction);

        return toResponse(transaction);
    }

    private void validateAccounts(
            TransferMoneyCommand command
    ) {
        if (command.sourceAccountId()
                .equals(command.destinationAccountId())) {

            throw new BusinessException(
                    ErrorCode.SAME_ACCOUNT_TRANSFER
            );
        }
    }

    private TransactionResponse handleExistingTransaction(
            UUID userId,
            MoneyTransaction transaction
    ) {
        accountTransferPort.verifySourceAccountAccess(
                userId,
                transaction.getSourceAccountId()
        );

        return toResponse(transaction);
    }

    private void validateTransfer(
            TransferMoneyCommand command,
            AccountTransferPort.TransferAccountInfo info
    ) {
        if (!info.currency().equals(command.currency())) {
            throw new BusinessException(
                    ErrorCode.TRANSACTION_CURRENCY_MISMATCH
            );
        }

        if (info.sourceBalance()
                .compareTo(command.amount()) < 0) {

            throw new BusinessException(
                    ErrorCode.ACCOUNT_INSUFFICIENT_BALANCE
            );
        }
    }

    private MoneyTransaction createTransaction(
            TransferMoneyCommand command
    ) {
        return MoneyTransaction.builder()
                .reference(command.reference())
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.PENDING)
                .sourceAccountId(command.sourceAccountId())
                .destinationAccountId(command.destinationAccountId())
                .amount(command.amount())
                .currency(command.currency())
                .description(command.description())
                .build();
    }

    private void completeTransaction(
            MoneyTransaction transaction
    ) {
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setCompletedAt(Instant.now());
    }

    private TransactionResponse toResponse(
            MoneyTransaction transaction
    ) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getReference(),
                transaction.getType(),
                transaction.getStatus(),
                transaction.getSourceAccountId(),
                transaction.getDestinationAccountId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getDescription(),
                transaction.getCreatedAt(),
                transaction.getCompletedAt()
        );
    }
}