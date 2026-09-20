package com.example.ledgercore.card.command.port.outbound;

import com.example.ledgercore.card.command.port.outbound.dto.CardProductInfo;

import java.util.UUID;

public interface CardProductPort {

    CardProductInfo getActiveProduct(UUID productId);
}