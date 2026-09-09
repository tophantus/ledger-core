package com.example.ledgercore.withdrawal.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.withdrawal.command.dto.ConfirmWithdrawalRequestCommand;
import com.example.ledgercore.withdrawal.command.dto.ConfirmWithdrawalRequestResponse;
import com.example.ledgercore.withdrawal.command.port.inbound.ConfirmWithdrawalRequestUseCase;
import com.example.ledgercore.withdrawal.command.port.outbound.WithdrawalOtpPort;
import com.example.ledgercore.withdrawal.command.repository.WithdrawalRequestCommandRepository;
import com.example.ledgercore.withdrawal.entity.WithdrawalRequest;
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

    private final WithdrawalOtpPort withdrawalOtpPort;

    private final ConfirmWithdrawalExecutionService
            confirmWithdrawalExecutionService;

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

        return confirmWithdrawalExecutionService.execute(
                command.userId(),
                request.getId(),
                request.getAccountId()
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