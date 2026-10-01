package com.example.ledgercore.transaction.adapter.outbound.credit;

import com.example.ledgercore.credit.query.dto.GetOwnedCreditFacilityQuery;
import com.example.ledgercore.credit.query.port.inbound.GetOwnedCreditFacilityUseCase;
import com.example.ledgercore.transaction.query.port.outbound.CreditFacilityAccessPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreditFacilityAccessAdapter
        implements CreditFacilityAccessPort {

    private final GetOwnedCreditFacilityUseCase
            getOwnedCreditFacilityUseCase;

    @Override
    public void verifyAccess(
            UUID userId,
            UUID creditFacilityId
    ) {
        getOwnedCreditFacilityUseCase.execute(
                new GetOwnedCreditFacilityQuery(
                        userId,
                        creditFacilityId
                )
        );
    }
}