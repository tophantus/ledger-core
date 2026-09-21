package com.example.ledgercore.card.adapter.outbound.credit;

import com.example.ledgercore.card.command.port.outbound.CardCreditFacilityPort;
import com.example.ledgercore.card.command.port.outbound.dto.CardCreditFacilityInfo;
import com.example.ledgercore.credit.query.dto.GetOwnedCreditFacilityQuery;
import com.example.ledgercore.credit.query.dto.OwnedCreditFacilityInfo;
import com.example.ledgercore.credit.query.port.inbound.GetOwnedCreditFacilityUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CardCreditFacilityAdapter
        implements CardCreditFacilityPort {

    private final GetOwnedCreditFacilityUseCase
            getOwnedCreditFacilityUseCase;

    @Override
    public CardCreditFacilityInfo getOwnedCreditFacility(
            UUID customerId,
            UUID creditFacilityId
    ) {
        OwnedCreditFacilityInfo facility =
                getOwnedCreditFacilityUseCase.execute(
                        new GetOwnedCreditFacilityQuery(
                                customerId,
                                creditFacilityId
                        )
                );

        return new CardCreditFacilityInfo(
                facility.id(),
                facility.customerId(),
                facility.productId(),
                facility.creditLimit(),
                facility.outstandingBalance(),
                facility.currency(),
                facility.status()
        );
    }
}