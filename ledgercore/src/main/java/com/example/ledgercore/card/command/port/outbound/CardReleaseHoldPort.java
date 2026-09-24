package com.example.ledgercore.card.command.port.outbound;

import com.example.ledgercore.card.enums.CardAuthorizationHoldType;

import java.util.UUID;

public interface CardReleaseHoldPort {

    void releaseHold(
            CardAuthorizationHoldType holdType,
            UUID holdId
    );
}