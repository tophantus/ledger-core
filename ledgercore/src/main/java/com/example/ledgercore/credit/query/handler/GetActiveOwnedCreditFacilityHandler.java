package com.example.ledgercore.credit.query.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.credit.entity.CreditFacility;
import com.example.ledgercore.credit.enums.CreditFacilityStatus;
import com.example.ledgercore.credit.query.dto.GetActiveOwnedCreditFacilityQuery;
import com.example.ledgercore.credit.query.dto.GetActiveOwnedCreditFacilityResult;
import com.example.ledgercore.credit.query.port.inbound.GetActiveOwnedCreditFacilityUseCase;
import com.example.ledgercore.credit.query.repository.CreditFacilityQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetActiveOwnedCreditFacilityHandler
        implements GetActiveOwnedCreditFacilityUseCase {

    private final CreditFacilityQueryRepository creditFacilityQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public GetActiveOwnedCreditFacilityResult execute(
            GetActiveOwnedCreditFacilityQuery query
    ) {
        validateQuery(query);

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

        if (facility.getStatus() != CreditFacilityStatus.ACTIVE) {
            throw new BusinessException(
                    ErrorCode.CREDIT_FACILITY_NOT_ACTIVE
            );
        }

        return new GetActiveOwnedCreditFacilityResult(
                facility.getId(),
                facility.getCustomerId(),
                facility.getProductId(),
                facility.getCreditLimit(),
                facility.getOutstandingBalance(),
                facility.getCurrency()
        );
    }

    private void validateQuery(
            GetActiveOwnedCreditFacilityQuery query
    ) {
        if (query == null
                || query.customerId() == null
                || query.creditFacilityId() == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }
    }
}