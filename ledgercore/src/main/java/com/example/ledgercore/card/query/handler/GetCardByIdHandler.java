package com.example.ledgercore.card.query.handler;

import com.example.ledgercore.card.entity.Card;
import com.example.ledgercore.card.query.dto.CardInfo;
import com.example.ledgercore.card.query.dto.GetCardByIdQuery;
import com.example.ledgercore.card.query.port.inbound.GetCardByIdUseCase;
import com.example.ledgercore.card.query.repository.CardQueryRepository;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetCardByIdHandler
        implements GetCardByIdUseCase {

    private final CardQueryRepository cardQueryRepository;

    @Override
    public CardInfo execute(GetCardByIdQuery query) {
        Card card = cardQueryRepository
                .findByIdAndCustomerId(
                        query.cardId(),
                        query.customerId()
                )
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.CARD_NOT_FOUND
                        )
                );

        return toCardInfo(card);
    }

    private CardInfo toCardInfo(Card card) {
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