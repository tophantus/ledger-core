package com.example.ledgercore.transaction.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.common.lock.DistributedLock;
import com.example.ledgercore.common.lock.LockKeyPrefix;
import com.example.ledgercore.transaction.command.port.outbound.AccountTransferPort;
import com.example.ledgercore.transaction.command.port.outbound.LedgerTransferPort;
import com.example.ledgercore.transaction.command.port.outbound.TransactionEventPort;
import com.example.ledgercore.transaction.command.repository.TransactionCommandRepository;
import com.example.ledgercore.transaction.command.repository.TransferIntentCommandRepository;
import com.example.ledgercore.transaction.entity.MoneyTransaction;
import com.example.ledgercore.transaction.entity.TransferIntent;
import com.example.ledgercore.transaction.enums.TransactionStatus;
import com.example.ledgercore.transaction.enums.TransactionType;
import com.example.ledgercore.transaction.event.TransferCompletedEvent;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConfirmTransferExecutionService {

    private final TransactionCommandRepository
            transactionCommandRepository;

    private final TransferIntentCommandRepository
            transferIntentCommandRepository;

    private final AccountTransferPort accountTransferPort;

    private final LedgerTransferPort ledgerTransferPort;

    private final TransactionEventPort transactionEventPort;

    private final Clock clock;

    @Transactional
    @DistributedLock(
            keys = {
                    "#sourceAccountId",
                    "#destinationAccountId"
            },
            prefix = LockKeyPrefix.ACCOUNT
    )
    public TransactionResponse execute(
            UUID userId,
            UUID intentId,
            UUID sourceAccountId,
            UUID destinationAccountId
    ) {

        TransferIntent intent =
                transferIntentCommandRepository
                        .findById(intentId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.TRANSFER_INTENT_NOT_FOUND
                                )
                        );

        validateOwner(
                userId,
                intent
        );

        validateAccountIds(
                intent,
                sourceAccountId,
                destinationAccountId
        );

        Instant now = Instant.now(clock);

        validateIntent(
                intent,
                now
        );

        AccountTransferPort.TransferAccountInfo transferInfo =
                accountTransferPort.getTransferInfo(
                        userId,
                        sourceAccountId,
                        destinationAccountId
                );

        validateTransfer(
                intent,
                transferInfo
        );

        MoneyTransaction transaction =
                createTransaction(
                        intent,
                        transferInfo
                );

        transactionCommandRepository.save(transaction);

        accountTransferPort.transfer(
                transferInfo.sourceAccountId(),
                transferInfo.destinationAccountId(),
                intent.getAmount()
        );

        ledgerTransferPort.recordTransfer(
                transaction.getId(),
                transferInfo.sourceAccountId(),
                transferInfo.destinationAccountId(),
                intent.getAmount(),
                intent.getCurrency()
        );

        completeTransaction(
                transaction,
                now
        );

        intent.complete(now);

        transactionEventPort.publishTransferCompleted(
                new TransferCompletedEvent(
                        transaction.getId(),
                        transaction.getReference(),
                        transaction.getSourceAccountId(),
                        transaction.getDestinationAccountId(),
                        transaction.getAmount(),
                        transaction.getCurrency(),
                        transaction.getCompletedAt()
                )
        );

        return toResponse(transaction);
    }

    private void validateOwner(
            UUID userId,
            TransferIntent intent
    ) {
        if (!intent.getUserId().equals(userId)) {
            throw new BusinessException(
                    ErrorCode.ACCESS_DENIED
            );
        }
    }

    private void validateAccountIds(
            TransferIntent intent,
            UUID sourceAccountId,
            UUID destinationAccountId
    ) {
        if (sourceAccountId.equals(destinationAccountId)) {
            throw new BusinessException(
                    ErrorCode.SAME_ACCOUNT_TRANSFER
            );
        }

        if (!intent.getSourceAccountId()
                .equals(sourceAccountId)
                || !intent.getDestinationAccountId()
                .equals(destinationAccountId)) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    private void validateIntent(
            TransferIntent intent,
            Instant now
    ) {
        if (!intent.isPending()) {
            throw new BusinessException(
                    ErrorCode.INVALID_TRANSACTION_STATUS
            );
        }

        if (intent.isExpired(now)) {
            throw new BusinessException(
                    ErrorCode.TRANSFER_INTENT_EXPIRED
            );
        }
    }

    private void validateTransfer(
            TransferIntent intent,
            AccountTransferPort.TransferAccountInfo transferInfo
    ) {
        if (!transferInfo.currency()
                .equalsIgnoreCase(intent.getCurrency())) {

            throw new BusinessException(
                    ErrorCode.TRANSACTION_CURRENCY_MISMATCH
            );
        }

        if (transferInfo.sourceBalance()
                .compareTo(intent.getAmount()) < 0) {

            throw new BusinessException(
                    ErrorCode.ACCOUNT_INSUFFICIENT_BALANCE
            );
        }
    }

    private MoneyTransaction createTransaction(
            TransferIntent intent,
            AccountTransferPort.TransferAccountInfo transferInfo
    ) {
        return MoneyTransaction.builder()
                .reference(intent.getReference())
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.PENDING)
                .sourceAccountId(
                        transferInfo.sourceAccountId()
                )
                .destinationAccountId(
                        transferInfo.destinationAccountId()
                )
                .amount(intent.getAmount())
                .currency(intent.getCurrency())
                .description(intent.getDescription())
                .build();
    }

    private void completeTransaction(
            MoneyTransaction transaction,
            Instant now
    ) {
        transaction.setStatus(
                TransactionStatus.COMPLETED
        );

        transaction.setCompletedAt(now);
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