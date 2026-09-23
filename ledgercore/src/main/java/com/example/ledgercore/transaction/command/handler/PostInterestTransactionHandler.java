package com.example.ledgercore.transaction.command.handler;

import com.example.ledgercore.common.currency.CurrencyAmountPolicy;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.common.lock.DistributedLock;
import com.example.ledgercore.common.lock.LockKeyPrefix;
import com.example.ledgercore.transaction.command.dto.PostInterestTransactionCommand;
import com.example.ledgercore.transaction.command.port.inbound.PostInterestTransactionUseCase;
import com.example.ledgercore.transaction.command.port.outbound.DepositUserAccountPort;
import com.example.ledgercore.transaction.command.port.outbound.InterestLedgerPort;
import com.example.ledgercore.transaction.command.repository.TransactionCommandRepository;
import com.example.ledgercore.transaction.entity.MoneyTransaction;
import com.example.ledgercore.transaction.enums.TransactionStatus;
import com.example.ledgercore.transaction.enums.TransactionType;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class PostInterestTransactionHandler
        implements PostInterestTransactionUseCase {

    private final TransactionCommandRepository transactionCommandRepository;
    private final DepositUserAccountPort depositUserAccountPort;
    private final InterestLedgerPort interestLedgerPort;

    @Override
    @DistributedLock(
            keys = "#command.accountId",
            prefix = LockKeyPrefix.ACCOUNT
    )
    @Transactional
    public TransactionResponse execute(
            PostInterestTransactionCommand command
    ) {
        validateCommand(command);

        DepositUserAccountPort.DepositAccountInfo depositInfo =
                depositUserAccountPort.getDepositInfo(
                        command.accountId()
                );

        validateCurrency(
                command,
                depositInfo
        );

        String reference =
                buildReference(command);

        MoneyTransaction transaction =
                MoneyTransaction.builder()
                        .reference(reference)
                        .type(TransactionType.INTEREST)
                        .status(TransactionStatus.PENDING)
                        .businessDate(command.businessDate())
                        .destinationAccountId(
                                command.accountId()
                        )
                        .amount(command.amount())
                        .currency(command.currency())
                        .description("Interest posting")
                        .build();

        transactionCommandRepository.save(transaction);

        depositUserAccountPort.deposit(
                command.accountId(),
                command.amount(),
                command.businessDate()
        );

        interestLedgerPort.recordInterestPosting(
                transaction.getId(),
                command.accountId(),
                command.amount(),
                command.currency(),
                command.businessDate()
        );

        completeTransaction(transaction);

        return toResponse(transaction);
    }

    private void validateCommand(
            PostInterestTransactionCommand command
    ) {
        if (command == null
                || command.accountId() == null
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

    private void validateCurrency(
            PostInterestTransactionCommand command,
            DepositUserAccountPort.DepositAccountInfo depositInfo
    ) {
        if (!depositInfo.currency()
                .equals(command.currency())) {

            throw new BusinessException(
                    ErrorCode.TRANSACTION_CURRENCY_MISMATCH
            );
        }
    }

    private String buildReference(
            PostInterestTransactionCommand command
    ) {
        return "INTEREST-" + command.accountId()
                + "-" + command.businessDate();
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
                null,
                transaction.getCreatedAt(),
                transaction.getCompletedAt()
        );
    }
}