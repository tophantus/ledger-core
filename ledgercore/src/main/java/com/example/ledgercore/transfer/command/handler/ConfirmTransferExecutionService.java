package com.example.ledgercore.transfer.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.common.lock.DistributedLock;
import com.example.ledgercore.common.lock.LockKeyPrefix;
import com.example.ledgercore.transaction.command.dto.TransferMoneyCommand;
import com.example.ledgercore.transaction.command.port.outbound.TransferUserAccountPort;
import com.example.ledgercore.transfer.command.port.outbound.TransferTransactionPort;
import com.example.ledgercore.transfer.command.repository.TransferIntentCommandRepository;
import com.example.ledgercore.transfer.entity.TransferIntent;
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

    private final TransferIntentCommandRepository
            transferIntentCommandRepository;

    private final TransferUserAccountPort transferUserAccountPort;

    private final TransferTransactionPort
            transferTransactionPort;

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

        TransferUserAccountPort.TransferAccountInfo transferInfo =
                transferUserAccountPort.getTransferInfo(
                        userId,
                        sourceAccountId,
                        destinationAccountId
                );

        validateTransfer(
                intent,
                transferInfo
        );

        TransactionResponse response =
                transferTransactionPort.transfer(
                        new TransferMoneyCommand(
                                transferInfo.sourceAccountId(),
                                transferInfo.destinationAccountId(),
                                intent.getAmount(),
                                intent.getCurrency(),
                                intent.getReference(),
                                intent.getDescription()
                        )
                );

        intent.complete(now);

        return response;
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
            TransferUserAccountPort.TransferAccountInfo transferInfo
    ) {
        if (transferInfo.currency() != intent.getCurrency()) {

            throw new BusinessException(
                    ErrorCode.TRANSACTION_CURRENCY_MISMATCH
            );
        }

        if (transferInfo.sourceAvailableBalance()
                .compareTo(intent.getAmount()) < 0) {

            throw new BusinessException(
                    ErrorCode.ACCOUNT_INSUFFICIENT_BALANCE
            );
        }
    }
}