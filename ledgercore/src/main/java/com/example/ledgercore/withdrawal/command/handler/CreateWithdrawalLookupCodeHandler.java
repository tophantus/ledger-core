package com.example.ledgercore.withdrawal.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.withdrawal.command.dto.CreateWithdrawalLookupCodeCommand;
import com.example.ledgercore.withdrawal.command.dto.CreateWithdrawalLookupCodeResponse;
import com.example.ledgercore.withdrawal.command.port.inbound.CreateWithdrawalLookupCodeUseCase;
import com.example.ledgercore.withdrawal.command.repository.WithdrawalLookupCodeCommandRepository;
import com.example.ledgercore.withdrawal.command.service.WithdrawalLookupCodeGenerator;
import com.example.ledgercore.withdrawal.entity.WithdrawalLookupCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateWithdrawalLookupCodeHandler
        implements CreateWithdrawalLookupCodeUseCase {

    private final WithdrawalLookupCodeCommandRepository
            withdrawalLookupCodeCommandRepository;

    private final WithdrawalLookupCodeGenerator
            withdrawalLookupCodeGenerator;

    @Override
    @Transactional
    public CreateWithdrawalLookupCodeResponse execute(
            CreateWithdrawalLookupCodeCommand command
    ) {
        validateCommand(command);

        String lookupCode;

        do {
            lookupCode =
                    withdrawalLookupCodeGenerator.generate();
        } while (
                withdrawalLookupCodeCommandRepository
                        .existsByLookupCode(lookupCode)
        );

        WithdrawalLookupCode lookupCodeEntity =
                WithdrawalLookupCode.builder()
                        .withdrawalIntentId(
                                command.withdrawalIntentId()
                        )
                        .lookupCode(lookupCode)
                        .expiresAt(command.expiresAt())
                        .build();

        withdrawalLookupCodeCommandRepository.save(
                lookupCodeEntity
        );

        return new CreateWithdrawalLookupCodeResponse(
                lookupCode
        );
    }

    private void validateCommand(
            CreateWithdrawalLookupCodeCommand command
    ) {
        if (command == null
                || command.withdrawalIntentId() == null
                || command.expiresAt() == null) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }
}