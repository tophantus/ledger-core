package com.example.ledgercore.credit.query.handler;

import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import com.example.ledgercore.credit.entity.CreditFacility;
import com.example.ledgercore.credit.query.port.inbound.GetCreditFacilityLedgerAccountUseCase;
import com.example.ledgercore.credit.query.repository.CreditFacilityQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetCreditFacilityLedgerAccountHandler
        implements GetCreditFacilityLedgerAccountUseCase {

    private final CreditFacilityQueryRepository
            creditFacilityQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public UUID execute(UUID creditFacilityId) {
        if (creditFacilityId == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        CreditFacility facility =
                creditFacilityQueryRepository
                        .findById(creditFacilityId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.CREDIT_FACILITY_NOT_FOUND
                                )
                        );

        return facility.getLedgerAccountId();
    }
}