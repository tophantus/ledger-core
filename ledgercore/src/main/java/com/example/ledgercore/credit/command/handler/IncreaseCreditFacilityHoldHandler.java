package com.example.ledgercore.credit.command.handler;

import com.example.ledgercore.common.currency.Currency;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.credit.command.port.inbound.IncreaseCreditFacilityHoldUseCase;
import com.example.ledgercore.credit.command.repository.CreditFacilityCommandRepository;
import com.example.ledgercore.credit.entity.CreditFacility;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IncreaseCreditFacilityHoldHandler
        implements IncreaseCreditFacilityHoldUseCase {

    private final CreditFacilityCommandRepository
            creditFacilityCommandRepository;

    @Override
    @Transactional
    public void execute(
            UUID creditFacilityId,
            BigDecimal amount,
            Currency currency
    ) {
        if (creditFacilityId == null
                || amount == null
                || currency == null
                || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        CreditFacility facility =
                creditFacilityCommandRepository
                        .findById(creditFacilityId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.CREDIT_FACILITY_NOT_FOUND
                                )
                        );

        if (facility.getCurrency() != currency) {
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
                facility.getHoldAmount().add(amount)
        );

        creditFacilityCommandRepository.save(facility);
    }
}