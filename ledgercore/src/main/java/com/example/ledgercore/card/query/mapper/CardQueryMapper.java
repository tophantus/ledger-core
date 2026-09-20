package com.example.ledgercore.card.query.mapper;

import com.example.ledgercore.card.entity.Card;
import com.example.ledgercore.card.query.dto.CardInfo;

public final class CardQueryMapper {

    private CardQueryMapper() {
    }

    public static CardInfo toCardInfo(Card card) {
        return new CardInfo(
                card.getId(),
                card.getCustomerId(),
                card.getType(),
                card.getForm(),
                card.getStatus(),
                card.getAccountId(),
                card.getCreditFacilityId(),
                card.getPanLast4(),
                card.getExpiryMonth(),
                card.getExpiryYear(),
                card.getIssuedAt(),
                card.getActivatedAt(),
                card.getClosedAt()
        );
    }
}