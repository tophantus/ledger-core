package com.example.ledgercore.card.command.port.outbound;

import com.example.ledgercore.card.command.port.outbound.dto.CardAccountInfo;

import java.util.UUID;

public interface CardAccountPort {

    CardAccountInfo getOwnedAccount(
            UUID customerId,
            UUID accountId
    );
}