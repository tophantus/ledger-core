package com.example.ledgercore.transaction.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.common.lock.DistributedLock;
import com.example.ledgercore.common.lock.LockKeyPrefix;
import com.example.ledgercore.transaction.command.dto.DepositMoneyCommand;
import com.example.ledgercore.transaction.command.port.inbound.DepositMoneyUseCase;
import com.example.ledgercore.transaction.command.port.outbound.AccountDepositPort;
import com.example.ledgercore.transaction.command.port.outbound.LedgerDepositPort;
import com.example.ledgercore.transaction.command.port.outbound.TransactionEventPort;
import com.example.ledgercore.transaction.command.repository.TransactionCommandRepository;
import com.example.ledgercore.transaction.entity.MoneyTransaction;
import com.example.ledgercore.transaction.enums.TransactionStatus;
import com.example.ledgercore.transaction.enums.TransactionType;
import com.example.ledgercore.transaction.event.DepositCompletedEvent;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DepositMoneyHandler implements DepositMoneyUseCase {

    private final TransactionCommandRepository transactionCommandRepository;
    private final AccountDepositPort accountDepositPort;
    private final LedgerDepositPort ledgerDepositPort;
    private final TransactionEventPort transactionEventPort;

    @Override
    @DistributedLock(
            keys = "#command.destinationAccountId",
            prefix = LockKeyPrefix.ACCOUNT
    )
    @Transactional
    public TransactionResponse execute(
            UUID adminUserId,
            DepositMoneyCommand command
    ) {
        validateAmount(command);

        MoneyTransaction existingTransaction =
                transactionCommandRepository
                        .findByReference(command.reference())
                        .orElse(null);

        if (existingTransaction != null) {
            return handleExistingTransaction(
                    existingTransaction
            );
        }

        AccountDepositPort.DepositAccountInfo depositInfo =
                accountDepositPort.getDepositInfo(
                        command.destinationAccountId()
                );

        validateDeposit(
                command,
                depositInfo
        );

        MoneyTransaction transaction =
                createTransaction(
                        command
                );

        transactionCommandRepository.save(transaction);

        accountDepositPort.deposit(
                command.destinationAccountId(),
                command.amount()
        );

        ledgerDepositPort.recordDeposit(
                transaction.getId(),
                command.destinationAccountId(),
                command.amount(),
                command.currency()
        );

        completeTransaction(transaction);

        transactionEventPort.publishDepositCompleted(
                new DepositCompletedEvent(
                        transaction.getId(),
                        transaction.getReference(),
                        transaction.getDestinationAccountId(),
                        transaction.getAmount(),
                        transaction.getCurrency(),
                        transaction.getCompletedAt()
                )
        );

        return toResponse(transaction);
    }

    private void validateAmount(
            DepositMoneyCommand command
    ) {
        if (command.amount().signum() <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_DEPOSIT_AMOUNT
            );
        }
    }

    private void validateDeposit(
            DepositMoneyCommand command,
            AccountDepositPort.DepositAccountInfo depositInfo
    ) {
        if (!depositInfo.currency().equals(command.currency())) {
            throw new BusinessException(
                    ErrorCode.TRANSACTION_CURRENCY_MISMATCH
            );
        }
    }

    private MoneyTransaction createTransaction(
            DepositMoneyCommand command
    ) {
        return MoneyTransaction.builder()
                .reference(command.reference())
                .type(TransactionType.DEPOSIT)
                .status(TransactionStatus.PENDING)
                .destinationAccountId(
                        command.destinationAccountId()
                )
                .amount(command.amount())
                .currency(command.currency())
                .description(command.description())
                .build();
    }

    private TransactionResponse handleExistingTransaction(
            MoneyTransaction transaction
    ) {
        if (transaction.getType() != TransactionType.DEPOSIT) {
            throw new BusinessException(
                    ErrorCode.TRANSACTION_REFERENCE_ALREADY_EXISTS
            );
        }

        return toResponse(transaction);
    }

    private void completeTransaction(
            MoneyTransaction transaction
    ) {
        transaction.setStatus(
                TransactionStatus.COMPLETED
        );
        transaction.setCompletedAt(
                Instant.now()
        );
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