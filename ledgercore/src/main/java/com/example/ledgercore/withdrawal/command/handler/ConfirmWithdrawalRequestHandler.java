package com.example.ledgercore.withdrawal.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.withdrawal.command.dto.ConfirmWithdrawalRequestCommand;
import com.example.ledgercore.withdrawal.command.port.inbound.ConfirmWithdrawalRequestUseCase;
import com.example.ledgercore.withdrawal.command.port.outbound.WithdrawalNotificationPort;
import com.example.ledgercore.withdrawal.command.port.outbound.WithdrawalOtpPort;
import com.example.ledgercore.withdrawal.command.repository.WithdrawalIntentCommandRepository;
import com.example.ledgercore.withdrawal.command.repository.WithdrawalRequestCommandRepository;
import com.example.ledgercore.withdrawal.command.service.WithdrawalCodeGenerator;
import com.example.ledgercore.withdrawal.command.service.WithdrawalCodeHasher;
import com.example.ledgercore.withdrawal.command.service.WithdrawalReferenceGenerator;
import com.example.ledgercore.withdrawal.config.WithdrawalIntentProperties;
import com.example.ledgercore.withdrawal.entity.WithdrawalIntent;
import com.example.ledgercore.withdrawal.entity.WithdrawalRequest;
import com.example.ledgercore.withdrawal.enums.WithdrawalIntentStatus;
import com.example.ledgercore.withdrawal.query.dto.ConfirmWithdrawalRequestResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConfirmWithdrawalRequestHandler
        implements ConfirmWithdrawalRequestUseCase {

    private final WithdrawalRequestCommandRepository
            withdrawalRequestCommandRepository;

    private final WithdrawalIntentCommandRepository
            withdrawalIntentCommandRepository;

    private final WithdrawalOtpPort withdrawalOtpPort;

    private final WithdrawalNotificationPort withdrawalNotificationPort;

    private final WithdrawalReferenceGenerator
            withdrawalReferenceGenerator;

    private final WithdrawalCodeGenerator
            withdrawalCodeGenerator;

    private final WithdrawalCodeHasher
            withdrawalCodeHasher;

    private final WithdrawalIntentProperties
            withdrawalIntentProperties;

    private final Clock clock;

    @Override
    @Transactional
    public ConfirmWithdrawalRequestResponse execute(
            ConfirmWithdrawalRequestCommand command
    ) {
        validateCommand(command);

        WithdrawalRequest request =
                withdrawalRequestCommandRepository
                        .findById(command.requestId())
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.WITHDRAWAL_REQUEST_NOT_FOUND
                                )
                        );

        validateOwnership(
                request,
                command.userId()
        );

        Instant now = Instant.now(clock);

        validateRequest(
                request,
                now
        );

        withdrawalOtpPort.verifyConfirmationOtp(
                command.userId(),
                request.getId(),
                command.otp()
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

        WithdrawalIntent intent =
                WithdrawalIntent.builder()
                        .withdrawalRequestId(request.getId())
                        .withdrawalReference(withdrawalReference)
                        .userId(request.getUserId())
                        .accountId(request.getAccountId())
                        .amount(request.getAmount())
                        .currency(request.getCurrency())
                        .withdrawalCodeHash(withdrawalCodeHash)
                        .status(WithdrawalIntentStatus.READY)
                        .expiresAt(intentExpiresAt)
                        .createdAt(now)
                        .build();

        intent = withdrawalIntentCommandRepository.save(intent);

        request.confirm(now);

        withdrawalNotificationPort.sendWithdrawalCode(
                intent.getId(),
                intent.getUserId(),
                intent.getWithdrawalReference(),
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
                intent.getAmount(),
                intent.getCurrency(),
                intent.getExpiresAt()
        );
    }

    private void validateCommand(
            ConfirmWithdrawalRequestCommand command
    ) {
        if (command == null
                || command.userId() == null
                || command.requestId() == null
                || command.otp() == null
                || command.otp().isBlank()) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    private void validateOwnership(
            WithdrawalRequest request,
            UUID userId
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
}