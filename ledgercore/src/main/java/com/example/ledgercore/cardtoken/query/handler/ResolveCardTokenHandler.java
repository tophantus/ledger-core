package com.example.ledgercore.cardtoken.query.handler;

import com.example.ledgercore.cardtoken.entity.CardToken;
import com.example.ledgercore.cardtoken.enums.CardTokenStatus;
import com.example.ledgercore.cardtoken.query.dto.ResolveCardTokenQuery;
import com.example.ledgercore.cardtoken.query.dto.ResolveCardTokenResult;
import com.example.ledgercore.cardtoken.query.port.inbound.ResolveCardTokenUseCase;
import com.example.ledgercore.cardtoken.query.repository.CardTokenQueryRepository;
import com.example.ledgercore.common.exception.BusinessException;
import com.example.ledgercore.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResolveCardTokenHandler
        implements ResolveCardTokenUseCase {

    private final CardTokenQueryRepository
            cardTokenQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public ResolveCardTokenResult execute(
            ResolveCardTokenQuery query
    ) {
        validateQuery(query);

        String token = query.token().trim();

        CardToken cardToken =
                cardTokenQueryRepository
                        .findByTokenAndStatus(
                                token,
                                CardTokenStatus.ACTIVE
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.CARD_TOKEN_NOT_FOUND
                                )
                        );

        return new ResolveCardTokenResult(
                cardToken.getCardId()
        );
    }

    private void validateQuery(
            ResolveCardTokenQuery query
    ) {
        if (query == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (query.token() == null
                || query.token().isBlank()) {
            throw new BusinessException(
                    ErrorCode.CARD_TOKEN_REQUIRED
            );
        }
    }
}