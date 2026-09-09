package com.example.ledgercore.hold.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.hold.command.dto.ReleaseAccountHoldCommand;
import com.example.ledgercore.hold.command.port.inbound.ReleaseAccountHoldUseCase;
import com.example.ledgercore.hold.command.port.outbound.AccountHoldPort;
import com.example.ledgercore.hold.command.repository.AccountHoldCommandRepository;
import com.example.ledgercore.hold.entity.AccountHold;
import com.example.ledgercore.hold.enums.AccountHoldStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ReleaseAccountHoldHandler
        implements ReleaseAccountHoldUseCase {

    private final AccountHoldCommandRepository
            accountHoldCommandRepository;

    private final AccountHoldPort accountHoldPort;

    @Override
    @Transactional
    public void execute(
            ReleaseAccountHoldCommand command
    ) {
        validateCommand(command);

        AccountHold hold =
                accountHoldCommandRepository
                        .findById(command.holdId())
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.ACCOUNT_HOLD_NOT_FOUND
                                )
                        );

        if (hold.getStatus() != AccountHoldStatus.ACTIVE) {
            throw new BusinessException(
                    ErrorCode.ACCOUNT_HOLD_NOT_ACTIVE
            );
        }

        accountHoldPort.decreaseHold(
                hold.getAccountId(),
                hold.getAmount(),
                hold.getCurrency()
        );

        hold.setStatus(AccountHoldStatus.RELEASED);
        hold.setReleasedAt(Instant.now());
    }

    private void validateCommand(
            ReleaseAccountHoldCommand command
    ) {
        if (command == null
                || command.holdId() == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }
}