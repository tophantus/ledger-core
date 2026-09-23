package com.example.ledgercore.credit.command.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.credit.command.dto
        .DecreaseCreditFacilityOutstandingBalanceCommand;
import com.example.ledgercore.credit.command.port.inbound
        .DecreaseCreditFacilityOutstandingBalanceUseCase;
import com.example.ledgercore.credit.command.repository
        .CreditFacilityCommandRepository;
import com.example.ledgercore.credit.command.service.CreditDailyBalanceService;
import com.example.ledgercore.credit.entity.CreditFacility;
import com.example.ledgercore.credit.enums.CreditFacilityStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class DecreaseCreditFacilityOutstandingBalanceHandler
        implements DecreaseCreditFacilityOutstandingBalanceUseCase {

    private final CreditFacilityCommandRepository
            creditFacilityCommandRepository;

    private final CreditDailyBalanceService
            creditDailyBalanceService;

    @Override
    @Transactional
    public void execute(
            DecreaseCreditFacilityOutstandingBalanceCommand command
    ) {
        validateCommand(command);

        CreditFacility facility =
                creditFacilityCommandRepository
                        .findById(command.creditFacilityId())
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.CREDIT_FACILITY_NOT_FOUND
                                )
                        );

        validateFacility(facility, command);

        if (facility.getOutstandingBalance()
                .compareTo(command.amount()) < 0) {

            throw new BusinessException(
                    ErrorCode.CREDIT_OUTSTANDING_BALANCE_INSUFFICIENT
            );
        }

        BigDecimal newOutstandingBalance =
                facility.getOutstandingBalance()
                        .subtract(command.amount());

        facility.setOutstandingBalance(
                newOutstandingBalance
        );

        facility.setUpdatedAt(Instant.now());

        creditDailyBalanceService.updateClosingBalance(
                facility.getId(),
                command.businessDate(),
                newOutstandingBalance
        );
    }

    private void validateCommand(
            DecreaseCreditFacilityOutstandingBalanceCommand command
    ) {
        if (command == null
                || command.creditFacilityId() == null
                || command.amount() == null
                || command.currency() == null) {

            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (command.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_AMOUNT
            );
        }
    }

    private void validateFacility(
            CreditFacility facility,
            DecreaseCreditFacilityOutstandingBalanceCommand command
    ) {
        if (facility.getStatus()
                != CreditFacilityStatus.ACTIVE) {

            throw new BusinessException(
                    ErrorCode.CREDIT_FACILITY_NOT_ACTIVE
            );
        }

        if (facility.getCurrency()
                != command.currency()) {

            throw new BusinessException(
                    ErrorCode.CREDIT_FACILITY_CURRENCY_MISMATCH
            );
        }
    }
}