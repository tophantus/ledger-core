package com.example.ledgercore.withdrawal.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.common.lock.DistributedLock;
import com.example.ledgercore.common.lock.LockKeyPrefix;
import com.example.ledgercore.withdrawal.command.dto.ExecuteWithdrawalCommand;
import com.example.ledgercore.withdrawal.command.dto.ExecuteWithdrawalResponse;
import com.example.ledgercore.withdrawal.command.port.outbound.WithdrawalHoldPort;
import com.example.ledgercore.withdrawal.command.port.outbound.WithdrawalTransactionPort;
import com.example.ledgercore.withdrawal.command.repository.WithdrawalExecutionCommandRepository;
import com.example.ledgercore.withdrawal.command.repository.WithdrawalIntentCommandRepository;
import com.example.ledgercore.withdrawal.command.service.WithdrawalCodeHasher;
import com.example.ledgercore.withdrawal.entity.WithdrawalExecution;
import com.example.ledgercore.withdrawal.entity.WithdrawalIntent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExecuteWithdrawalExecutionService {

    private final WithdrawalIntentCommandRepository
            withdrawalIntentCommandRepository;

    private final WithdrawalExecutionCommandRepository
            withdrawalExecutionCommandRepository;

    private final WithdrawalCodeHasher
            withdrawalCodeHasher;

    private final WithdrawalTransactionPort
            withdrawalTransactionPort;

    private final WithdrawalHoldPort
            withdrawalHoldPort;

    private final Clock clock;

    @Transactional
    @DistributedLock(
            keys = "#accountId",
            prefix = LockKeyPrefix.ACCOUNT
    )
    public ExecuteWithdrawalResponse execute(
            ExecuteWithdrawalCommand command,
            UUID intentId,
            UUID accountId,
            UUID atmId
    ) {
        WithdrawalIntent intent =
                withdrawalIntentCommandRepository
                        .findById(intentId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.WITHDRAWAL_INTENT_NOT_FOUND
                                )
                        );

        validateAccount(
                intent,
                accountId
        );

        Instant now = Instant.now(clock);

        validateIntent(
                intent,
                now
        );

        validateWithdrawalCode(
                command.withdrawalCode(),
                intent
        );

        validateAmount(
                command.amount(),
                intent
        );

        if (withdrawalExecutionCommandRepository
                .existsByWithdrawalIntentId(intent.getId())) {

            throw new BusinessException(
                    ErrorCode.WITHDRAWAL_ALREADY_EXECUTED
            );
        }

        UUID transactionId =
                withdrawalTransactionPort.withdraw(
                        intent.getAccountId(),
                        intent.getAmount(),
                        intent.getCurrency(),
                        intent.getWithdrawalReference(),
                        "ATM withdrawal"
                );

        WithdrawalExecution execution =
                WithdrawalExecution.builder()
                        .withdrawalIntentId(
                                intent.getId()
                        )
                        .atmTerminalId(
                                atmId
                        )
                        .transactionId(
                                transactionId
                        )
                        .executedAt(now)
                        .createdAt(now)
                        .build();

        execution =
                withdrawalExecutionCommandRepository
                        .save(execution);

        withdrawalHoldPort.releaseHold(
                intent.getHoldId()
        );

        intent.complete(now);

        return new ExecuteWithdrawalResponse(
                execution.getId(),
                intent.getId(),
                transactionId,
                atmId,
                intent.getWithdrawalReference(),
                intent.getAmount(),
                intent.getCurrency(),
                execution.getExecutedAt()
        );
    }

    private void validateAccount(
            WithdrawalIntent intent,
            UUID accountId
    ) {
        if (!intent.getAccountId().equals(accountId)) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    private void validateIntent(
            WithdrawalIntent intent,
            Instant now
    ) {
        if (!intent.isReady()) {
            throw new BusinessException(
                    ErrorCode.WITHDRAWAL_INTENT_NOT_READY
            );
        }

        if (intent.isExpired(now)) {
            throw new BusinessException(
                    ErrorCode.WITHDRAWAL_INTENT_EXPIRED
            );
        }
    }

    private void validateWithdrawalCode(
            String withdrawalCode,
            WithdrawalIntent intent
    ) {
        if (!withdrawalCodeHasher.matches(
                withdrawalCode,
                intent.getWithdrawalCodeHash()
        )) {
            throw new BusinessException(
                    ErrorCode.WITHDRAWAL_CODE_INVALID
            );
        }
    }

    private void validateAmount(
            BigDecimal amount,
            WithdrawalIntent intent
    ) {
        if (amount.compareTo(
                intent.getAmount()
        ) != 0) {

            throw new BusinessException(
                    ErrorCode.WITHDRAWAL_AMOUNT_MISMATCH
            );
        }
    }
}