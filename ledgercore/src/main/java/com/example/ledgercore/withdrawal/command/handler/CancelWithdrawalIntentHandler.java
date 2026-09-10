package com.example.ledgercore.withdrawal.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.withdrawal.command.dto.CancelWithdrawalIntentCommand;
import com.example.ledgercore.withdrawal.command.port.inbound.CancelWithdrawalIntentUseCase;
import com.example.ledgercore.withdrawal.command.repository.WithdrawalIntentCommandRepository;
import com.example.ledgercore.withdrawal.entity.WithdrawalIntent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CancelWithdrawalIntentHandler
        implements CancelWithdrawalIntentUseCase {

    private final WithdrawalIntentCommandRepository
            withdrawalIntentCommandRepository;

    private final CancelWithdrawalIntentService
            cancelWithdrawalIntentService;

    @Override
    public void execute(
            CancelWithdrawalIntentCommand command
    ) {
        validateCommand(command);

        WithdrawalIntent intent =
                withdrawalIntentCommandRepository
                        .findById(command.intentId())
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.WITHDRAWAL_INTENT_NOT_FOUND
                                )
                        );

        if (!intent.getUserId().equals(command.userId())) {
            throw new BusinessException(
                    ErrorCode.ACCESS_DENIED
            );
        }

        cancelWithdrawalIntentService.cancel(
                intent.getId(),
                intent.getAccountId()
        );
    }

    private void validateCommand(
            CancelWithdrawalIntentCommand command
    ) {
        if (command == null
                || command.userId() == null
                || command.intentId() == null) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }
}