package com.example.ledgercore.credit.query.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.credit.entity.CreditFacility;
import com.example.ledgercore.credit.query.dto.GetOwnedCreditFacilityQuery;
import com.example.ledgercore.credit.query.dto.OwnedCreditFacilityInfo;
import com.example.ledgercore.credit.query.port.inbound.GetOwnedCreditFacilityUseCase;
import com.example.ledgercore.credit.query.repository.CreditFacilityQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetOwnedCreditFacilityHandler
        implements GetOwnedCreditFacilityUseCase {

    private final CreditFacilityQueryRepository creditFacilityQueryRepository;

    @Override
    public OwnedCreditFacilityInfo execute(
            GetOwnedCreditFacilityQuery query
    ) {
        CreditFacility facility =
                creditFacilityQueryRepository
                        .findByIdAndCustomerId(
                                query.creditFacilityId(),
                                query.customerId()
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.CREDIT_FACILITY_NOT_FOUND
                                )
                        );

        BigDecimal availableCredit = facility.getCreditLimit()
                .subtract(facility.getOutstandingBalance())
                .subtract(facility.getHoldAmount());

        return new OwnedCreditFacilityInfo(
                facility.getId(),
                facility.getCustomerId(),
                facility.getProductId(),
                availableCredit,
                facility.getCurrency(),
                facility.getStatus()
        );
    }
}