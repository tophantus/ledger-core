package com.example.ledgercore.withdrawal.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.common.lock.DistributedLock;
import com.example.ledgercore.common.lock.LockKeyPrefix;
import com.example.ledgercore.withdrawal.command.dto.ConfirmWithdrawalRequestResponse;
import com.example.ledgercore.withdrawal.command.dto.CreateWithdrawalLookupCodeCommand;
import com.example.ledgercore.withdrawal.command.port.inbound.CreateWithdrawalLookupCodeUseCase;
import com.example.ledgercore.withdrawal.command.port.outbound.WithdrawalAccountInfo;
import com.example.ledgercore.withdrawal.command.port.outbound.WithdrawalAccountPort;
import com.example.ledgercore.withdrawal.command.port.outbound.WithdrawalHoldPort;
import com.example.ledgercore.withdrawal.command.port.outbound.WithdrawalNotificationPort;
import com.example.ledgercore.withdrawal.command.repository.WithdrawalIntentCommandRepository;
import com.example.ledgercore.withdrawal.command.repository.WithdrawalRequestCommandRepository;
import com.example.ledgercore.withdrawal.command.service.WithdrawalCodeGenerator;
import com.example.ledgercore.withdrawal.command.service.WithdrawalCodeHasher;
import com.example.ledgercore.withdrawal.command.service.WithdrawalReferenceGenerator;
import com.example.ledgercore.withdrawal.config.WithdrawalIntentProperties;
import com.example.ledgercore.withdrawal.entity.WithdrawalIntent;
import com.example.ledgercore.withdrawal.entity.WithdrawalRequest;
import com.example.ledgercore.withdrawal.enums.WithdrawalIntentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConfirmWithdrawalExecutionService {

    private final WithdrawalRequestCommandRepository
            withdrawalRequestCommandRepository;

    private final WithdrawalIntentCommandRepository
            withdrawalIntentCommandRepository;

    private final CreateWithdrawalLookupCodeUseCase
            createWithdrawalLookupCodeUseCase;

    private final WithdrawalAccountPort
            withdrawalAccountPort;

    private final WithdrawalHoldPort
            withdrawalHoldPort;

    private final WithdrawalNotificationPort
            withdrawalNotificationPort;

    private final WithdrawalReferenceGenerator
            withdrawalReferenceGenerator;

    private final WithdrawalCodeGenerator
            withdrawalCodeGenerator;

    private final WithdrawalCodeHasher
            withdrawalCodeHasher;

    private final WithdrawalIntentProperties
            withdrawalIntentProperties;

    private final Clock clock;

    @Transactional
    @DistributedLock(
            keys = "#accountId",
            prefix = LockKeyPrefix.ACCOUNT
    )
    public ConfirmWithdrawalRequestResponse execute(
            UUID userId,
            UUID requestId,
            UUID accountId
    ) {
        WithdrawalRequest request =
                withdrawalRequestCommandRepository
                        .findById(requestId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.WITHDRAWAL_REQUEST_NOT_FOUND
                                )
                        );

        validateOwnership(
                userId,
                request
        );

        Instant now = Instant.now(clock);

        validateRequest(
                request,
                now
        );

        WithdrawalAccountInfo account =
                withdrawalAccountPort.getWithdrawalInfo(
                        accountId
                );

        validateAccount(
                account,
                request
        );

        String withdrawalReference =
                withdrawalReferenceGenerator.generate();

        String withdrawalCode =
                withdrawalCodeGenerator.generate();

        String withdrawalCodeHash =
                withdrawalCodeHasher.hash(
                        withdrawalCode
                );

        Instant intentExpiresAt =
                now.plus(
                        withdrawalIntentProperties
                                .getExpiration()
                );

        UUID intentId = UUID.randomUUID();

        UUID holdId =
                withdrawalHoldPort.createHold(
                        intentId,
                        request.getAccountId(),
                        request.getAmount(),
                        request.getCurrency()
                );

        WithdrawalIntent intent =
                WithdrawalIntent.builder()
                        .id(intentId)
                        .withdrawalRequestId(request.getId())
                        .withdrawalReference(withdrawalReference)
                        .userId(request.getUserId())
                        .accountId(request.getAccountId())
                        .holdId(holdId)
                        .amount(request.getAmount())
                        .currency(request.getCurrency())
                        .withdrawalCodeHash(withdrawalCodeHash)
                        .status(WithdrawalIntentStatus.READY)
                        .expiresAt(intentExpiresAt)
                        .createdAt(now)
                        .build();

        withdrawalIntentCommandRepository.save(intent);

        var lookupCodeResponse =
                createWithdrawalLookupCodeUseCase.execute(
                        new CreateWithdrawalLookupCodeCommand(
                                intent.getId(),
                                intent.getExpiresAt()
                        )
                );

        request.confirm(now);

        withdrawalNotificationPort.sendWithdrawalCode(
                intent.getId(),
                intent.getUserId(),
                lookupCodeResponse.lookupCode(),
                withdrawalCode,
                intent.getAmount(),
                intent.getCurrency(),
                intent.getExpiresAt()
        );

        return new ConfirmWithdrawalRequestResponse(
                request.getId(),
                request.getStatus(),
                intent.getId(),
                intent.getWithdrawalReference(),
                intent.getAmount().toPlainString(),
                intent.getCurrency(),
                intent.getExpiresAt()
        );
    }

    private void validateOwnership(
            UUID userId,
            WithdrawalRequest request
    ) {
        if (!request.getUserId().equals(userId)) {
            throw new BusinessException(
                    ErrorCode.ACCESS_DENIED
            );
        }
    }

    private void validateRequest(
            WithdrawalRequest request,
            Instant now
    ) {
        if (!request.isPending()) {
            throw new BusinessException(
                    ErrorCode.WITHDRAWAL_REQUEST_NOT_PENDING
            );
        }

        if (request.isExpired(now)) {
            request.expire();

            throw new BusinessException(
                    ErrorCode.WITHDRAWAL_REQUEST_EXPIRED
            );
        }
    }

    private void validateAccount(
            WithdrawalAccountInfo account,
            WithdrawalRequest request
    ) {
        if (!account.userId().equals(request.getUserId())) {
            throw new BusinessException(
                    ErrorCode.ACCESS_DENIED
            );
        }

        if (account.currency() != request.getCurrency()) {

            throw new BusinessException(
                    ErrorCode.ACCOUNT_CURRENCY_MISMATCH
            );
        }

        if (account.availableBalance()
                .compareTo(request.getAmount()) < 0) {

            throw new BusinessException(
                    ErrorCode.ACCOUNT_INSUFFICIENT_BALANCE
            );
        }
    }
}