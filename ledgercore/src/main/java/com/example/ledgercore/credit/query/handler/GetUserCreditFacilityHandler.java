package com.example.ledgercore.credit.query.handler;

import com.example.ledgercore.credit.entity.CreditFacility;
import com.example.ledgercore.credit.enums.CreditFacilityStatus;
import com.example.ledgercore.credit.query.dto.GetUserCreditFacilityQuery;
import com.example.ledgercore.credit.query.dto.GetUserCreditFacilityResult;
import com.example.ledgercore.credit.query.port.inbound.GetUserCreditFacilityUseCase;
import com.example.ledgercore.credit.query.repository.CreditFacilityQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetUserCreditFacilityHandler
        implements GetUserCreditFacilityUseCase {

    private final CreditFacilityQueryRepository creditFacilityQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<GetUserCreditFacilityResult> execute(
            GetUserCreditFacilityQuery query
    ) {
        return creditFacilityQueryRepository
                .findFirstByCustomerIdAndStatus(
                        query.userId(),
                        CreditFacilityStatus.ACTIVE
                )
                .map(this::toResult);
    }

    private GetUserCreditFacilityResult toResult(
            CreditFacility facility
    ) {
        return new GetUserCreditFacilityResult(
                facility.getId(),
                facility.getCustomerId(),
                facility.getProductId(),
                facility.getCreditLimit().toPlainString(),
                facility.getOutstandingBalance().toPlainString(),
                facility.getCurrency(),
                facility.getStatus(),
                facility.getOpenedAt()
        );
    }
}