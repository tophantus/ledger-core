package com.example.ledgercore.transaction.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.transaction.command.dto.ConfirmTransferCommand;
import com.example.ledgercore.transaction.command.port.inbound.ConfirmTransferUseCase;
import com.example.ledgercore.transaction.command.port.outbound.TransferOtpPort;
import com.example.ledgercore.transaction.command.repository.TransferIntentCommandRepository;
import com.example.ledgercore.transaction.entity.TransferIntent;
import com.example.ledgercore.transaction.query.dto.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConfirmTransferHandler
        implements ConfirmTransferUseCase {

    private final TransferIntentCommandRepository
            transferIntentCommandRepository;

    private final TransferOtpPort transferOtpPort;

    private final ConfirmTransferExecutionService
            confirmTransferExecutionService;

    private final Clock clock;

    @Override
    @Transactional
    public TransactionResponse execute(
            UUID userId,
            ConfirmTransferCommand command
    ) {
        TransferIntent intent =
                transferIntentCommandRepository
                        .findById(command.intentId())
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.TRANSFER_INTENT_NOT_FOUND
                                )
                        );

        validateOwner(
                userId,
                intent
        );

        validateIntent(
                intent,
                Instant.now(clock)
        );

        transferOtpPort.verifyConfirmationOtp(
                userId,
                intent.getId(),
                command.otp()
        );

        return confirmTransferExecutionService.execute(
                userId,
                intent.getId(),
                intent.getSourceAccountId(),
                intent.getDestinationAccountId()
        );
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
}