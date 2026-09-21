package com.example.ledgercore.hold.command.handler;

import com.example.ledgercore.common.currency.CurrencyAmountPolicy;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.hold.command.dto.CreateCreditHoldCommand;
import com.example.ledgercore.hold.command.dto.CreateCreditHoldResponse;
import com.example.ledgercore.hold.command.port.inbound.CreateCreditHoldUseCase;
import com.example.ledgercore.hold.command.port.outbound.CreditHoldPort;
import com.example.ledgercore.hold.command.repository.CreditHoldCommandRepository;
import com.example.ledgercore.hold.entity.CreditHold;
import com.example.ledgercore.hold.enums.CreditHoldStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CreateCreditHoldHandler
        implements CreateCreditHoldUseCase {

    private final CreditHoldCommandRepository
            creditHoldCommandRepository;

    private final CreditHoldPort creditHoldPort;

    @Override
    @Transactional
    public CreateCreditHoldResponse execute(
            CreateCreditHoldCommand command
    ) {
        validateCommand(command);

        CreditHold hold =
                CreditHold.builder()
                        .creditFacilityId(
                                command.creditFacilityId()
                        )
                        .amount(command.amount())
                        .currency(command.currency())
                        .holdType(command.holdType())
                        .referenceType(command.referenceType())
                        .referenceId(command.referenceId())
                        .status(CreditHoldStatus.ACTIVE)
                        .build();

        hold = creditHoldCommandRepository.save(hold);

        creditHoldPort.increaseHold(
                hold.getCreditFacilityId(),
                hold.getAmount(),
                hold.getCurrency()
        );

        return new CreateCreditHoldResponse(
                hold.getId(),
                hold.getCreditFacilityId(),
                hold.getAmount(),
                hold.getCurrency(),
                hold.getStatus(),
                hold.getExpiresAt()
        );
    }

    private void validateCommand(
            CreateCreditHoldCommand command
    ) {
        if (command == null
                || command.creditFacilityId() == null
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

        CurrencyAmountPolicy.validate(
                command.amount(),
                command.currency()
        );
    }
}