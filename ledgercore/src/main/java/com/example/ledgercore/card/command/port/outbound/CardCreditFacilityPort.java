package com.example.ledgercore.card.command.port.outbound;

import com.example.ledgercore.card.command.port.outbound.dto.CardCreditFacilityInfo;

import java.util.UUID;

public interface CardCreditFacilityPort {

    CardCreditFacilityInfo getOwnedCreditFacility(
            UUID customerId,
            UUID creditFacilityId
    );
}