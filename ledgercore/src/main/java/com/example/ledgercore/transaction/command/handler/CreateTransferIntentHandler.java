package com.example.ledgercore.transaction.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.otp.enums.OtpPurpose;
import com.example.ledgercore.transaction.command.dto.CreateTransferIntentCommand;
import com.example.ledgercore.transaction.command.dto.CreateTransferIntentResult;
import com.example.ledgercore.transaction.command.port.inbound.CreateTransferIntentUseCase;
import com.example.ledgercore.transaction.command.port.outbound.AccountTransferPort;
import com.example.ledgercore.transaction.command.port.outbound.TransferOtpPort;
import com.example.ledgercore.transaction.command.repository.TransferIntentCommandRepository;
import com.example.ledgercore.transaction.entity.TransferIntent;
import com.example.ledgercore.transaction.enums.TransferIntentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateTransferIntentHandler
        implements CreateTransferIntentUseCase {

    private final AccountTransferPort accountTransferPort;
    private final TransferIntentCommandRepository
            transferIntentCommandRepository;
    private final TransferOtpPort transferOtpPort;
    private final Clock clock;

    @Override
    @Transactional
    public CreateTransferIntentResult execute(
            UUID userId,
            CreateTransferIntentCommand command
    ) {
        TransferIntent existingIntent =
                transferIntentCommandRepository
                        .findByReference(command.reference())
                        .orElse(null);

        if (existingIntent != null) {
            return handleExistingIntent(
                    userId,
                    existingIntent
            );
        }
        validateAmount(command);

        UUID destinationAccountId =
                accountTransferPort.getAccountIdByAccountNo(
                        command.destinationAccountNo()
                );

        if (command.sourceAccountId()
                .equals(destinationAccountId)) {

            throw new BusinessException(
                    ErrorCode.SAME_ACCOUNT_TRANSFER
            );
        }

        AccountTransferPort.TransferAccountInfo transferInfo =
                accountTransferPort.getTransferInfo(
                        userId,
                        command.sourceAccountId(),
                        destinationAccountId
                );

        validateTransfer(
                command,
                transferInfo
        );

        Instant now = Instant.now(clock);

        Instant expiresAt =
                now.plus(
                        OtpPurpose.CONFIRM_TRANSFER
                                .getExpiration()
                );

        TransferIntent intent =
                TransferIntent.builder()
                        .userId(userId)
                        .sourceAccountId(
                                transferInfo.sourceAccountId()
                        )
                        .destinationAccountId(
                                transferInfo.destinationAccountId()
                        )
                        .amount(command.amount())
                        .currency(command.currency())
                        .reference(command.reference())
                        .description(command.description())
                        .status(TransferIntentStatus.PENDING)
                        .expiresAt(expiresAt)
                        .createdAt(now)
                        .build();

        TransferIntent savedIntent =
                transferIntentCommandRepository.save(intent);

        transferOtpPort.sendConfirmationOtp(
                userId,
                savedIntent.getId()
        );

        return toResult(savedIntent);
    }

    private CreateTransferIntentResult handleExistingIntent(
            UUID userId,
            TransferIntent intent
    ) {
        if (!intent.getUserId().equals(userId)) {
            throw new BusinessException(
                    ErrorCode.ACCESS_DENIED
            );
        }

        return toResult(intent);
    }

    private void validateAmount(
            CreateTransferIntentCommand command
    ) {
        if (command.amount() == null
                || command.amount().signum() <= 0) {

            throw new BusinessException(
                    ErrorCode.INVALID_TRANSFER_AMOUNT
            );
        }
    }

    private void validateTransfer(
            CreateTransferIntentCommand command,
            AccountTransferPort.TransferAccountInfo transferInfo
    ) {
        if (!transferInfo.currency()
                .equalsIgnoreCase(command.currency())) {

            throw new BusinessException(
                    ErrorCode.TRANSACTION_CURRENCY_MISMATCH
            );
        }

        if (transferInfo.sourceBalance()
                .compareTo(command.amount()) < 0) {

            throw new BusinessException(
                    ErrorCode.ACCOUNT_INSUFFICIENT_BALANCE
            );
        }
    }

    private CreateTransferIntentResult toResult(
            TransferIntent intent
    ) {
        return new CreateTransferIntentResult(
                intent.getId(),
                intent.getSourceAccountId(),
                intent.getDestinationAccountId(),
                intent.getAmount(),
                intent.getCurrency(),
                intent.getReference(),
                intent.getStatus(),
                intent.getExpiresAt(),
                intent.getCreatedAt()
        );
    }
}