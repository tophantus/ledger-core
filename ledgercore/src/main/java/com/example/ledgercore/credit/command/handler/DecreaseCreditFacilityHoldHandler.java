package com.example.ledgercore.credit.command.handler;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.credit.command.dto.DecreaseCreditFacilityHoldCommand;
import com.example.ledgercore.credit.command.port.inbound.DecreaseCreditFacilityHoldUseCase;
import com.example.ledgercore.credit.command.repository.CreditFacilityCommandRepository;
import com.example.ledgercore.credit.entity.CreditFacility;
import com.example.ledgercore.credit.enums.CreditFacilityStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DecreaseCreditFacilityHoldHandler
        implements DecreaseCreditFacilityHoldUseCase {

    private final CreditFacilityCommandRepository
            creditFacilityCommandRepository;

    @Override
    @Transactional
    public void execute(
            DecreaseCreditFacilityHoldCommand command
    ) {
        if (command == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        validateInput(
                command.creditFacilityId(),
                command.amount(),
                command.currency()
        );

        CreditFacility facility =
                creditFacilityCommandRepository
                        .findById(command.creditFacilityId())
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.CREDIT_FACILITY_NOT_FOUND
                                )
                        );

        if (facility.getStatus() != CreditFacilityStatus.ACTIVE) {
            throw new BusinessException(
                    ErrorCode.CREDIT_FACILITY_NOT_ACTIVE
            );
        }

        if (facility.getCurrency() != command.currency()) {
            throw new BusinessException(
                    ErrorCode.CREDIT_FACILITY_CURRENCY_MISMATCH
            );
        }

        if (facility.getHoldAmount().compareTo(command.amount()) < 0) {
            throw new BusinessException(
                    ErrorCode.CREDIT_HOLD_AMOUNT_INSUFFICIENT
            );
        }

        facility.setHoldAmount(
                facility.getHoldAmount().subtract(command.amount())
        );
    }

    private void validateInput(
            UUID creditFacilityId,
            BigDecimal amount,
            Currency currency
    ) {
        if (creditFacilityId == null
                || amount == null
                || amount.signum() <= 0
                || currency == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }
}