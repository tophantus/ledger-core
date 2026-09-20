package com.example.ledgercore.card.query.handler;

import com.example.ledgercore.card.entity.Card;
import com.example.ledgercore.card.query.dto.CardInfo;
import com.example.ledgercore.card.query.dto.GetUserCardsQuery;
import com.example.ledgercore.card.query.mapper.CardQueryMapper;
import com.example.ledgercore.card.query.port.inbound.GetUserCardsUseCase;
import com.example.ledgercore.card.query.repository.CardQueryRepository;
import com.example.ledgercore.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetUserCardsHandler
        implements GetUserCardsUseCase {

    private final CardQueryRepository cardQueryRepository;

    @Override
    public PageResponse<CardInfo> execute(
            GetUserCardsQuery query
    ) {
        Pageable pageable = PageRequest.of(
                query.page(),
                query.size(),
                Sort.by(
                        Sort.Direction.DESC,
                        "createdAt"
                )
        );

        Page<Card> cardPage =
                cardQueryRepository.findByCustomerId(
                        query.customerId(),
                        pageable
                );

        return new PageResponse<>(
                cardPage.getContent()
                        .stream()
                        .map(CardQueryMapper::toCardInfo)
                        .toList(),
                cardPage.getNumber(),
                cardPage.getSize(),
                cardPage.getTotalElements(),
                cardPage.getTotalPages()
        );
    }
}