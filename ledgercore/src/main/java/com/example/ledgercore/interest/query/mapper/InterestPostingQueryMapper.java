package com.example.ledgercore.interest.query.mapper;

import com.example.ledgercore.interest.entity.InterestPosting;
import com.example.ledgercore.interest.query.dto.InterestPostingResponse;

public final class InterestPostingQueryMapper {

    private InterestPostingQueryMapper() {
    }

    public static InterestPostingResponse toResponse(
            InterestPosting posting
    ) {
        return new InterestPostingResponse(
                posting.getId(),
                posting.getAccountId(),
                posting.getRunId(),
                posting.getPeriodStart(),
                posting.getPeriodEnd(),
                posting.getInterestAmount(),
                posting.getTransactionId(),
                posting.getPostedAt(),
                posting.getCreatedAt()
        );
    }
}