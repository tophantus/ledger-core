package com.example.ledgercore.card.command.port.outbound;

import com.example.ledgercore.card.enums.CardAuthorizationHoldType;

import java.util.UUID;

public interface CardAuthorizationHoldPort {

    void releaseHold(
            CardAuthorizationHoldType holdType,
            UUID holdId
    );
}