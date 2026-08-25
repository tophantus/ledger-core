package com.example.ledgercore.transaction.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.common.lock.DistributedLock;
import com.example.ledgercore.common.lock.LockKeyPrefix;
import com.example.ledgercore.transaction.command.dto.WithdrawMoneyCommand;
import com.example.ledgercore.transaction.command.port.inbound.WithdrawMoneyUseCase;
import com.example.ledgercore.transaction.command.port.outbound.AccountWithdrawPort;
import com.example.ledgercore.transaction.command.port.outbound.LedgerWithdrawPort;
import com.example.ledgercore.transaction.command.port.outbound.TransactionEventPort;
import com.example.ledgercore.transaction.command.repository.TransactionCommandRepository;
import com.example.ledgercore.transaction.entity.MoneyTransaction;
import com.example.ledgercore.transaction.enums.TransactionStatus;
import com.example.ledgercore.transaction.enums.TransactionType;
import com.example.ledgercore.transaction.event.WithdrawCompletedEvent;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WithdrawMoneyHandler
        implements WithdrawMoneyUseCase {

    private final TransactionCommandRepository transactionCommandRepository;
    private final AccountWithdrawPort accountWithdrawPort;
    private final LedgerWithdrawPort ledgerWithdrawPort;
    private final TransactionEventPort transactionEventPort;

    @Override
    @DistributedLock(
            keys = "#command.sourceAccountId",
            prefix = LockKeyPrefix.ACCOUNT
    )
    @Transactional
    public TransactionResponse execute(
            UUID userId,
            WithdrawMoneyCommand command
    ) {
        validateAmount(command);

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

        AccountWithdrawPort.WithdrawAccountInfo withdrawInfo =
                accountWithdrawPort.getWithdrawInfo(
                        userId,
                        command.sourceAccountId()
                );

        validateWithdraw(
                command,
                withdrawInfo
        );

        MoneyTransaction transaction =
                createTransaction(command);

        transactionCommandRepository.save(transaction);

        accountWithdrawPort.withdraw(
                command.sourceAccountId(),
                command.amount()
        );

        ledgerWithdrawPort.recordWithdraw(
                transaction.getId(),
                command.sourceAccountId(),
                command.amount(),
                command.currency()
        );

        completeTransaction(transaction);

        transactionEventPort.publishWithdrawCompleted(
                new WithdrawCompletedEvent(
                        transaction.getId(),
                        transaction.getReference(),
                        transaction.getSourceAccountId(),
                        transaction.getAmount(),
                        transaction.getCurrency(),
                        transaction.getCompletedAt()
                )
        );

        return toResponse(transaction);
    }

    private void validateAmount(
            WithdrawMoneyCommand command
    ) {
        if (command.amount().signum() <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_WITHDRAW_AMOUNT
            );
        }
    }

    private void validateWithdraw(
            WithdrawMoneyCommand command,
            AccountWithdrawPort.WithdrawAccountInfo withdrawInfo
    ) {
        if (!withdrawInfo.currency()
                .equals(command.currency())) {

            throw new BusinessException(
                    ErrorCode.TRANSACTION_CURRENCY_MISMATCH
            );
        }

        if (withdrawInfo.balance()
                .compareTo(command.amount()) < 0) {

            throw new BusinessException(
                    ErrorCode.ACCOUNT_INSUFFICIENT_BALANCE
            );
        }
    }

    private MoneyTransaction createTransaction(
            WithdrawMoneyCommand command
    ) {
        return MoneyTransaction.builder()
                .reference(command.reference())
                .type(TransactionType.WITHDRAW)
                .status(TransactionStatus.PENDING)
                .sourceAccountId(
                        command.sourceAccountId()
                )
                .amount(command.amount())
                .currency(command.currency())
                .description(command.description())
                .build();
    }

    private TransactionResponse handleExistingTransaction(
            UUID userId,
            MoneyTransaction transaction
    ) {
        if (transaction.getType()
                != TransactionType.WITHDRAW) {

            throw new BusinessException(
                    ErrorCode.TRANSACTION_REFERENCE_ALREADY_EXISTS
            );
        }

        accountWithdrawPort.verifySourceAccountAccess(
                userId,
                transaction.getSourceAccountId()
        );

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