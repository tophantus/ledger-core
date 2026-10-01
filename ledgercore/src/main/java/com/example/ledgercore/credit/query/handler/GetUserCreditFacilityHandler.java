package com.example.ledgercore.credit.query.handler;

import com.example.ledgercore.credit.entity.CreditFacility;
import com.example.ledgercore.credit.entity.CreditRepaymentMandate;
import com.example.ledgercore.credit.enums.CreditFacilityStatus;
import com.example.ledgercore.credit.enums.CreditRepaymentMandateStatus;
import com.example.ledgercore.credit.query.dto.GetUserCreditFacilityQuery;
import com.example.ledgercore.credit.query.dto.GetUserCreditFacilityResult;
import com.example.ledgercore.credit.query.port.inbound.GetUserCreditFacilityUseCase;
import com.example.ledgercore.credit.query.repository.CreditFacilityQueryRepository;
import com.example.ledgercore.credit.query.repository.CreditRepaymentMandateQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetUserCreditFacilityHandler
        implements GetUserCreditFacilityUseCase {

    private final CreditFacilityQueryRepository creditFacilityQueryRepository;
    private final CreditRepaymentMandateQueryRepository repaymentMandateQueryRepository;

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
        GetUserCreditFacilityResult.RepaymentMandateResult mandate =
                repaymentMandateQueryRepository
                        .findByCreditFacilityIdAndStatus(
                                facility.getId(),
                                CreditRepaymentMandateStatus.ACTIVE
                        )
                        .map(this::toRepaymentMandateResult)
                        .orElse(null);

        return new GetUserCreditFacilityResult(
                facility.getId(),
                facility.getCustomerId(),
                facility.getProductId(),
                facility.getCreditLimit().toPlainString(),
                facility.getOutstandingBalance().toPlainString(),
                facility.getHoldAmount().toPlainString(),
                facility.getAvailableCredit().toPlainString(),
                facility.getCurrency(),
                facility.getStatus(),
                facility.getOpenedAt(),
                mandate
        );
    }

    private GetUserCreditFacilityResult.RepaymentMandateResult
    toRepaymentMandateResult(CreditRepaymentMandate mandate) {

        return new GetUserCreditFacilityResult.RepaymentMandateResult(
                mandate.getId(),
                mandate.getAccountId(),
                mandate.getRepaymentType(),
                mandate.getStatus(),
                mandate.getCreatedAt(),
                mandate.getUpdatedAt()
        );
    }
}