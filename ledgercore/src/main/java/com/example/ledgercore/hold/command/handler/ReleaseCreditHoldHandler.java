package com.example.ledgercore.hold.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.hold.command.dto.ReleaseCreditHoldCommand;
import com.example.ledgercore.hold.command.port.inbound.ReleaseCreditHoldUseCase;
import com.example.ledgercore.hold.command.port.outbound.CreditHoldPort;
import com.example.ledgercore.hold.command.repository.CreditHoldCommandRepository;
import com.example.ledgercore.hold.entity.CreditHold;
import com.example.ledgercore.hold.enums.CreditHoldStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ReleaseCreditHoldHandler
        implements ReleaseCreditHoldUseCase {

    private final CreditHoldCommandRepository
            creditHoldCommandRepository;

    private final CreditHoldPort creditHoldPort;

    @Override
    @Transactional
    public void execute(
            ReleaseCreditHoldCommand command
    ) {
        validateCommand(command);

        CreditHold hold =
                creditHoldCommandRepository
                        .findById(command.holdId())
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.CREDIT_HOLD_NOT_FOUND
                                )
                        );

        if (hold.getStatus() != CreditHoldStatus.ACTIVE) {
            throw new BusinessException(
                    ErrorCode.CREDIT_HOLD_NOT_ACTIVE
            );
        }

        creditHoldPort.decreaseHold(
                hold.getCreditFacilityId(),
                hold.getAmount(),
                hold.getCurrency()
        );

        hold.setStatus(CreditHoldStatus.RELEASED);
        hold.setReleasedAt(Instant.now());
    }

    private void validateCommand(
            ReleaseCreditHoldCommand command
    ) {
        if (command == null
                || command.holdId() == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }
}