package com.example.ledgercore.hold.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.hold.command.dto.CreateAccountHoldCommand;
import com.example.ledgercore.hold.command.dto.CreateAccountHoldResponse;
import com.example.ledgercore.hold.command.port.inbound.CreateAccountHoldUseCase;
import com.example.ledgercore.hold.command.port.outbound.AccountHoldPort;
import com.example.ledgercore.hold.command.repository.AccountHoldCommandRepository;
import com.example.ledgercore.hold.entity.AccountHold;
import com.example.ledgercore.hold.enums.AccountHoldStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CreateAccountHoldHandler
        implements CreateAccountHoldUseCase {

    private final AccountHoldCommandRepository
            accountHoldCommandRepository;

    private final AccountHoldPort accountHoldPort;

    @Override
    @Transactional
    public CreateAccountHoldResponse execute(
            CreateAccountHoldCommand command
    ) {
        validateCommand(command);

        AccountHold hold =
                AccountHold.builder()
                        .accountId(command.accountId())
                        .amount(command.amount())
                        .currency(
                                command.currency()
                                        .trim()
                                        .toUpperCase()
                        )
                        .holdType(command.holdType())
                        .referenceType(command.referenceType())
                        .referenceId(command.referenceId())
                        .status(AccountHoldStatus.ACTIVE)
                        .build();

        hold = accountHoldCommandRepository.save(hold);

        accountHoldPort.increaseHold(
                hold.getAccountId(),
                hold.getAmount(),
                hold.getCurrency()
        );

        return new CreateAccountHoldResponse(
                hold.getId(),
                hold.getAccountId(),
                hold.getAmount(),
                hold.getCurrency(),
                hold.getStatus(),
                hold.getExpiresAt()
        );
    }

    private void validateCommand(
            CreateAccountHoldCommand command
    ) {
        if (command == null
                || command.accountId() == null
                || command.amount() == null
                || command.currency() == null
                || command.holdType() == null
                || command.referenceType() == null
                || command.referenceId() == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (command.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_HOLD_AMOUNT
            );
        }

        if (command.currency().isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }
}