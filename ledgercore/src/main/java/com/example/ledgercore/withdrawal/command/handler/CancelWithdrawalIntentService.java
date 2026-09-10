package com.example.ledgercore.withdrawal.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.common.lock.DistributedLock;
import com.example.ledgercore.common.lock.LockKeyPrefix;
import com.example.ledgercore.withdrawal.command.port.outbound.WithdrawalHoldPort;
import com.example.ledgercore.withdrawal.command.repository.WithdrawalIntentCommandRepository;
import com.example.ledgercore.withdrawal.entity.WithdrawalIntent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CancelWithdrawalIntentService {

    private final WithdrawalIntentCommandRepository
            withdrawalIntentCommandRepository;

    private final WithdrawalHoldPort
            withdrawalHoldPort;

    @Transactional
    @DistributedLock(
            keys = "#accountId",
            prefix = LockKeyPrefix.ACCOUNT
    )
    public void cancel(
            UUID intentId,
            UUID accountId
    ) {
        WithdrawalIntent intent =
                withdrawalIntentCommandRepository
                        .findById(intentId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.WITHDRAWAL_INTENT_NOT_FOUND
                                )
                        );

        if (!intent.getAccountId().equals(accountId)) {
            throw new BusinessException(
                    ErrorCode.ACCESS_DENIED
            );
        }

        if (!intent.isReady()) {
            throw new BusinessException(
                    ErrorCode.WITHDRAWAL_INTENT_NOT_READY
            );
        }

        withdrawalHoldPort.releaseHold(
                intent.getHoldId()
        );

        intent.cancel();
    }
}