package com.example.ledgercore.withdrawal.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.withdrawal.command.dto.ExecuteWithdrawalCommand;
import com.example.ledgercore.withdrawal.command.dto.ExecuteWithdrawalResponse;
import com.example.ledgercore.withdrawal.command.port.inbound.ExecuteWithdrawalUseCase;
import com.example.ledgercore.withdrawal.command.port.outbound.AtmAuthenticationPort;
import com.example.ledgercore.withdrawal.command.repository.WithdrawalIntentCommandRepository;
import com.example.ledgercore.withdrawal.entity.WithdrawalIntent;
import com.example.ledgercore.withdrawal.query.dto.GetWithdrawalIntentIdByLookupCodeQuery;
import com.example.ledgercore.withdrawal.query.port.inbound.GetWithdrawalIntentIdByLookupCodeUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExecuteWithdrawalHandler
        implements ExecuteWithdrawalUseCase {

    private final AtmAuthenticationPort
            atmAuthenticationPort;

    private final GetWithdrawalIntentIdByLookupCodeUseCase
            getWithdrawalIntentIdByLookupCodeUseCase;

    private final WithdrawalIntentCommandRepository
            withdrawalIntentCommandRepository;

    private final ExecuteWithdrawalExecutionService
            executeWithdrawalExecutionService;

    @Override
    public ExecuteWithdrawalResponse execute(
            ExecuteWithdrawalCommand command
    ) {
        validateCommand(command);

        UUID atmId =
                atmAuthenticationPort.authenticate(
                        command.terminalCode(),
                        command.credential()
                );

        UUID intentId =
                getWithdrawalIntentIdByLookupCodeUseCase.execute(
                        new GetWithdrawalIntentIdByLookupCodeQuery(
                                command.lookupCode()
                        )
                );

        WithdrawalIntent intent =
                withdrawalIntentCommandRepository
                        .findById(intentId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.WITHDRAWAL_INTENT_NOT_FOUND
                                )
                        );

        validateIntent(intent);

        return executeWithdrawalExecutionService.execute(
                command,
                intent.getId(),
                intent.getAccountId(),
                atmId
        );
    }

    private void validateCommand(
            ExecuteWithdrawalCommand command
    ) {
        if (command == null
                || command.terminalCode() == null
                || command.terminalCode().isBlank()
                || command.credential() == null
                || command.credential().isBlank()
                || command.lookupCode() == null
                || command.lookupCode().isBlank()
                || command.withdrawalCode() == null
                || command.withdrawalCode().isBlank()
                || command.amount() == null
                || command.amount().signum() <= 0) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }

    private void validateIntent(
            WithdrawalIntent intent
    ) {
        if (!intent.isReady()) {
            throw new BusinessException(
                    ErrorCode.WITHDRAWAL_INTENT_NOT_READY
            );
        }
    }
}