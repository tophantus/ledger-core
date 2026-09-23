package com.example.ledgercore.credit.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.credit.command.dto.IncreaseCreditFacilityHoldCommand;
import com.example.ledgercore.credit.command.port.inbound.IncreaseCreditFacilityHoldUseCase;
import com.example.ledgercore.credit.command.repository.CreditFacilityCommandRepository;
import com.example.ledgercore.credit.entity.CreditFacility;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class IncreaseCreditFacilityHoldHandler
        implements IncreaseCreditFacilityHoldUseCase {

    private final CreditFacilityCommandRepository
            creditFacilityCommandRepository;

    @Override
    @Transactional
    public void execute(
            IncreaseCreditFacilityHoldCommand command
    ) {
        if (command.creditFacilityId() == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (command.amount() == null
                || command.currency() == null
                || command.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        CreditFacility facility =
                creditFacilityCommandRepository
                        .findById(command.creditFacilityId())
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.CREDIT_FACILITY_NOT_FOUND
                                )
                        );

        if (facility.getCurrency() != command.currency()) {
            throw new BusinessException(
                    ErrorCode.ACCOUNT_CURRENCY_MISMATCH
            );
        }

        if (facility.getStatus()
                != com.example.ledgercore.credit.enums.CreditFacilityStatus.ACTIVE) {
            throw new BusinessException(
                    ErrorCode.CREDIT_FACILITY_NOT_ACTIVE
            );
        }

        facility.setHoldAmount(
                facility.getHoldAmount().add(command.amount())
        );

        creditFacilityCommandRepository.save(facility);
    }
}